package br.com.conde.raiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Os numeros do README, afirmados.
 *
 * <p>Um README e texto: ele nao quebra quando deixa de ser verdade. Cada afirmacao das
 * tabelas esta repetida aqui para quebrar.
 */
class MedidasTest {

    private static final List<Medidas.LinhaConvergencia> CONVERGENCIA = Medidas.aConvergencia();
    private static final List<Medidas.LinhaFalhas> FALHAS = Medidas.asFalhas();
    private static final List<Medidas.LinhaEstagnacao> ESTAGNACAO = Medidas.aEstagnacao();
    private static final List<Medidas.LinhaMultipla> MULTIPLA = Medidas.aMultipla();

    // ───────────────────── 1. a correcao ─────────────────────

    @Test
    @DisplayName("nas 30 combinacoes, ninguem diz que achou a raiz sem ter achado")
    void ninguemMente() {
        List<Medidas.LinhaCorrecao> linhas = Medidas.aCorrecao();

        assertThat(linhas).hasSize(30);

        assertThat(linhas).allSatisfy(l ->
                assertThat(l.valido())
                        .as(l.funcao() + " / " + l.metodo() + ": " + l.problema())
                        .isTrue());

        assertThat(linhas).filteredOn(l -> l.metodo().equals("bisseccao"))
                .as("a bisseccao converge nas seis, sempre")
                .hasSize(6)
                .allSatisfy(l -> assertThat(l.convergiu()).as(l.funcao()).isTrue());

        assertThat(linhas).filteredOn(l -> l.metodo().equals("misto"))
                .as("e o misto tambem")
                .allSatisfy(l -> assertThat(l.convergiu()).as(l.funcao()).isTrue());
    }

    // ───────────────────── 2. a convergencia ─────────────────────

    /**
     * A CORRECAO PRINCIPAL: contando o que custa, a secante ganha de Newton.
     *
     * <p>Em iteracoes Newton ganha, como todo mundo diz: 5 contra 7 na raiz de dois. Em
     * AVALIACOES, que e o que uma funcao cara cobra, ele perde: 11 contra 9, porque cada
     * passo dele gasta a funcao e a derivada.
     */
    @Test
    @DisplayName("Newton usa menos iteracoes e mais avaliacoes que a secante: 5 x 7 e 11 x 9")
    void aSecanteGanhaEmAvaliacoes() {
        assertThat(convergencia("x2 menos 2", "Newton").iteracoes()).isEqualTo(5);
        assertThat(convergencia("x2 menos 2", "secante").iteracoes()).isEqualTo(7);

        assertThat(convergencia("x2 menos 2", "Newton").avaliacoes()).isEqualTo(11);
        assertThat(convergencia("x2 menos 2", "secante").avaliacoes()).isEqualTo(9);

        assertThat(convergencia("cubica de Wallis", "Newton").avaliacoes()).isEqualTo(13);
        assertThat(convergencia("cubica de Wallis", "secante").avaliacoes()).isEqualTo(9);

        assertThat(convergencia("x2 menos 2", "Newton").avaliacoesDaDerivada())
                .as("e a derivada e metade do custo dele")
                .isEqualTo(5);

        assertThat(convergencia("x2 menos 2", "secante").avaliacoesDaDerivada())
                .as("enquanto a secante nao precisa dela")
                .isZero();
    }

    @Test
    @DisplayName("a bisseccao gasta de 42 a 46 avaliacoes, e isso nao depende da funcao")
    void aBisseccaoEhUmaReta() {
        for (Funcao funcao : Funcoes.todas()) {
            Medidas.LinhaConvergencia linha = convergencia(funcao.nome(), "bisseccao");

            assertThat(linha.convergiu()).as(funcao.nome()).isTrue();
            assertThat(linha.avaliacoes()).as(funcao.nome()).isBetween(42L, 46L);
        }
    }

