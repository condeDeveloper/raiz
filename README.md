# raiz

Busca de raiz do zero em Java 21: **contando o que custa, a secante ganha de Newton.**

O juiz é a **bisseção levada até a precisão da máquina**. Ela é o juiz certo e não só o
conveniente porque é a única cuja convergência é um **teorema** e não uma esperança: com a função
contínua e as duas pontas de sinais opostos, o teorema do valor intermediário garante que há raiz
dentro, e cortar ao meio preserva isso para sempre.

Ela não supõe derivada, não supõe suavidade, não supõe que a função se pareça com uma reta.
Funciona numa função com bico, com degrau, ou numa tabela de medidas. Nenhum outro método daqui
pode dizer isso.

E o juiz exige **correção, não velocidade**: que quem diz que achou a raiz tenha achado, e que
quem mantém o cerco nunca se perca. Não exige que Newton convirja; a tabela das falhas conta de
onde ele não volta, e contar isso seria impossível se o juiz reprovasse.

A unidade é a **avaliação da função**, nunca o segundo. Num problema de verdade a função é uma
simulação, um sistema linear ou uma consulta cara, e tudo o que o método faz em volta dela some
perto disso.

## Newton usa menos passos e mais contas

```
$ mvn exec:java -Dexec.args="convergencia"

  funcao               metodo              convergiu    iteracoes    da funcao    da derivada     avaliacoes
  x2 menos 2           bisseccao                 sim           41           42              0             42
  x2 menos 2           misto                     sim           14           15              0             15
  x2 menos 2           falsa posicao             sim           17           19              0             19
  x2 menos 2           secante                   sim            7            9              0              9
  x2 menos 2           Newton                    sim            5            6              5             11
  cubica de Wallis     secante                   sim            7            9              0              9
  cubica de Wallis     Newton                    sim            6            7              6             13
```

Em **iterações** Newton ganha, como todo mundo diz: 5 contra 7. Em **avaliações** ele perde, 11
contra 9, porque cada passo dele gasta a função **e a derivada**.

A conta de ordem de convergência diz a mesma coisa quando feita por avaliação em vez de por passo:
Newton tem ordem 2 por passo e duas avaliações por passo, o que dá √2 ≈ 1,414 por avaliação; a
secante tem ordem 1,618 por passo e uma avaliação por passo. O número de ouro ganha da raiz de
dois, e essa é a comparação que importa quando a função é cara.

E há a desvantagem de Newton que nenhuma tabela de velocidade mostra: **ele precisa da derivada**.
Numa função que vem de uma simulação ela não existe, e aproximá-la custa mais uma avaliação — o
que transforma Newton numa secante mais cara.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `convergencia`, `falhas`, `estagnacao` e `multipla`.

## O que as medidas me corrigiram

**Escrevi que quem mantém o cerco sempre converge. Converge, e numa das funções isso levaria
10²⁴ iterações.**

O argumento parecia fechado: manter o cerco garante que a raiz está lá dentro e que o intervalo
nunca cresce, logo a convergência é um teorema. É verdade, e é um teorema **sobre o limite**, que
não dá prazo nenhum.

```
$ mvn exec:java -Dexec.args="multipla"

  metodo              convergiu    iteracoes     avaliacoes           erro    razao do erro
  bisseccao                 sim           43             44       5.68e-14           0.5000
  misto                     sim           90             91       2.51e-13           0.7257
  falsa posicao             nao          200            202            NaN           0.9823
  secante                   sim           93             95       2.81e-12           0.7549
  Newton                    sim           64            129       1.34e-12           0.6667
```

Na raiz tripla de `(x-1)³`, a falsa posição tira **menos de dois por cento do erro por passo**.
O erro dela encolhe como `1/√n`: para chegar a doze casas seriam uns 10²⁴ passos. Meu juiz a
reprovava por isso, e o errado era o juiz.

Hoje o contrato dele é o que de fato se pode garantir: **quem mantém o cerco nunca se perde** —
mesmo sem convergir, o ponto devolvido continua dentro do intervalo original. Velocidade é
assunto das tabelas.

E a mesma tabela tem o resultado que eu não esperava: **na raiz tripla a bisseção ganha de todos.**
44 avaliações contra 91 do misto, 95 da secante e 129 de Newton. A coluna da direita explica em um
número: numa raiz de multiplicidade `m`, Newton passa a ganhar o fator `(m-1)/m` por passo. Com
`m = 3` isso é **0,6667**, e dois terços por passo é pior que a metade por passo da bisseção. O
método mais sofisticado perde para o mais bobo, e perde por um motivo que se escreve numa fração.

### A parada pelo tamanho do passo mente

A secante, parando só quando o passo fica menor que a tolerância, anunciou que tinha achado a raiz
de `exp(x) - 100` a **4,5 unidades** de distância dela.

