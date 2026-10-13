package br.com.conde.raiz;

/**
 * O MISTO: tenta o passo da secante e cai na bisseccao quando ele nao presta.
 *
 * <p>Ele e a resposta de engenharia para o repositorio inteiro, e a ideia cabe numa frase:
 * a cada passo, calcula onde a secante mandaria; se esse ponto esta dentro do cerco e
 * encolhe o intervalo o bastante, usa; senao, corta ao meio.
 *
 * <p>Com isso ele herda as duas coisas boas e nenhuma das ruins. Mantem o cerco, entao a
 * convergencia e garantida por teorema, igual a bisseccao. E anda com ordem perto da
 * secante quando a funcao colabora, que e quase sempre.
 *
 * <p>O "encolhe o bastante" e a parte que evita a estagnacao da falsa posicao, e ela
 * precisa ser escrita com cuidado. A primeira versao desta classe exigia que o atalho
 * caisse a menos de meia largura do meio, o que TODO ponto dentro do cerco satisfaz: a
 * salvaguarda nunca disparava e o metodo era a falsa posicao com outro nome, inclusive
 * travando nas mesmas tres funcoes.
 *
 * <p>A regra que funciona olha duas iteracoes para tras: se o cerco nao encolheu pelo menos
 * pela metade desde entao, esta e uma bisseccao, sem discussao.
 *
 * <p>Dai sai o limite, e vale fazer a conta com cuidado. Se a regra nao dispara na iteracao
 * n, o cerco encolheu pela metade em duas iteracoes; se dispara, aquela iteracao e uma
 * bisseccao e o cerco encolhe pela metade nela. Em tres iteracoes consecutivas, portanto, o
 * cerco sempre pelo menos se parte ao meio: o orcamento e TRES vezes o da bisseccao, e nao
 * dois. O teste afirma o triplo porque o dobro nao passava.
 *
 * <p>E essencialmente o que toda biblioteca seria usa, com o nome de Brent ou de Dekker. A
 * versao de verdade tem tambem interpolacao de tres pontos e cuidados de ponto flutuante
 * que nao estao aqui; o que esta aqui e a ideia que faz a diferenca.
 */
public final class Misto implements Metodo {

    @Override
    public String nome() {
        return "misto";
    }

    @Override
    public boolean mantemOCerco() {
        return true;
    }

    @Override
    public boolean precisaDaDerivada() {
        return false;
    }

    @Override
    public Resultado achar(Funcao funcao, double tolerancia, int maximoDeIteracoes) {
        Contador contador = new Contador();

        double esquerda = funcao.de();
        double direita = funcao.ate();

        double naEsquerda = funcao.em(esquerda, contador);
        double naDireita = funcao.em(direita, contador);

        if (naEsquerda * naDireita > 0)
            return Resultado.falhou(nome(), funcao, Double.NaN, contador,
                    "as duas pontas tem o mesmo sinal: nao ha cerco");

        double larguraHaDuas = Math.abs(direita - esquerda);
        double larguraHaUma = larguraHaDuas;

        while (contador.iteracoes() < maximoDeIteracoes) {
            contador.iteracao();

            double largura = direita - esquerda;

            if (Math.abs(largura) < tolerancia)
                return Resultado.achou(nome(), funcao, esquerda + largura / 2, contador);

            double meio = esquerda + largura / 2;

            double candidato = meio;

            /* Se o cerco nao encolheu pela metade nas duas ultimas iteracoes, esta e uma
             * bisseccao. E isto que impede a ponta parada da falsa posicao. */
            boolean forcarBisseccao = Math.abs(largura) > larguraHaDuas / 2;

            if (!forcarBisseccao && naDireita != naEsquerda) {
                double daSecante = (esquerda * naDireita - direita * naEsquerda)
                        / (naDireita - naEsquerda);

                boolean dentro = daSecante > Math.min(esquerda, direita)
                        && daSecante < Math.max(esquerda, direita);

                if (dentro) candidato = daSecante;
            }

            larguraHaDuas = larguraHaUma;
            larguraHaUma = Math.abs(largura);

            double noCandidato = funcao.em(candidato, contador);

            if (noCandidato == 0) return Resultado.achou(nome(), funcao, candidato, contador);

            if (naEsquerda * noCandidato < 0) {
                direita = candidato;
                naDireita = noCandidato;
            } else {
                esquerda = candidato;
                naEsquerda = noCandidato;
            }
        }

        return Resultado.falhou(nome(), funcao, esquerda + (direita - esquerda) / 2, contador,
                "passou de " + maximoDeIteracoes + " iteracoes");
    }
}
