package br.com.conde.raiz;

/**
 * A FALSA POSICAO: igual a bisseccao, mas corta onde a reta entre as pontas cruza o eixo.
 *
 * <p>A ideia parece obviamente melhor e e de antes de Cristo. A bisseccao joga fora o valor
 * da funcao e usa so o sinal; esta usa o valor, tracando a reta entre as duas pontas e
 * cortando onde ela cruza. Se a funcao for quase uma reta, acerta quase de primeira.
 *
 * <p>Ela mantem o cerco, entao herda a garantia da bisseccao: converge sempre.
 *
 * <h2>E numa funcao convexa ela fica pior que a bisseccao</h2>
 *
 * <p>Numa funcao com curvatura de um sinal so, a reta fica sempre do mesmo lado da curva, e
 * o corte cai sempre do mesmo lado da raiz. Uma das pontas NUNCA ANDA, para sempre, e o
 * intervalo deixa de encolher: ele so vai raspando de um lado.
 *
 * <p>A convergencia continua sendo garantida e passa a ser linear com uma constante
 * horrivel. Na exponencial deste repositorio ela gasta varias vezes o que a bisseccao
 * gasta, e a tabela da estagnacao mostra a ponta parada.
 *
 * <p>O conserto tem nome, Illinois, e e uma linha: quando uma ponta repete, divide-se o
 * valor dela por dois para puxar a reta. Ele nao esta aqui de proposito, porque o assunto
 * desta classe e o que acontece sem ele.
 */
public final class FalsaPosicao implements Metodo {

    @Override
    public String nome() {
        return "falsa posicao";
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

        double corte = esquerda;

        while (contador.iteracoes() < maximoDeIteracoes) {
            contador.iteracao();

            double anterior = corte;

            corte = (esquerda * naDireita - direita * naEsquerda) / (naDireita - naEsquerda);

            double noCorte = funcao.em(corte, contador);

            if (noCorte == 0 || Math.abs(corte - anterior) < tolerancia)
                return Resultado.achou(nome(), funcao, corte, contador);

            if (naEsquerda * noCorte < 0) {
                direita = corte;
                naDireita = noCorte;
            } else {
                esquerda = corte;
                naEsquerda = noCorte;
            }
        }

        return Resultado.falhou(nome(), funcao, corte, contador,
                "passou de " + maximoDeIteracoes + " iteracoes");
    }

    /** Quantas iteracoes seguidas cada ponta ficou parada, para a tabela da estagnacao. */
    public record Estagnacao(long maiorSeguidaNaEsquerda, long maiorSeguidaNaDireita,
            long iteracoes) {

        public double fracaoParada() {
            return iteracoes == 0 ? 0
                    : (double) Math.max(maiorSeguidaNaEsquerda, maiorSeguidaNaDireita) / iteracoes;
        }
    }

    /** Roda contando quantas iteracoes seguidas cada ponta ficou sem se mexer. */
    public Estagnacao medirEstagnacao(Funcao funcao, double tolerancia, int maximoDeIteracoes) {
        Contador contador = new Contador();

        double esquerda = funcao.de();
        double direita = funcao.ate();

        double naEsquerda = funcao.em(esquerda, contador);
        double naDireita = funcao.em(direita, contador);

        long paradaNaEsquerda = 0;
        long paradaNaDireita = 0;
        long maiorNaEsquerda = 0;
        long maiorNaDireita = 0;

        double corte = esquerda;

        while (contador.iteracoes() < maximoDeIteracoes) {
            contador.iteracao();

            double anterior = corte;

            corte = (esquerda * naDireita - direita * naEsquerda) / (naDireita - naEsquerda);

            double noCorte = funcao.em(corte, contador);

            if (noCorte == 0 || Math.abs(corte - anterior) < tolerancia) break;

            if (naEsquerda * noCorte < 0) {
                direita = corte;
                naDireita = noCorte;

                paradaNaDireita = 0;
                paradaNaEsquerda++;
            } else {
                esquerda = corte;
                naEsquerda = noCorte;

                paradaNaEsquerda = 0;
                paradaNaDireita++;
            }

            maiorNaEsquerda = Math.max(maiorNaEsquerda, paradaNaEsquerda);
            maiorNaDireita = Math.max(maiorNaDireita, paradaNaDireita);
        }

        return new Estagnacao(maiorNaEsquerda, maiorNaDireita, contador.iteracoes());
    }
}
