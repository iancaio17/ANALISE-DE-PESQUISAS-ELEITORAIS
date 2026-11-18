package dominio.analise;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dados de um gráfico de barras percentuais, independentes do formato de saída. Cada exportador
 * decide como desenhá-lo: o PDF desenha barras; o CSV (que só guarda texto) traz a tabela do gráfico
 * e uma coluna de barras em texto.
 *
 * <p>Os percentuais vêm sempre de {@link ItemResultado}: respostas especiais ficam fora do gráfico,
 * como na tela.
 */
public final class Grafico {

    /** Largura, em caracteres, da barra de texto que representa 100%. */
    private static final int CARACTERES_DA_BARRA_COMPLETA = 40;
    private static final String CARACTERE_DA_BARRA = "█";
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    /** Uma série de valores (por exemplo, a pesquisa A) com um percentual para cada rótulo. */
    public record Serie(String nome, List<Double> percentuais) {
    }

    private final String titulo;
    private final List<String> rotulos;
    private final List<Serie> series;

    public Grafico(String titulo, List<String> rotulos, List<Serie> series) {
        for (Serie serie : series) {
            if (serie.percentuais().size() != rotulos.size()) {
                throw new IllegalArgumentException("Cada série precisa de um valor por rótulo.");
            }
        }
        this.titulo = titulo;
        this.rotulos = List.copyOf(rotulos);
        this.series = List.copyOf(series);
    }

    /** Gráfico de uma única série com as opções válidas de uma distribuição. */
    public static Grafico deDistribuicao(String titulo, Distribuicao distribuicao) {
        List<String> rotulos = new ArrayList<>();
        List<Double> percentuais = new ArrayList<>();
        for (ItemResultado item : distribuicao.getItens()) {
            rotulos.add(item.getRotulo());
            percentuais.add(item.getPercentual());
        }
        return new Grafico(titulo, rotulos, List.of(new Serie("Percentual (%)", percentuais)));
    }

    public String getTitulo() {
        return titulo;
    }

    public List<String> getRotulos() {
        return rotulos;
    }

    public List<Serie> getSeries() {
        return series;
    }

    /** Linhas de tabela que representam o gráfico em formatos só de texto (CSV). */
    public List<String[]> gerarLinhas() {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[] {"GRÁFICO: " + titulo});

        List<String> cabecalho = new ArrayList<>();
        cabecalho.add("Opção");
        for (Serie serie : series) {
            cabecalho.add(serie.nome());
        }
        for (Serie serie : series) {
            cabecalho.add("Barra · " + serie.nome());
        }
        linhas.add(cabecalho.toArray(String[]::new));

        for (int i = 0; i < rotulos.size(); i++) {
            List<String> linha = new ArrayList<>();
            linha.add(rotulos.get(i));
            for (Serie serie : series) {
                linha.add(String.format(PT_BR, "%.1f", serie.percentuais().get(i)));
            }
            for (Serie serie : series) {
                linha.add(barraDeTexto(serie.percentuais().get(i)));
            }
            linhas.add(linha.toArray(String[]::new));
        }
        return linhas;
    }

    private static String barraDeTexto(double percentual) {
        long tamanho = Math.round(percentual * CARACTERES_DA_BARRA_COMPLETA / 100.0);
        return CARACTERE_DA_BARRA.repeat((int) Math.max(0, tamanho));
    }
}
