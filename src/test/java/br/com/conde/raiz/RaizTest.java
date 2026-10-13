package br.com.conde.raiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** As pecas, uma a uma, antes de qualquer medida. */
class RaizTest {

    // ───────────────────── as funcoes ─────────────────────

    @Nested
    @DisplayName("as funcoes")
    class AsFuncoes {

        @Test
        @DisplayName("toda funcao troca de sinal entre as duas pontas do intervalo")
        void todasCercam() {
            for (Funcao funcao : Funcoes.todas())
                assertThat(funcao.cerca())
                        .as(funcao.nome() + ": f(" + funcao.de() + ")=" + funcao.em(funcao.de())
                                + " e f(" + funcao.ate() + ")=" + funcao.em(funcao.ate()))
                        .isTrue();
        }

        @Test
        @DisplayName("onde a raiz tem forma fechada, a funcao vale zero nela")
        void aRaizFechadaEhRaiz() {
            int conferidas = 0;

            for (Funcao funcao : Funcoes.todas()) {
                if (Double.isNaN(funcao.raizExata())) continue;

                assertThat(funcao.em(funcao.raizExata()))
                        .as(funcao.nome())
                        .isCloseTo(0.0, within(1e-12));

                conferidas++;
            }

            assertThat(conferidas).as("cinco das seis tem forma fechada").isEqualTo(5);
        }

        /**
         * O JUIZ BATE COM A FORMA FECHADA ONDE AS DUAS EXISTEM.
         *
         * <p>Esse e o teste que autoriza usar a bisseccao como referencia onde a forma
         * fechada nao existe. Sem ele, a referencia seria so outra implementacao minha.
         */
        @Test
        @DisplayName("a bisseccao de referencia bate com a raiz de forma fechada")
        void oJuizBateComAFormaFechada() {
            for (Funcao funcao : Funcoes.todas()) {
                if (Double.isNaN(funcao.raizExata())) continue;

                assertThat(Juiz.referencia(funcao))
                        .as(funcao.nome())
                        .isCloseTo(funcao.raizExata(), within(1e-9));
            }
        }

        @Test
        @DisplayName("a derivada bate com a diferenca finita, em toda funcao")
        void aDerivadaEhADerivada() {
            double passo = 1e-6;

            for (Funcao funcao : Funcoes.todas())
                for (int i = 1; i < 10; i++) {
                    double x = funcao.de() + (funcao.ate() - funcao.de()) * i / 10.0;

                    double aproximada = (funcao.em(x + passo) - funcao.em(x - passo)) / (2 * passo);

                    assertThat(funcao.derivada(x))
                            .as(funcao.nome() + " em " + x)
                            .isCloseTo(aproximada, within(1e-4 * Math.max(1, Math.abs(aproximada))));
                }
        }

