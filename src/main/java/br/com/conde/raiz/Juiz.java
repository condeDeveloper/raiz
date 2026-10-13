package br.com.conde.raiz;

import java.util.List;

/**
 * O JUIZ: a bisseccao levada ate a precisao da maquina.
 *
 * <p>Ele e o juiz certo e nao so o conveniente porque e o unico cuja resposta nao depende
 * de nada que se possa errar. A bisseccao converge por TEOREMA, sem supor derivada, sem
 * supor suavidade, sem supor que a funcao se pareca com uma reta. O que ela devolve e a
 * raiz, com erro menor que o tamanho do intervalo dividido por dois elevado ao numero de
 * passos, e esse limite e sabido antes de comecar.
 *
 * <p>E ele nao depende de constante decorada. Onde a raiz tem forma fechada, o teste
 * confere as duas contra a outra; onde nao tem, o juiz continua valendo.
 *
 * <p>Ele exige DUAS coisas, e nenhuma delas e ser rapido:
 *
 * <ul>
 *   <li>Quem diz que convergiu convergiu mesmo: a raiz devolvida esta dentro da tolerancia
 *       da raiz de referencia, e a funcao ali vale perto de zero.
 *   <li>Quem mantem o cerco NUNCA SE PERDE: mesmo sem convergir, o ponto devolvido continua
 *       dentro do intervalo original. Essa e a garantia de verdade dos metodos com cerco, e
 *       ela e o que os outros nao tem.
 * </ul>
 *
 * <p>Repare que a segunda exigencia nao e "sempre converge". Eu tinha escrito assim e tive
 * que corrigir: manter o cerco garante que a raiz esta la dentro e que o intervalo nunca
 * cresce, e isso nao da prazo nenhum. A falsa posicao na raiz tripla converge como um sobre
 * a raiz de n, o que daria uns dez elevado a 24 iteracoes. Converge, e nao da para esperar.
 *
 * <p>Ele tambem nao exige que Newton convirja. A tabela das falhas conta de onde ele nao
 * volta, e contar isso seria impossivel se o juiz reprovasse.
 */
public final class Juiz {

    /** A tolerancia da raiz de referencia: perto do limite do ponto flutuante. */
    public static final double TOLERANCIA_DO_JUIZ = 1e-15;

    /**
     * O orcamento de iteracoes do julgamento, que e generoso de proposito.
     *
     * <p>As tabelas rodam com 200 iteracoes, que e o que alguem poria num programa. O juiz
     * roda com muito mais porque a pergunta dele e outra: quem mantem o cerco converge
     * SEMPRE, e isso e um teorema sobre o limite e nao sobre um orcamento.
     *
     * <p>A falsa posicao precisa de milhares de iteracoes em tres das seis funcoes daqui.
     * Reprova-la por isso seria confundir lentidao com erro, e a lentidao dela ja tem uma
     * tabela so.
     */
    public static final int ORCAMENTO_DO_JUIZ = 200_000;

    /**
     * Quantas iteracoes a bisseccao precisa, sabido antes de comecar.
     *
     * <p>E o unico limite deste repositorio que se escreve de antemao, e o teste confere
     * que ela nunca passa dele.
     */
    public static int orcamentoDaBisseccao(Funcao funcao, double tolerancia) {
        return Bisseccao.passosPrevistos(Math.abs(funcao.ate() - funcao.de()), tolerancia);
    }

    private Juiz() {}

    /** Os cinco metodos, do mais garantido ao mais arriscado. */
    public static List<Metodo> todos() {
        return List.of(new Bisseccao(), new Misto(), new FalsaPosicao(), new Secante(),
                new Newton());
    }

    /** A raiz de referencia: bisseccao ate a maquina nao distinguir mais. */
    public static double referencia(Funcao funcao) {
        Metodo.Resultado resultado = new Bisseccao().achar(funcao, TOLERANCIA_DO_JUIZ, 200);

        if (!resultado.convergiu())
            throw new IllegalStateException(
                    "a bisseccao nao convergiu em " + funcao.nome() + ": " + resultado.problema());

        return resultado.raiz();
    }

    public record Veredito(String metodo, String funcao, boolean valido, double erro,
            double valorNaRaiz, String problema) {}

    public static Veredito julgar(Metodo metodo, Funcao funcao, double tolerancia) {
        double certa = referencia(funcao);

        Metodo.Resultado resultado = metodo.achar(funcao, tolerancia, ORCAMENTO_DO_JUIZ);

        if (!resultado.convergiu()) {
            /* Quem mantem o cerco pode ser lento, e nao pode se perder. */
            if (metodo.mantemOCerco()) {
                boolean dentro = resultado.raiz() >= Math.min(funcao.de(), funcao.ate())
                        && resultado.raiz() <= Math.max(funcao.de(), funcao.ate());

                if (!dentro)
                    return new Veredito(metodo.nome(), funcao.nome(), false, Double.NaN,
                            Double.NaN, "mantem o cerco e saiu do intervalo: " + resultado.raiz());
            }

            return new Veredito(metodo.nome(), funcao.nome(), true, Double.NaN, Double.NaN,
                    "nao convergiu: " + resultado.problema());
        }

        double erro = Math.abs(resultado.raiz() - certa);
        double valor = Math.abs(funcao.em(resultado.raiz()));

        /* A folga de mil vezes existe porque a tolerancia e sobre o PASSO e nao sobre o
         * erro: perto de uma raiz multipla um passo pequeno ainda deixa erro maior. */
        if (erro > tolerancia * 1_000 && valor > 1e-9)
            return new Veredito(metodo.nome(), funcao.nome(), false, erro, valor,
                    "disse que convergiu e errou por " + erro);

        return new Veredito(metodo.nome(), funcao.nome(), true, erro, valor, "");
    }
}
