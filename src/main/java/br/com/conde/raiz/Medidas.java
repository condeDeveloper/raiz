package br.com.conde.raiz;

import java.util.ArrayList;
import java.util.List;

/**
 * AS CINCO TABELAS que o medidor imprime e que os testes conferem numero por numero.
 *
 * <p>Tudo aqui e contado, nunca cronometrado: avaliacoes da funcao, avaliacoes da derivada
 * e iteracoes. Os mesmos numeros saem em qualquer sistema, porque ponto flutuante de
 * precisao dupla e definido pela norma e nao pela maquina.
 */
public final class Medidas {

    /** A tolerancia das tabelas: doze casas, que e o que se pede na pratica. */
    public static final double TOLERANCIA = 1e-12;

    public static final int MAXIMO_DE_ITERACOES = 200;

    private Medidas() {}

    // ───────────────────── 1. a correcao ─────────────────────

    public record LinhaCorrecao(String funcao, String metodo, boolean convergiu, double erro,
            double valorNaRaiz, boolean valido, String problema) {}

    /** Toda raiz devolvida e raiz, e quem mantem o cerco sempre converge. */
    public static List<LinhaCorrecao> aCorrecao() {
        List<LinhaCorrecao> saida = new ArrayList<>();

        for (Funcao funcao : Funcoes.todas())
            for (Metodo metodo : Juiz.todos()) {
                Juiz.Veredito veredito = Juiz.julgar(metodo, funcao, TOLERANCIA);

                Metodo.Resultado resultado =
                        metodo.achar(funcao, TOLERANCIA, MAXIMO_DE_ITERACOES);

                saida.add(new LinhaCorrecao(funcao.nome(), metodo.nome(), resultado.convergiu(),
                        veredito.erro(), veredito.valorNaRaiz(), veredito.valido(),
                        veredito.problema()));
            }

        return saida;
    }

    // ───────────────────── 2. a convergencia ─────────────────────

    public record LinhaConvergencia(String funcao, String metodo, boolean convergiu,
            long iteracoes, long avaliacoesDaFuncao, long avaliacoesDaDerivada,
            long avaliacoes, double erro) {}

    /** Quantas contas ate a precisao pedida, por funcao e por metodo. */
    public static List<LinhaConvergencia> aConvergencia() {
        List<LinhaConvergencia> saida = new ArrayList<>();

        for (Funcao funcao : Funcoes.todas()) {
            double certa = Juiz.referencia(funcao);

            for (Metodo metodo : Juiz.todos()) {
                Metodo.Resultado resultado =
                        metodo.achar(funcao, TOLERANCIA, MAXIMO_DE_ITERACOES);

                saida.add(new LinhaConvergencia(funcao.nome(), metodo.nome(),
                        resultado.convergiu(), resultado.iteracoes(),
                        resultado.avaliacoesDaFuncao(), resultado.avaliacoesDaDerivada(),
                        resultado.avaliacoes(),
                        resultado.convergiu() ? Math.abs(resultado.raiz() - certa)
                                : Double.NaN));
            }
        }

        return saida;
    }

    // ───────────────────── 3. as falhas ─────────────────────

    /** Quantos pontos de partida a varredura testa em cada funcao. */
    public static final int PONTOS_DE_PARTIDA = 2_001;

    public record LinhaFalhas(String funcao, int pontos, int convergiram, double fracao,
            double menorQueFalha, double maiorQueFalha) {}

    /**
     * DE ONDE NEWTON NAO VOLTA, varrendo o intervalo inteiro.
     *
     * <p>Mil e um pontos igualmente espacados entre as duas pontas, cada um usado como
     * palpite inicial. A coluna conta de quantos ele chega na raiz.
     *
     * <p>As duas ultimas colunas acham a fronteira: o menor e o maior ponto a partir do
     * qual ele falha. No arco tangente essa fronteira e exata e tem nome na literatura.
     */
    public static List<LinhaFalhas> asFalhas() {
        List<LinhaFalhas> saida = new ArrayList<>();

        Newton newton = new Newton();

        for (Funcao funcao : Funcoes.todas()) {
            double certa = Juiz.referencia(funcao);

            int convergiram = 0;

            double menorQueFalha = Double.NaN;
            double maiorQueFalha = Double.NaN;

            for (int i = 0; i < PONTOS_DE_PARTIDA; i++) {
                double palpite = funcao.de()
                        + (funcao.ate() - funcao.de()) * i / (PONTOS_DE_PARTIDA - 1.0);

                Metodo.Resultado resultado =
                        newton.achar(funcao, palpite, TOLERANCIA, MAXIMO_DE_ITERACOES);

                boolean chegou = resultado.convergiu()
                        && Math.abs(resultado.raiz() - certa) < 1e-6;

                if (chegou) {
                    convergiram++;
                    continue;
                }

                if (Double.isNaN(menorQueFalha)) menorQueFalha = palpite;

                maiorQueFalha = palpite;
            }

            saida.add(new LinhaFalhas(funcao.nome(), PONTOS_DE_PARTIDA, convergiram,
                    (double) convergiram / PONTOS_DE_PARTIDA, menorQueFalha, maiorQueFalha));
        }

        return saida;
    }

