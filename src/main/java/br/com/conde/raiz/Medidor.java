package br.com.conde.raiz;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("convergencia") || comando.equals("tudo")) convergencia();
        if (comando.equals("falhas") || comando.equals("tudo")) falhas();
        if (comando.equals("estagnacao") || comando.equals("tudo")) estagnacao();
        if (comando.equals("multipla") || comando.equals("tudo")) multipla();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("TODA RAIZ DEVOLVIDA E RAIZ, E QUEM MANTEM O CERCO SEMPRE CONVERGE?");
        System.out.println("O juiz e a bisseccao levada ate a precisao da maquina.");
        System.out.println();

        var linhas = Medidas.aCorrecao();
        long invalidas = linhas.stream().filter(l -> !l.valido()).count();

        System.out.printf("  %-20s %-16s %12s %14s %16s %10s %s%n",
                "funcao", "metodo", "convergiu", "erro", "funcao na raiz", "confere",
                "problema");

        for (var l : linhas)
            System.out.printf(Locale.ROOT, "  %-20s %-16s %12s %14.2e %16.2e %10s %s%n",
                    l.funcao(), l.metodo(), l.convergiu() ? "sim" : "nao", l.erro(),
                    l.valorNaRaiz(), l.valido() ? "sim" : "NAO", l.problema());

        System.out.println();
        System.out.printf("  %d combinacoes, %d invalidas.%n", linhas.size(), invalidas);
        System.out.println();
    }

    private static void convergencia() {
        System.out.println();
        System.out.println("QUANTAS CONTAS ATE A PRECISAO DE " + Medidas.TOLERANCIA + ".");
        System.out.println("A coluna que importa e a de avaliacoes, nao a de iteracoes.");
        System.out.println();

        System.out.printf("  %-20s %-16s %12s %12s %12s %14s %14s%n",
                "funcao", "metodo", "convergiu", "iteracoes", "da funcao", "da derivada",
                "avaliacoes");

        for (var l : Medidas.aConvergencia())
            System.out.printf("  %-20s %-16s %12s %12d %12d %14d %14d%n",
                    l.funcao(), l.metodo(), l.convergiu() ? "sim" : "nao", l.iteracoes(),
                    l.avaliacoesDaFuncao(), l.avaliacoesDaDerivada(), l.avaliacoes());

        System.out.println();
    }

    private static void falhas() {
        System.out.println();
        System.out.println("DE ONDE NEWTON NAO VOLTA, varrendo " + Medidas.PONTOS_DE_PARTIDA
                + " pontos de partida em cada funcao.");
        System.out.println();

        System.out.printf("  %-20s %10s %14s %12s %16s %16s%n",
                "funcao", "pontos", "convergiram", "fracao", "menor que falha",
                "maior que falha");

        for (var l : Medidas.asFalhas())
            System.out.printf(Locale.ROOT, "  %-20s %10d %14d %12.4f %16.4f %16.4f%n",
                    l.funcao(), l.pontos(), l.convergiram(), l.fracao(), l.menorQueFalha(),
                    l.maiorQueFalha());

        System.out.println();
        System.out.printf(Locale.ROOT,
                "  A fronteira exata do arco tangente, achada por bisseccao: %.12f%n",
                Medidas.fronteiraDoArcoTangente());
        System.out.println();
    }

    private static void estagnacao() {
        System.out.println();
        System.out.println("A FALSA POSICAO E O LADO QUE NUNCA ANDA.");
        System.out.println();

        System.out.printf("  %-20s %14s %16s %16s %16s %14s %16s%n",
                "funcao", "falsa posicao", "bisseccao", "parada a esquerda",
                "parada a direita", "fracao parada", "vezes a bisseccao");

        for (var l : Medidas.aEstagnacao())
            System.out.printf(Locale.ROOT, "  %-20s %14d %16d %16d %16d %14.4f %16.2f%n",
                    l.funcao(), l.iteracoesDaFalsa(), l.iteracoesDaBisseccao(),
                    l.maiorSeguidaNaEsquerda(), l.maiorSeguidaNaDireita(), l.fracaoParada(),
                    l.vezesABisseccao());

        System.out.println();
    }

    private static void multipla() {
        System.out.println();
        System.out.println("A RAIZ TRIPLA DERRUBA A ORDEM, em (x menos 1) ao cubo.");
        System.out.println("A razao do erro perto do fim: 0,67 e linear, perto de zero e quadratico.");
        System.out.println();

        System.out.printf("  %-16s %12s %12s %14s %14s %16s%n",
                "metodo", "convergiu", "iteracoes", "avaliacoes", "erro", "razao do erro");

        for (var l : Medidas.aMultipla())
            System.out.printf(Locale.ROOT, "  %-16s %12s %12d %14d %14.2e %16.4f%n",
                    l.metodo(), l.convergiu() ? "sim" : "nao", l.iteracoes(), l.avaliacoes(),
                    l.erro(), l.razaoDoErro());

        System.out.println();
    }
}
