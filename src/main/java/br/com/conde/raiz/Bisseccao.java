package br.com.conde.raiz;

/**
 * A BISSECCAO: corta o intervalo ao meio e fica com a metade que ainda cerca a raiz.
 *
 * <p>E o JUIZ deste repositorio, e e o juiz certo e nao so o conveniente porque ela e a
 * unica cuja convergencia e um TEOREMA e nao uma esperanca. Com a funcao continua e as
 * duas pontas de sinais opostos, o teorema do valor intermediario garante que ha raiz
 * dentro, e cortar ao meio preserva isso para sempre.
 *
 * <p>Ela nao supoe nada mais: nao supoe derivada, nao supoe suavidade, nao supoe que a
 * funcao se pareca com uma reta. Funciona numa funcao com bico, com degrau, ou numa tabela
 * de medidas. Nenhum outro metodo daqui pode dizer isso.
 *
 * <p>E ela tem uma propriedade que vale mais do que parece: o erro depois de k passos e no
 * maximo o tamanho do intervalo dividido por dois elevado a k, e isso e sabido ANTES de
 * comecar. Com um intervalo de tamanho um e tolerancia de dez elevado a menos doze, sao
 * exatamente 40 passos, sempre, em qualquer funcao. Ela e a unica que da para orcar.
 *
 * <p>O preco e que ela nunca acelera. Cada passo ganha um bit, nem mais nem menos, mesmo
 * quando a funcao e uma reta e a resposta estava a um passo de distancia.
 */
public final class Bisseccao implements Metodo {

    @Override
    public String nome() {
        return "bisseccao";
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

        while (contador.iteracoes() < maximoDeIteracoes) {
            contador.iteracao();

            double meio = esquerda + (direita - esquerda) / 2;

            if (direita - esquerda < tolerancia) return Resultado.achou(nome(), funcao, meio, contador);

            double noMeio = funcao.em(meio, contador);

            if (noMeio == 0) return Resultado.achou(nome(), funcao, meio, contador);

            /* Fica a metade em que o sinal ainda troca: o cerco nunca se perde. */
            if (naEsquerda * noMeio < 0) {
                direita = meio;
                naDireita = noMeio;
            } else {
                esquerda = meio;
                naEsquerda = noMeio;
            }
        }

        return Resultado.falhou(nome(), funcao, esquerda + (direita - esquerda) / 2, contador,
                "passou de " + maximoDeIteracoes + " iteracoes");
    }

    /** Quantos passos a bisseccao precisa, sabido antes de comecar. */
    public static int passosPrevistos(double tamanhoDoIntervalo, double tolerancia) {
        return (int) Math.ceil(Math.log(tamanhoDoIntervalo / tolerancia) / Math.log(2));
    }
}