    /**
     * A FRONTEIRA EXATA DO ARCO TANGENTE, achada por bisseccao sobre o palpite.
     *
     * <p>Ela e a solucao de uma equacao que tambem precisa de um metodo numerico para ser
     * resolvida, o que e uma piada que vale contar: para achar de onde Newton falha,
     * usa-se a bisseccao.
     */
    public static double fronteiraDoArcoTangente() {
        Funcao funcao = Funcoes.arcoTangente();

        Newton newton = new Newton();

        double volta = 1.0;
        double naoVolta = 2.0;

        for (int i = 0; i < 200; i++) {
            double meio = volta + (naoVolta - volta) / 2;

            Metodo.Resultado resultado =
                    newton.achar(funcao, meio, TOLERANCIA, MAXIMO_DE_ITERACOES);

            boolean chegou = resultado.convergiu() && Math.abs(resultado.raiz()) < 1e-6;

            if (chegou) volta = meio;
            else naoVolta = meio;
        }

        return volta;
    }

    // ───────────────────── 4. a estagnacao ─────────────────────

    public record LinhaEstagnacao(String funcao, long iteracoesDaFalsa, long iteracoesDaBisseccao,
            long maiorSeguidaNaEsquerda, long maiorSeguidaNaDireita, double fracaoParada,
            double vezesABisseccao) {}

    /** A falsa posicao e o lado que nunca anda. */
    public static List<LinhaEstagnacao> aEstagnacao() {
        List<LinhaEstagnacao> saida = new ArrayList<>();

        FalsaPosicao falsa = new FalsaPosicao();
        Bisseccao bisseccao = new Bisseccao();

        for (Funcao funcao : Funcoes.todas()) {
            FalsaPosicao.Estagnacao estagnacao =
                    falsa.medirEstagnacao(funcao, TOLERANCIA, MAXIMO_DE_ITERACOES);

            long daBisseccao = bisseccao.achar(funcao, TOLERANCIA, MAXIMO_DE_ITERACOES)
                    .iteracoes();

            saida.add(new LinhaEstagnacao(funcao.nome(), estagnacao.iteracoes(), daBisseccao,
                    estagnacao.maiorSeguidaNaEsquerda(), estagnacao.maiorSeguidaNaDireita(),
                    estagnacao.fracaoParada(),
                    (double) estagnacao.iteracoes() / daBisseccao));
        }

        return saida;
    }

    // ───────────────────── 5. a raiz multipla ─────────────────────

    public record LinhaMultipla(String metodo, boolean convergiu, long iteracoes,
            long avaliacoes, double erro, double razaoDoErro) {}

    /**
     * A RAIZ MULTIPLA DERRUBA A ORDEM DE NEWTON.
     *
     * <p>A coluna da razao do erro e a que conta: ela e o erro de um passo dividido pelo do
     * passo anterior, perto do fim. Com convergencia quadratica ela despenca para zero; com
     * convergencia linear ela fica numa constante.
     *
     * <p>Numa raiz de multiplicidade m, Newton passa a ganhar o fator (m-1)/m por passo.
     * Com m igual a tres isso da dois tercos, e dois tercos por passo e pior que a metade
     * por passo da bisseccao.
     */
    public static List<LinhaMultipla> aMultipla() {
        List<LinhaMultipla> saida = new ArrayList<>();

        Funcao funcao = Funcoes.cuboDeslocado();

        double certa = Juiz.referencia(funcao);

        for (Metodo metodo : Juiz.todos()) {
            Metodo.Resultado resultado = metodo.achar(funcao, TOLERANCIA, MAXIMO_DE_ITERACOES);

            saida.add(new LinhaMultipla(metodo.nome(), resultado.convergiu(),
                    resultado.iteracoes(), resultado.avaliacoes(),
                    resultado.convergiu() ? Math.abs(resultado.raiz() - certa) : Double.NaN,
                    razaoDoErro(metodo, funcao, certa)));
        }

        return saida;
    }

    /**
     * Quanto o erro encolhe por passo, pela media geometrica dos ultimos trinta.
     *
     * <p>A primeira versao desta conta usava a razao de UM passo so, e ela nao dizia nada:
     * o erro da bisseccao oscila, porque o meio do intervalo as vezes cai quase em cima da
     * raiz e as vezes nao, e a razao de um passo saia 0,17 onde a resposta certa e 0,5.
     *
     * <p>A media geometrica e a media certa aqui e nao so a conveniente: o erro depois de k
     * passos e o produto das razoes, entao a razao tipica e a raiz k-esima do produto, que
     * e exatamente a media geometrica. Media aritmetica de razoes nao quer dizer nada.
     */
    private static double razaoDoErro(Metodo metodo, Funcao funcao, double certa) {
        double somaDosLogaritmos = 0;
        int quantas = 0;

        double anterior = Double.NaN;

        for (int iteracoes = 10; iteracoes <= 40; iteracoes++) {
            Metodo.Resultado resultado = metodo.achar(funcao, 0, iteracoes);

            double erro = Math.abs(resultado.raiz() - certa);

            if (!Double.isNaN(anterior) && anterior > 0 && erro > 0 && erro < anterior * 1e6) {
                somaDosLogaritmos += Math.log(erro / anterior);
                quantas++;
            }

            anterior = erro;
        }

        return quantas == 0 ? Double.NaN : Math.exp(somaDosLogaritmos / quantas);
    }
}
