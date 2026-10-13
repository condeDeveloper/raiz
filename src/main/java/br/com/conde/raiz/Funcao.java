package br.com.conde.raiz;

/**
 * UMA FUNCAO COM UMA RAIZ CONHECIDA NUM INTERVALO QUE A CERCA.
 *
 * <p>Toda funcao daqui vem com tres coisas, e a terceira e o que faz o juiz existir: um
 * intervalo em que ela troca de sinal. Pelo teorema do valor intermediario, uma funcao
 * continua que vale menos de zero numa ponta e mais de zero na outra tem raiz no meio.
 *
 * <p>Isso nao e um detalhe de montagem: e a diferenca entre os metodos deste repositorio.
 * Os que mantem o cerco tem convergencia GARANTIDA, por teorema, sem supor nada sobre a
 * funcao alem da continuidade. Os que largam o cerco sao mais rapidos quando dao certo e
 * nao tem garantia nenhuma.
 *
 * <p>A derivada esta aqui porque Newton precisa dela. Numa funcao de verdade ela costuma
 * nao estar disponivel, e isso e uma desvantagem dele que nenhuma tabela de velocidade
 * mostra.
 */
public interface Funcao {

    String nome();

    double em(double x);

    double derivada(double x);

    /** Uma ponta do intervalo em que a funcao troca de sinal. */
    double de();

    double ate();

    /**
     * A raiz, quando ela tem forma fechada que da para escrever.
     *
     * <p>Devolve NaN quando nao tem, e ai o juiz usa a bisseccao levada ate o fim, que e
     * garantida por teorema e nao depende de nenhuma constante decorada.
     */
    default double raizExata() {
        return Double.NaN;
    }

    /** A raiz tem multiplicidade maior que um: a funcao toca o eixo sem atravessar. */
    default int multiplicidade() {
        return 1;
    }

    default double em(double x, Contador contador) {
        contador.avaliouAFuncao();

        return em(x);
    }

    default double derivada(double x, Contador contador) {
        contador.avaliouADerivada();

        return derivada(x);
    }

    /** A funcao troca de sinal entre as duas pontas, que e o que o cerco exige. */
    default boolean cerca() {
        return em(de()) * em(ate()) < 0;
    }
}
