package br.com.conde.raiz;

import java.util.List;

/**
 * AS FUNCOES, que sao a metade que importa.
 *
 * <p>Nao existe "o metodo mais rapido". Existe o mais rapido NAQUELA funcao, e as funcoes
 * daqui nao foram escolhidas para serem justas: cada uma e o melhor caso de um metodo e o
 * pior de outro.
 *
 * <ul>
 *   <li>RAIZ DE DOIS: lisa, bem comportada, derivada longe de zero. O melhor caso de
 *       Newton e o caso em que todo mundo funciona.
 *   <li>ARCO TANGENTE: lisa tambem, e Newton diverge dela a partir de um ponto exato. E a
 *       funcao que mostra que "lisa" nao basta.
 *   <li>CUBICA DE WALLIS: a de 1685, a primeira em que o metodo de Newton foi escrito.
 *   <li>EXPONENCIAL MENOS CEM: muito convexa no intervalo. O caso contra a falsa posicao.
 *   <li>CUBO DESLOCADO: raiz tripla. O caso em que Newton deixa de ser quadratico.
 *   <li>DECIMA POTENCIA: raiz simples com derivada minuscula perto dela.
 * </ul>
 */
public final class Funcoes {

    private Funcoes() {}

    /** x ao quadrado menos dois, cuja raiz e a raiz de dois. */
    public static Funcao raizDeDois() {
        return new Funcao() {
            @Override
            public String nome() {
                return "x2 menos 2";
            }

            @Override
            public double em(double x) {
                return x * x - 2;
            }

            @Override
            public double derivada(double x) {
                return 2 * x;
            }

            @Override
            public double de() {
                return 1;
            }

            @Override
            public double ate() {
                return 2;
            }

            @Override
            public double raizExata() {
                return Math.sqrt(2);
            }
        };
    }

    /**
     * O arco tangente, cuja raiz e zero.
     *
     * <p>Ela e lisa, tem derivada em toda parte e a derivada nunca e zero. E Newton diverge
     * dela a partir de um ponto exato, que a tabela das falhas acha: a tangente no ponto x
     * cruza o eixo mais longe da raiz do que x estava, e a cada passo ele se afasta mais.
     */
    public static Funcao arcoTangente() {
        return new Funcao() {
            @Override
            public String nome() {
                return "arco tangente";
            }

            @Override
            public double em(double x) {
                return Math.atan(x);
            }

            @Override
            public double derivada(double x) {
                return 1 / (1 + x * x);
            }

            @Override
            public double de() {
                return -3;
            }

            @Override
            public double ate() {
                return 4;
            }

            @Override
            public double raizExata() {
                return 0;
            }
        };
    }

    /** A cubica de Wallis, de 1685, a primeira em que o metodo de Newton foi escrito. */
    public static Funcao cubicaDeWallis() {
        return new Funcao() {
            @Override
            public String nome() {
                return "cubica de Wallis";
            }

            @Override
            public double em(double x) {
                return x * x * x - 2 * x - 5;
            }

            @Override
            public double derivada(double x) {
                return 3 * x * x - 2;
            }

            @Override
            public double de() {
                return 2;
            }

            @Override
            public double ate() {
                return 3;
            }
        };
    }

    /**
     * A exponencial menos cem, cuja raiz e o logaritmo de cem.
     *
     * <p>Muito convexa no intervalo: a funcao vale quase nada numa ponta e milhares na
     * outra. E o caso contra a falsa posicao, porque a reta entre as duas pontas passa
     * rasante perto da ponta baixa e o lado alto nunca anda.
     */
    public static Funcao exponencialMenosCem() {
        return new Funcao() {
            @Override
            public String nome() {
                return "exp menos 100";
            }

            @Override
            public double em(double x) {
                return Math.exp(x) - 100;
            }

            @Override
            public double derivada(double x) {
                return Math.exp(x);
            }

            @Override
            public double de() {
                return 0;
            }

            @Override
            public double ate() {
                return 10;
            }

            @Override
            public double raizExata() {
                return Math.log(100);
            }
        };
    }

    /** O cubo de x menos um: raiz tripla em um, onde a derivada tambem zera. */
    public static Funcao cuboDeslocado() {
        return new Funcao() {
            @Override
            public String nome() {
                return "(x menos 1) ao cubo";
            }

            @Override
            public double em(double x) {
                double t = x - 1;

                return t * t * t;
            }

            @Override
            public double derivada(double x) {
                double t = x - 1;

                return 3 * t * t;
            }

            @Override
            public double de() {
                return 0;
            }

            @Override
            public double ate() {
                return 2.5;
            }

            @Override
            public double raizExata() {
                return 1;
            }

            @Override
            public int multiplicidade() {
                return 3;
            }
        };
    }

    /** x elevado a dez menos um: raiz simples, mas com derivada minuscula perto do zero. */
    public static Funcao decimaPotencia() {
        return new Funcao() {
            @Override
            public String nome() {
                return "x10 menos 1";
            }

            @Override
            public double em(double x) {
                return Math.pow(x, 10) - 1;
            }

            @Override
            public double derivada(double x) {
                return 10 * Math.pow(x, 9);
            }

            @Override
            public double de() {
                return 0.1;
            }

            @Override
            public double ate() {
                return 1.5;
            }

            @Override
            public double raizExata() {
                return 1;
            }
        };
    }

    /** As seis, na ordem em que as tabelas as mostram. */
    public static List<Funcao> todas() {
        return List.of(raizDeDois(), cubicaDeWallis(), arcoTangente(), exponencialMenosCem(),
                decimaPotencia(), cuboDeslocado());
    }
}