Não é um erro de implementação. Perto de zero a exponencial é quase plana, os passos ficam
minúsculos porque a função mal muda, e o teste de parada confunde "a função é plana aqui" com
"cheguei". Hoje os dois métodos sem cerco exigem também que o **resíduo** seja pequeno, e nessa
função eles passam a admitir que não acharam, em vez de mentir.

A opção de parar só pelo passo continua no código, com um construtor próprio, porque é ela que o
teste usa para provar que a mentira acontece.

### E a salvaguarda do método misto nunca disparava

O método misto tenta o passo da secante e cai na bisseção quando ele não presta. A primeira versão
exigia que o atalho caísse a menos de meia largura do meio do intervalo — condição que **todo**
ponto dentro do cerco satisfaz. A salvaguarda nunca disparava, e o método travava exatamente nas
mesmas três funções que a falsa posição.

A regra que funciona olha duas iterações para trás: se o cerco não encolheu pela metade desde
então, esta é uma bisseção, sem discussão. Daí sai um orçamento que dá para escrever: em três
iterações consecutivas o cerco sempre se parte ao meio, então ele gasta no máximo **três vezes** o
que a bisseção gastaria. O teste afirma o triplo porque o dobro não passava.

## De onde Newton não volta

```
$ mvn exec:java -Dexec.args="falhas"

  funcao                   pontos    convergiram       fracao  menor que falha  maior que falha
  x2 menos 2                 2001           2001       1.0000              NaN              NaN
  cubica de Wallis           2001           2001       1.0000              NaN              NaN
  arco tangente              2001            795       0.3973          -3.0000           4.0000
  exp menos 100              2001           2001       1.0000              NaN              NaN
  x10 menos 1                2001           2001       1.0000              NaN              NaN
  (x menos 1) ao cubo        2001           2001       1.0000              NaN              NaN

  A fronteira exata do arco tangente, achada por bisseccao: 1.391745200271
```

Dois mil e um pontos de partida igualmente espaçados em cada intervalo. Em cinco das seis funções
Newton volta de **todos**. No arco tangente ele volta de **39,73%**.

A função é lisa, tem derivada em toda parte e a derivada nunca zera. Nada nela avisa. O que
acontece é geométrico: a partir de um ponto, a tangente é tão deitada que ela cruza o eixo mais
longe da raiz do que o ponto estava, e cada passo joga mais longe que o anterior.

A fronteira é exata e tem doze casas aqui: **1,391745200271**. Ela é a raiz de uma equação que
também não tem forma fechada, então achar de onde Newton falha exigiu um método numérico. Foi a
bisseção.

## A ponta que nunca anda

```
$ mvn exec:java -Dexec.args="estagnacao"

  funcao                falsa posicao        bisseccao parada a esquerda parada a direita  fracao parada vezes a bisseccao
  x2 menos 2                       17               41                0               16         0.9412             0.41
  arco tangente                     6               44                1                1         0.1667             0.14
  exp menos 100                   200               45                0              200         1.0000             4.44
  x10 menos 1                     200               42                0              200         1.0000             4.76
```

A falsa posição é a bisseção que corta onde a reta entre as pontas cruza o eixo, em vez de cortar
no meio. A ideia é de antes de Cristo e parece obviamente melhor: usa o valor da função e não só o
sinal.

E ela **não é sempre melhor nem sempre pior**. Na raiz de dois ela gasta 0,41 do que a bisseção
gasta; na exponencial ela gasta 4,44 vezes e nem assim converge.

A diferença é a curvatura. Com curvatura de um sinal só no intervalo, a reta fica sempre do mesmo
lado da curva e o corte cai sempre do mesmo lado da raiz: **uma das pontas não anda nas 200
iterações**, nenhuma vez. No arco tangente, que muda de curvatura exatamente na raiz, isso não
acontece e ela é a mais rápida da tabela.

O conserto tem nome, Illinois, e é uma linha: quando uma ponta repete, divide-se o valor dela por
dois para puxar a reta. Ele não está aqui de propósito, porque o assunto desta tabela é o que
acontece sem ele.

## O que não tem aqui

**Raízes complexas.** Tudo aqui é real e cercado por troca de sinal. Uma raiz complexa não tem
troca de sinal nenhuma para cercar, e os métodos com cerco simplesmente não se aplicam.

**Várias raízes no intervalo.** O cerco garante que há **uma** raiz dentro, não qual delas. Com
três raízes no intervalo, a bisseção acha uma e não diz que há outras.

**Sistemas.** Newton em várias variáveis troca a derivada por uma matriz e a divisão por resolver
um sistema linear, e aí o custo por passo muda de categoria. O que esta tabela mede continua
valendo, e o eixo passa a ser outro.

**Precisão além do `double`.** A tolerância de 1e-12 cabe com folga, mas perto da raiz tripla o
resíduo é da ordem de 1e-38 e o ponto flutuante já não distingue nada: a função vale zero numa
faixa inteira. É por isso que o erro ali é 1e-13 e não 1e-16, em todos os métodos.

## Licença

MIT.
