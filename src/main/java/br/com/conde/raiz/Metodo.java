package br.com.conde.raiz;

/**
 * UM METODO DE BUSCA DE RAIZ.
 *
 * <p>Todos recebem a mesma coisa: a funcao, um intervalo que cerca a raiz e a tolerancia.
 * Dar o mesmo intervalo a todos nao e generosidade com os que nao precisam dele: e o que
 * torna a comparacao uma comparacao. Newton usa so o meio do intervalo como palpite
 * inicial, e e assim que ele e usado na vida.
 *
 * <p>O que muda entre eles e se o cerco e MANTIDO. Quem mantem tem convergencia garantida
 * por teorema, sem supor nada alem de continuidade; quem larga anda mais rapido quando da
 * certo e pode nao voltar.
 *
 * <p>Nenhum deles devolve "falhou" como excecao. Devolver um resultado que diz que NAO
 * convergiu e proposital: a tabela das falhas e sobre com que frequencia isso acontece, e
 * uma excecao transformaria uma medida numa interrupcao.
 */
public interface Metodo {

    String nome();

    /** O metodo mantem o cerco em toda iteracao, e por isso tem garantia. */
    boolean mantemOCerco();

    /** O metodo precisa da derivada da funcao. */
    boolean precisaDaDerivada();

    Resultado achar(Funcao funcao, double tolerancia, int maximoDeIteracoes);

    /** O que um metodo devolve: a raiz que ele achou e o que custou achar. */
    record Resultado(String metodo, String funcao, double raiz, boolean convergiu,
            long iteracoes, long avaliacoesDaFuncao, long avaliacoesDaDerivada,
            String problema) {

        public long avaliacoes() {
            return avaliacoesDaFuncao + avaliacoesDaDerivada;
        }

        public static Resultado falhou(String metodo, Funcao funcao, double onde,
                Contador contador, String porque) {

            return new Resultado(metodo, funcao.nome(), onde, false, contador.iteracoes(),
                    contador.avaliacoesDaFuncao(), contador.avaliacoesDaDerivada(), porque);
        }

        public static Resultado achou(String metodo, Funcao funcao, double raiz,
                Contador contador) {

            return new Resultado(metodo, funcao.nome(), raiz, true, contador.iteracoes(),
                    contador.avaliacoesDaFuncao(), contador.avaliacoesDaDerivada(), "");
        }
    }
}