        @Test
        @DisplayName("so o cubo deslocado tem raiz multipla, e e tripla")
        void soUmaTemRaizMultipla() {
            assertThat(Funcoes.todas()).filteredOn(f -> f.multiplicidade() > 1)
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.multiplicidade()).isEqualTo(3);
                        assertThat(f.derivada(f.raizExata()))
                                .as("numa raiz multipla a derivada tambem zera")
                                .isZero();
                    });
        }
    }

    // ───────────────────── a bisseccao ─────────────────────

    @Nested
    @DisplayName("a bisseccao")
    class ABisseccao {

        /**
         * O ORCAMENTO DELA E SABIDO ANTES DE COMECAR, e nenhuma outra pode dizer isso.
         */
        @Test
        @DisplayName("ela nunca passa do numero de passos previsto pelo logaritmo")
        void elaNuncaPassaDoPrevisto() {
            for (Funcao funcao : Funcoes.todas()) {
                int previsto = Juiz.orcamentoDaBisseccao(funcao, Medidas.TOLERANCIA);

                long gastou = new Bisseccao()
                        .achar(funcao, Medidas.TOLERANCIA, Medidas.MAXIMO_DE_ITERACOES)
                        .iteracoes();

                assertThat(gastou)
                        .as(funcao.nome() + ": previsto " + previsto)
                        .isLessThanOrEqualTo(previsto + 1);
            }
        }

        @Test
        @DisplayName("o previsto e o logaritmo na base dois da largura sobre a tolerancia")
        void oPrevistoEhOLogaritmo() {
            assertThat(Bisseccao.passosPrevistos(1, 0.5)).isEqualTo(1);
            assertThat(Bisseccao.passosPrevistos(1, 0.25)).isEqualTo(2);
            assertThat(Bisseccao.passosPrevistos(1, 1e-12)).isEqualTo(40);
        }

        @Test
        @DisplayName("sem troca de sinal nas pontas ela recusa, em vez de chutar")
        void semCercoElaRecusa() {
            Funcao semRaiz = new Funcao() {
                @Override
                public String nome() {
                    return "x2 mais 1";
                }

                @Override
                public double em(double x) {
                    return x * x + 1;
                }

                @Override
                public double derivada(double x) {
                    return 2 * x;
                }

                @Override
                public double de() {
                    return -1;
                }

                @Override
                public double ate() {
                    return 1;
                }
            };

            Metodo.Resultado resultado = new Bisseccao().achar(semRaiz, 1e-12, 100);

            assertThat(resultado.convergiu()).isFalse();
            assertThat(resultado.problema()).contains("nao ha cerco");
        }

        @Test
        @DisplayName("ela acha a raiz de dois com doze casas certas")
        void elaAchaARaizDeDois() {
            assertThat(new Bisseccao().achar(Funcoes.raizDeDois(), 1e-12, 100).raiz())
                    .isCloseTo(Math.sqrt(2), within(1e-11));
        }
    }

    // ───────────────────── os metodos ─────────────────────

    @Nested
    @DisplayName("os metodos")
    class OsMetodos {

        @Test
        @DisplayName("quem diz que convergiu esta na raiz, nas 30 combinacoes")
        void quemConvergiuEstaNaRaiz() {
            for (Funcao funcao : Funcoes.todas())
                for (Metodo metodo : Juiz.todos()) {
                    Juiz.Veredito veredito = Juiz.julgar(metodo, funcao, Medidas.TOLERANCIA);

                    assertThat(veredito.valido())
                            .as(funcao.nome() + " / " + metodo.nome() + ": " + veredito.problema())
                            .isTrue();
                }
        }

        /**
         * QUEM MANTEM O CERCO NUNCA SE PERDE, mesmo sem convergir.
         *
         * <p>Esta e a garantia de verdade, e ela vale mesmo onde a convergencia nao cabe no
         * orcamento. A falsa posicao na raiz tripla nao converge e continua dentro.
         */
        @Test
        @DisplayName("quem mantem o cerco devolve um ponto dentro do intervalo, convergindo ou nao")
        void quemMantemOCercoNaoSePerde() {
            for (Funcao funcao : Funcoes.todas())
                for (Metodo metodo : Juiz.todos()) {
                    if (!metodo.mantemOCerco()) continue;

                    double raiz = metodo.achar(funcao, Medidas.TOLERANCIA, 30).raiz();

                    assertThat(raiz)
                            .as(funcao.nome() + " / " + metodo.nome())
                            .isBetween(funcao.de(), funcao.ate());
                }
        }

        @Test
        @DisplayName("so Newton precisa da derivada, e so ele a avalia")
        void soNewtonUsaADerivada() {
            assertThat(Juiz.todos()).filteredOn(Metodo::precisaDaDerivada)
                    .extracting(Metodo::nome)
                    .containsExactly("Newton");

            for (Metodo metodo : Juiz.todos()) {
                long daDerivada = metodo.achar(Funcoes.raizDeDois(), Medidas.TOLERANCIA, 100)
                        .avaliacoesDaDerivada();

                if (metodo.precisaDaDerivada()) assertThat(daDerivada).as(metodo.nome()).isPositive();
                else assertThat(daDerivada).as(metodo.nome()).isZero();
            }
        }

        @Test
        @DisplayName("tres dos cinco mantem o cerco, e os dois que nao mantem sao os rapidos")
        void quemMantemOCerco() {
            assertThat(Juiz.todos()).filteredOn(Metodo::mantemOCerco)
                    .extracting(Metodo::nome)
                    .containsExactlyInAnyOrder("bisseccao", "misto", "falsa posicao");

            assertThat(Juiz.todos()).filteredOn(m -> !m.mantemOCerco())
                    .extracting(Metodo::nome)
                    .containsExactlyInAnyOrder("secante", "Newton");
        }

        /**
         * A PARADA SO PELO PASSO MENTE, e esta e a prova no menor caso.
         *
         * <p>Na exponencial a funcao e quase plana perto de zero: os passos da secante ficam
         * minusculos a quatro unidades da raiz, e ela anuncia que chegou.
         */
        @Test
        @DisplayName("parando so pelo passo, a secante diz que achou a raiz a 4,5 de distancia")
        void aParadaSoPeloPassoMente() {
            Funcao funcao = Funcoes.exponencialMenosCem();

            Metodo.Resultado mentirosa = new Secante(false)
                    .achar(funcao, Medidas.TOLERANCIA, Medidas.MAXIMO_DE_ITERACOES);

            assertThat(mentirosa.convergiu()).as("ela anuncia que convergiu").isTrue();

            assertThat(Math.abs(mentirosa.raiz() - Juiz.referencia(funcao)))
                    .as("e esta a mais de quatro unidades da raiz")
                    .isGreaterThan(4.0);

            Metodo.Resultado honesta = new Secante()
                    .achar(funcao, Medidas.TOLERANCIA, Medidas.MAXIMO_DE_ITERACOES);

            assertThat(honesta.convergiu())
                    .as("exigindo tambem o residuo, ela admite que nao achou")
                    .isFalse();
        }
    }

    // ───────────────────── o misto ─────────────────────

    @Nested
    @DisplayName("o misto")
    class OMisto {

        /**
         * A SALVAGUARDA QUE DE FATO DISPARA.
         *
         * <p>A primeira versao desta classe exigia que o atalho caisse a menos de meia
         * largura do meio, o que todo ponto dentro do cerco satisfaz: ela nunca disparava e
         * o metodo travava nas mesmas tres funcoes que a falsa posicao.
         */
        @Test
        @DisplayName("o misto converge nas tres funcoes em que a falsa posicao trava")
        void oMistoNaoTravaOndeAFalsaTrava() {
            for (Funcao funcao : List.of(Funcoes.exponencialMenosCem(), Funcoes.decimaPotencia(),
                    Funcoes.cuboDeslocado())) {

                assertThat(new FalsaPosicao()
                        .achar(funcao, Medidas.TOLERANCIA, Medidas.MAXIMO_DE_ITERACOES)
                        .convergiu())
                        .as(funcao.nome() + ": a falsa posicao trava")
                        .isFalse();

                assertThat(new Misto()
                        .achar(funcao, Medidas.TOLERANCIA, Medidas.MAXIMO_DE_ITERACOES)
                        .convergiu())
                        .as(funcao.nome() + ": e o misto nao")
                        .isTrue();
            }
        }

        @Test
        @DisplayName("o misto nunca gasta mais que o triplo do orcamento da bisseccao")
        void oMistoTemOrcamento() {
            for (Funcao funcao : Funcoes.todas()) {
                int daBisseccao = Juiz.orcamentoDaBisseccao(funcao, Medidas.TOLERANCIA);

                assertThat(new Misto()
                        .achar(funcao, Medidas.TOLERANCIA, Medidas.MAXIMO_DE_ITERACOES)
                        .iteracoes())
                        .as(funcao.nome() + ": o cerco encolhe pela metade a cada tres")
                        .isLessThanOrEqualTo(3L * daBisseccao + 3);
            }
        }
    }

    // ───────────────────── o contador ─────────────────────

    @Nested
    @DisplayName("o contador")
    class OContador {

        @Test
        @DisplayName("ele separa as avaliacoes da funcao das da derivada")
        void eleSepara() {
            Contador contador = new Contador();

            contador.avaliouAFuncao();
            contador.avaliouAFuncao();
            contador.avaliouADerivada();
            contador.iteracao();

            assertThat(contador.avaliacoesDaFuncao()).isEqualTo(2);
            assertThat(contador.avaliacoesDaDerivada()).isEqualTo(1);
            assertThat(contador.avaliacoes()).isEqualTo(3);
            assertThat(contador.avaliacoesPorIteracao()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("cada iteracao de Newton custa duas avaliacoes e a da secante custa uma")
        void oCustoPorIteracao() {
            Funcao funcao = Funcoes.raizDeDois();

            Metodo.Resultado deNewton = new Newton().achar(funcao, Medidas.TOLERANCIA, 100);
            Metodo.Resultado daSecante = new Secante().achar(funcao, Medidas.TOLERANCIA, 100);

            assertThat((double) deNewton.avaliacoes() / deNewton.iteracoes())
                    .as("funcao e derivada por passo")
                    .isGreaterThan(2.0);

            assertThat((double) daSecante.avaliacoes() / daSecante.iteracoes())
                    .as("so a funcao por passo")
                    .isLessThan(1.5);
        }
    }
}
