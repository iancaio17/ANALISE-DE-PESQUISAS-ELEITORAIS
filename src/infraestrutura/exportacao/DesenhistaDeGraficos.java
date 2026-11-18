package infraestrutura.exportacao;

import dominio.analise.Grafico;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Desenha gráficos de barras horizontais como comandos de página do PDF (retângulos e texto Courier).
 * Cada barra mede o percentual da opção, com 100% ocupando toda a área do gráfico. Quando uma
 * página acaba, o desenho continua na seguinte.
 */
class DesenhistaDeGraficos {

    private static final double LIMITE_SUPERIOR = 800;
    private static final double LIMITE_INFERIOR = 60;
    private static final double MARGEM_ESQUERDA = 50;
    private static final double INICIO_DAS_BARRAS = 260;
    private static final double LARGURA_DE_100_POR_CENTO = 240;
    private static final double ALTURA_DA_BARRA = 9;
    private static final double ESPACO_ENTRE_BARRAS = 2;
    private static final double ESPACO_ENTRE_OPCOES = 6;
    private static final double ESPACO_ENTRE_GRAFICOS = 22;
    private static final int MAXIMO_DE_CARACTERES_DO_ROTULO = 38;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    /** Cores das séries (R G B de 0 a 1): azul e laranja, distinguíveis também em impressão cinza. */
    private static final String[] CORES = {"0.13 0.36 0.66", "0.90 0.50 0.10"};

    private final List<String> paginas = new ArrayList<>();
    private StringBuilder pagina;
    private double y;

    /** Devolve o conteúdo (comandos) de cada página necessária para desenhar todos os gráficos. */
    List<String> desenhar(List<Grafico> graficos) {
        for (Grafico grafico : graficos) {
            desenharGrafico(grafico);
        }
        fecharPagina();
        return paginas;
    }

    private void desenharGrafico(Grafico grafico) {
        double alturaMinima = 40 + alturaDaOpcao(grafico);
        if (pagina == null || y - alturaMinima < LIMITE_INFERIOR) {
            novaPagina();
        }
        escreverTitulo(grafico.getTitulo(), false);
        escreverLegenda(grafico);

        for (int i = 0; i < grafico.getRotulos().size(); i++) {
            if (y - alturaDaOpcao(grafico) < LIMITE_INFERIOR) {
                novaPagina();
                escreverTitulo(grafico.getTitulo(), true);
            }
            desenharOpcao(grafico, i);
        }
        y -= ESPACO_ENTRE_GRAFICOS;
    }

    private void desenharOpcao(Grafico grafico, int indice) {
        double topo = y;
        escreverTexto(MARGEM_ESQUERDA, topo - ALTURA_DA_BARRA, 8, abreviar(grafico.getRotulos().get(indice)));
        for (int s = 0; s < grafico.getSeries().size(); s++) {
            double percentual = grafico.getSeries().get(s).percentuais().get(indice);
            double largura = Math.max(0, Math.min(100, percentual)) * LARGURA_DE_100_POR_CENTO / 100.0;
            double baseDaBarra = topo - (s + 1) * ALTURA_DA_BARRA - s * ESPACO_ENTRE_BARRAS;
            pagina.append(CORES[s % CORES.length]).append(" rg\n")
                    .append(String.format(Locale.ROOT, "%.1f %.1f %.1f %.1f re f\n",
                            INICIO_DAS_BARRAS, baseDaBarra, largura, ALTURA_DA_BARRA));
            pagina.append("0 g\n");
            escreverTexto(INICIO_DAS_BARRAS + largura + 4, baseDaBarra + 1, 8,
                    String.format(PT_BR, "%.1f%%", percentual));
        }
        y -= alturaDaOpcao(grafico);
    }

    private double alturaDaOpcao(Grafico grafico) {
        int series = grafico.getSeries().size();
        return series * ALTURA_DA_BARRA + (series - 1) * ESPACO_ENTRE_BARRAS + ESPACO_ENTRE_OPCOES;
    }

    private void escreverTitulo(String titulo, boolean continuacao) {
        String texto = "GRÁFICO: " + titulo + (continuacao ? " (continuação)" : "");
        for (String linha : quebrar(texto, 88)) {
            escreverTexto(MARGEM_ESQUERDA, y - 10, 9, linha);
            y -= 13;
        }
        y -= 4;
    }

    private void escreverLegenda(Grafico grafico) {
        if (grafico.getSeries().size() < 2) {
            return;
        }
        double x = MARGEM_ESQUERDA;
        for (int s = 0; s < grafico.getSeries().size(); s++) {
            pagina.append(CORES[s % CORES.length]).append(" rg\n")
                    .append(String.format(Locale.ROOT, "%.1f %.1f 8 8 re f\n0 g\n", x, y - 9));
            String nome = grafico.getSeries().get(s).nome();
            escreverTexto(x + 12, y - 8, 8, nome);
            x += 12 + nome.length() * 4.8 + 16;
        }
        y -= 16;
    }

    private void escreverTexto(double x, double base, int tamanho, String texto) {
        pagina.append(String.format(Locale.ROOT, "BT /F1 %d Tf %.1f %.1f Td (", tamanho, x, base))
                .append(escapar(texto)).append(") Tj ET\n");
    }

    private void novaPagina() {
        fecharPagina();
        pagina = new StringBuilder();
        y = LIMITE_SUPERIOR;
    }

    private void fecharPagina() {
        if (pagina != null) {
            paginas.add(pagina.toString());
            pagina = null;
        }
    }

    private static String abreviar(String rotulo) {
        if (rotulo.length() <= MAXIMO_DE_CARACTERES_DO_ROTULO) {
            return rotulo;
        }
        return rotulo.substring(0, MAXIMO_DE_CARACTERES_DO_ROTULO - 3) + "...";
    }

    private static List<String> quebrar(String texto, int largura) {
        List<String> linhas = new ArrayList<>();
        String resto = texto;
        while (resto.length() > largura) {
            int corte = resto.lastIndexOf(' ', largura);
            if (corte < largura / 2) {
                corte = largura;
            }
            linhas.add(resto.substring(0, corte));
            resto = resto.substring(corte).stripLeading();
        }
        linhas.add(resto);
        return linhas;
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