    @Test
    @DisplayName("o misto converge nas seis e e sempre mais barato que a bisseccao, menos na tripla")
    void oMistoGanhaDaBisseccao() {
        for (Funcao funcao : Funcoes.todas())
            assertThat(convergencia(funcao.nome(), "misto").convergiu())
                    .as(funcao.nome())
                    .isTrue();

        for (String funcao : List.of("x2 menos 2", "cubica de Wallis", "arco tangente",
                "exp menos 100", "x10 menos 1"))
            assertThat(convergencia(funcao, "misto").avaliacoes())
                    .as(funcao)
                    .isLessThan(convergencia(funcao, "bisseccao").avaliacoes());

        assertThat(convergencia("(x menos 1) ao cubo", "misto").avaliacoes())
                .as("na raiz tripla ele fica atras: 91 contra 44")
                .isEqualTo(91);
    }

    private static Medidas.LinhaConvergencia convergencia(String funcao, String metodo) {
        return CONVERGENCIA.stream()
                .filter(l -> l.funcao().equals(funcao) && l.metodo().equals(metodo))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 3. as falhas ─────────────────────

    /**
     * NEWTON VOLTA DE 39,73% DO INTERVALO NO ARCO TANGENTE, e de 100% nas outras cinco.
     *
     * <p>A funcao e lisa, tem derivada em toda parte, e a derivada nunca zera. Nada nela
     * avisa, e Newton diverge de quase dois tercos dos pontos de partida.
     */
    @Test
    @DisplayName("no arco tangente Newton volta de 795 dos 2.001 pontos, e das outras volta de todos")
    void newtonFalhaNoArcoTangente() {
        assertThat(falhas("arco tangente").convergiram()).isEqualTo(795);
        assertThat(falhas("arco tangente").fracao()).isCloseTo(0.3973, within(0.0001));

        for (String funcao : List.of("x2 menos 2", "cubica de Wallis", "exp menos 100",
                "x10 menos 1", "(x menos 1) ao cubo"))
            assertThat(falhas(funcao).fracao()).as(funcao).isEqualTo(1.0);
    }

    /**
     * E A FRONTEIRA E EXATA, achada por bisseccao sobre o palpite inicial.
     *
     * <p>Ela e a raiz de uma equacao que tambem nao tem forma fechada, entao achar de onde
     * Newton falha exige um metodo numerico. Foi a bisseccao.
     */
    @Test
    @DisplayName("a fronteira do arco tangente e 1,3917452003, achada por bisseccao")
    void aFronteiraEhExata() {
        double fronteira = Medidas.fronteiraDoArcoTangente();

        assertThat(fronteira).isCloseTo(1.3917452003, within(1e-9));

        Newton newton = new Newton();

        assertThat(newton.achar(Funcoes.arcoTangente(), fronteira * 0.999,
                Medidas.TOLERANCIA, Medidas.MAXIMO_DE_ITERACOES).convergiu())
                .as("um tiquinho antes dela ele volta")
                .isTrue();

        assertThat(newton.achar(Funcoes.arcoTangente(), fronteira * 1.001,
                Medidas.TOLERANCIA, Medidas.MAXIMO_DE_ITERACOES).convergiu())
                .as("e um tiquinho depois ele nao volta mais")
                .isFalse();
    }

    private static Medidas.LinhaFalhas falhas(String funcao) {
        return FALHAS.stream()
                .filter(l -> l.funcao().equals(funcao))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 4. a estagnacao ─────────────────────

    /**
     * A FALSA POSICAO NAO E SEMPRE PIOR QUE A BISSECCAO. E as vezes muito pior.
     *
     * <p>Na raiz de dois ela gasta 0,41 do que a bisseccao gasta. Na exponencial ela gasta
     * 4,44 vezes e nem assim converge. A diferenca e a curvatura: com curvatura de um sinal
     * so, uma ponta nunca anda.
     */
    @Test
    @DisplayName("a falsa posicao e 0,41 vezes a bisseccao numa funcao e 4,44 vezes em outra")
    void aFalsaPosicaoDependeDaCurvatura() {
        assertThat(estagnacao("x2 menos 2").vezesABisseccao()).isCloseTo(0.41, within(0.01));
        assertThat(estagnacao("arco tangente").vezesABisseccao()).isCloseTo(0.14, within(0.01));

        assertThat(estagnacao("exp menos 100").vezesABisseccao()).isCloseTo(4.44, within(0.01));
        assertThat(estagnacao("x10 menos 1").vezesABisseccao()).isCloseTo(4.76, within(0.01));
    }

    @Test
    @DisplayName("na exponencial a ponta direita fica parada nas 200 iteracoes, sem excecao")
    void aPontaQueNuncaAnda() {
        for (String funcao : List.of("exp menos 100", "x10 menos 1", "(x menos 1) ao cubo")) {
            assertThat(estagnacao(funcao).maiorSeguidaNaDireita()).as(funcao).isEqualTo(200);
            assertThat(estagnacao(funcao).fracaoParada()).as(funcao).isEqualTo(1.0);
            assertThat(estagnacao(funcao).maiorSeguidaNaEsquerda()).as(funcao).isZero();
        }

        assertThat(estagnacao("arco tangente").fracaoParada())
                .as("e no arco tangente, que muda de curvatura na raiz, ela nao para")
                .isLessThan(0.2);
    }

    private static Medidas.LinhaEstagnacao estagnacao(String funcao) {
        return ESTAGNACAO.stream()
                .filter(l -> l.funcao().equals(funcao))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 5. a raiz multipla ─────────────────────

    /**
     * NA RAIZ TRIPLA NEWTON PERDE DA BISSECCAO, e a razao do erro diz exatamente por que.
     *
     * <p>Ele ganha o fator (m-1)/m por passo numa raiz de multiplicidade m. Com m igual a
     * tres isso e dois tercos, e dois tercos por passo e pior que a metade por passo.
     */
    @Test
    @DisplayName("na raiz tripla a razao do erro de Newton e 0,6667, que e dois tercos exatos")
    void aRaizTriplaDerrubaAOrdem() {
        assertThat(multipla("Newton").razaoDoErro())
                .as("dois tercos, que e (m-1)/m com m igual a tres")
                .isCloseTo(2.0 / 3.0, within(0.001));

        assertThat(multipla("bisseccao").razaoDoErro())
                .as("e a bisseccao continua sendo exatamente metade por passo")
                .isCloseTo(0.5, within(0.001));

        assertThat(multipla("Newton").razaoDoErro())
                .as("dois tercos por passo e pior que metade por passo")
                .isGreaterThan(multipla("bisseccao").razaoDoErro());
    }

    @Test
    @DisplayName("e por isso a bisseccao gasta 44 avaliacoes onde Newton gasta 129")
    void aBisseccaoGanhaNaRaizTripla() {
        assertThat(multipla("bisseccao").avaliacoes()).isEqualTo(44);
        assertThat(multipla("Newton").avaliacoes()).isEqualTo(129);
        assertThat(multipla("secante").avaliacoes()).isEqualTo(95);
        assertThat(multipla("misto").avaliacoes()).isEqualTo(91);

        assertThat(multipla("bisseccao").avaliacoes())
                .as("a mais boba de todas ganha das quatro, nesta funcao")
                .isLessThan(multipla("misto").avaliacoes());
    }

    @Test
    @DisplayName("a falsa posicao converge como um sobre a raiz de n, e por isso nao converge")
    void aFalsaPosicaoNaoChega() {
        assertThat(multipla("falsa posicao").convergiu()).isFalse();

        assertThat(multipla("falsa posicao").razaoDoErro())
                .as("ela tira menos de dois por cento do erro por passo")
                .isGreaterThan(0.97);
    }

    private static Medidas.LinhaMultipla multipla(String metodo) {
        return MULTIPLA.stream()
                .filter(l -> l.metodo().equals(metodo))
                .findFirst()
                .orElseThrow();
    }
}
