package br.com.conde.raiz;

/**
 * A SECANTE: Newton com a tangente trocada pela reta entre os dois ultimos pontos.
 *
 * <p>A conta e a mesma da falsa posicao, com uma diferenca que muda tudo: ela usa os dois
 * pontos MAIS RECENTES, e nao os dois que cercam a raiz. Ela larga o cerco, e por isso nao
 * fica com uma ponta parada.
 *
 * <p>Ela converge com ordem 1,618, o numero de ouro, que e menos que os 2 de Newton e muito
 * mais que o 1 da bisseccao. E, ao contrario de Newton, nao precisa da derivada.
 *
 * <p>E e por isso que a comparacao certa entre os dois nao e por iteracao, e por AVALIACAO:
 * cada passo de Newton gasta a funcao e a derivada, e cada passo da secante gasta so a
 * funcao. Com a derivada custando o mesmo que a funcao, a secante faz 1,618 por avaliacao
 * contra a raiz quadrada de 2, que e 1,414, de Newton. A tabela da convergencia tem as duas
 * colunas por causa disso.
 *
 * <p>Ela falha pelos mesmos motivos de Newton, e mais um: se os dois ultimos pontos derem o
 * mesmo valor, a reta fica horizontal e nao ha onde cruzar.
 */
public final class Secante implements Metodo {

    private final boolean exigeResiduo;

    /** Para e so quando o passo for pequeno E a funcao ali valer perto de zero. */
    public Secante() {
        this(true);
    }

    /**
     * Com exigeResiduo falso, ele para so pelo tamanho do passo.
     *
     * <p>Essa e a parada ingenua, e ela MENTE: numa funcao achatada os passos ficam
     * minusculos longe da raiz e o metodo anuncia que chegou. A tabela da parada mede isso,
     * e e por isso que esta opcao existe em vez de ter sido apagada.
     */
    public Secante(boolean exigeResiduo) {
        this.exigeResiduo = exigeResiduo;
    }

    public boolean exigeResiduo() {
        return exigeResiduo;
    }

    /** O residuo esta pequeno o bastante para a parada valer. */
    private boolean paradaVale(Funcao funcao, double x, Contador contador) {
        if (!exigeResiduo) return true;

        return Math.abs(funcao.em(x, contador)) < 1e-8;
    }

    @Override
    public String nome() {
        return "secante" + (exigeResiduo ? "" : " so passo");
    }

    @Override
    public boolean mantemOCerco() {
        return false;
    }

    @Override
    public boolean precisaDaDerivada() {
        return false;
    }

    @Override
    public Resultado achar(Funcao funcao, double tolerancia, int maximoDeIteracoes) {
        Contador contador = new Contador();

        double anterior = funcao.de();
        double atual = funcao.ate();

        double noAnterior = funcao.em(anterior, contador);
        double noAtual = funcao.em(atual, contador);

        while (contador.iteracoes() < maximoDeIteracoes) {
            contador.iteracao();

            if (noAtual == noAnterior)
                return Resultado.falhou(nome(), funcao, atual, contador,
                        "os dois pontos deram o mesmo valor: a reta ficou horizontal");

            double proximo = atual - noAtual * (atual - anterior) / (noAtual - noAnterior);

            if (!Double.isFinite(proximo))
                return Resultado.falhou(nome(), funcao, atual, contador, "o passo saiu dos numeros");

            if (Math.abs(proximo - atual) < tolerancia && paradaVale(funcao, proximo, contador))
                return Resultado.achou(nome(), funcao, proximo, contador);

            anterior = atual;
            noAnterior = noAtual;

            atual = proximo;
            noAtual = funcao.em(atual, contador);
        }

        return Resultado.falhou(nome(), funcao, atual, contador,
                "passou de " + maximoDeIteracoes + " iteracoes");
    }
}
