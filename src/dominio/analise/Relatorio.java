package dominio.analise;

import java.util.List;
import java.util.Locale;

/**
 * Relatório abstrato (TEMPLATE METHOD): as subclasses montam as linhas (tabela); esta classe sabe
 * transformá-las em texto para a tela. Os exportadores (CSV/PDF) também consomem estas linhas.
 *
 * Convenção das linhas: array com 0 posições = linha em branco; 1 posição = título/seção;
 * 2 posições = par "chave / valor"; 3 ou mais = linha de tabela.
 */
public abstract class Relatorio {

    protected static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public abstract String getTitulo();

    public abstract List<String[]> gerarLinhas();

    /** Versão em texto, com colunas alinhadas, para exibir em tela. */
    public String paraTexto() {
        List<String[]> linhas = gerarLinhas();
        int colunas = 0;
        for (String[] l : linhas) colunas = Math.max(colunas, l.length);
        int[] largura = new int[colunas];
        // Só linhas de TABELA (3+ colunas) definem a largura; linhas "chave/valor" não a inflam.
        for (String[] l : linhas) {
            if (l.length < 3) continue;
            for (int j = 0; j < l.length; j++) largura[j] = Math.max(largura[j], l[j].length());
        }

        StringBuilder sb = new StringBuilder();
        for (String[] l : linhas) {
            for (int j = 0; j < l.length; j++) {
                boolean ultima = (j == l.length - 1);
                sb.append(ultima ? l[j] : pad(l[j], largura[j]));
                if (!ultima) sb.append("  ");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    protected static String pct(double valor) {
        return String.format(PT_BR, "%.1f", valor);
    }

    private static String pad(String s, int n) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) sb.append(' ');
        return sb.toString();
    }
}
