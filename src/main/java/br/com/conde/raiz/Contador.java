package br.com.conde.raiz;

/**
 * O CONTADOR: avaliacoes da funcao. Nunca segundos.
 *
 * <p>A AVALIACAO e a medida certa e nao so a conveniente, e escolher entre ela e a ITERACAO
 * e metade deste repositorio. Num problema de verdade a funcao e uma simulacao, um sistema
 * linear ou uma consulta cara, e tudo o que o metodo faz em volta dela some perto disso.
 *
 * <p>Contar iteracoes favoreceria Newton de graca: cada iteracao dele usa a funcao E a
 * derivada, entao uma iteracao dele custa duas avaliacoes onde a da secante custa uma. A
 * tabela da convergencia tem as duas colunas lado a lado de proposito.
 *
 * <p>E a derivada tem coluna separada porque nem sempre ela existe. Quando a funcao vem de
 * uma simulacao, a derivada nao esta disponivel e aproxima-la custa mais uma avaliacao, o
 * que transforma Newton numa secante mais cara.
 *
 * <p>Segundos nao serviriam: a integracao continua roda em tres sistemas, e as funcoes
 * daqui custam nanossegundos. Um cronometro mediria o laco do Java.
 */
public final class Contador {

    private long avaliacoesDaFuncao;
    private long avaliacoesDaDerivada;
    private long iteracoes;

    public void avaliouAFuncao() {
        avaliacoesDaFuncao++;
    }

    public void avaliouADerivada() {
        avaliacoesDaDerivada++;
    }

    public void iteracao() {
        iteracoes++;
    }

    public long avaliacoesDaFuncao() {
        return avaliacoesDaFuncao;
    }

    public long avaliacoesDaDerivada() {
        return avaliacoesDaDerivada;
    }

    /** O que custa de verdade: toda avaliacao, da funcao ou da derivada. */
    public long avaliacoes() {
        return avaliacoesDaFuncao + avaliacoesDaDerivada;
    }

    public long iteracoes() {
        return iteracoes;
    }

    public double avaliacoesPorIteracao() {
        return iteracoes == 0 ? 0 : (double) avaliacoes() / iteracoes;
    }
}
