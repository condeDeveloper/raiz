package br.com.conde.raiz;

/**
 * NEWTON: anda ate onde a tangente no ponto cruza o eixo.
 *
 * <p>Quando funciona, ele e de outra categoria: o numero de digitos certos DOBRA a cada
 * passo. Partindo de um palpite com um digito certo, sao dois, quatro, oito, dezesseis.
 * Chegar a dezesseis digitos custa quatro ou cinco passos.
 *
 * <p>E ele LARGA O CERCO. Nada no metodo olha o intervalo: ele anda para onde a tangente
 * mandar, e a tangente pode mandar para fora, para longe, ou de volta ao mesmo lugar.
 *
 * <h2>Tres jeitos de ele nao voltar</h2>
 *
 * <ul>
 *   <li>A derivada zera: a tangente fica horizontal e o proximo ponto e o infinito.
 *   <li>Ele se afasta: no arco tangente, a partir de um ponto exato, cada passo cai mais
 *       longe da raiz do que o anterior estava. A tabela das falhas acha esse ponto.
 *   <li>Ele cicla: ha funcoes em que dois pontos mandam um para o outro para sempre.
 * </ul>
 *
 * <p>E ha uma quarta coisa, que nao e falha e e decepcao: numa raiz MULTIPLA a derivada
 * tambem zera ali, e a duplicacao de digitos vira um ganho de uma fracao fixa por passo.
 * Ele continua convergindo e passa a ser mais lento que a bisseccao. A tabela da multipla
 * mede isso.
 */
public final class Newton implements Metodo {

    private final boolean exigeResiduo;

    /** Para e so quando o passo for pequeno E a funcao ali valer perto de zero. */
    public Newton() {
        this(true);
    }

    /**
     * Com exigeResiduo falso, ele para so pelo tamanho do passo.
     *
     * <p>Essa e a parada ingenua, e ela MENTE: numa funcao achatada os passos ficam
     * minusculos longe da raiz e o metodo anuncia que chegou. A tabela da parada mede isso,
     * e e por isso que esta opcao existe em vez de ter sido apagada.
     */
    public Newton(boolean exigeResiduo) {
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
        return "Newton" + (exigeResiduo ? "" : " so passo");
    }

    @Override
    public boolean mantemOCerco() {
        return false;
    }

    @Override
    public boolean precisaDaDerivada() {
        return true;
    }

    @Override
    public Resultado achar(Funcao funcao, double tolerancia, int maximoDeIteracoes) {
        /* O palpite inicial e o meio do intervalo: e assim que ele e usado na vida. */
        return achar(funcao, funcao.de() + (funcao.ate() - funcao.de()) / 2, tolerancia,
                maximoDeIteracoes);
    }

    /** O mesmo, a partir de um palpite escolhido, que e o que a tabela das falhas varre. */
    public Resultado achar(Funcao funcao, double palpite, double tolerancia,
            int maximoDeIteracoes) {

        Contador contador = new Contador();

        double x = palpite;

        while (contador.iteracoes() < maximoDeIteracoes) {
            contador.iteracao();

            double valor = funcao.em(x, contador);

            if (valor == 0) return Resultado.achou(nome(), funcao, x, contador);

            double inclinacao = funcao.derivada(x, contador);

            if (inclinacao == 0)
                return Resultado.falhou(nome(), funcao, x, contador,
                        "a derivada zerou em " + x + ": a tangente ficou horizontal");

            double proximo = x - valor / inclinacao;

            if (!Double.isFinite(proximo))
                return Resultado.falhou(nome(), funcao, x, contador, "o passo saiu dos numeros");

            if (Math.abs(proximo - x) < tolerancia && paradaVale(funcao, proximo, contador))
                return Resultado.achou(nome(), funcao, proximo, contador);

            x = proximo;
        }

        return Resultado.falhou(nome(), funcao, x, contador,
                "passou de " + maximoDeIteracoes + " iteracoes");
    }
}
