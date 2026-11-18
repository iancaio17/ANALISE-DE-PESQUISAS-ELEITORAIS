package apresentacao.analise;

import apresentacao.componentes.Leitura;
import dominio.analise.Distribuicao;
import dominio.analise.ItemResultado;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

/** Formas de desenhar uma distribuição. Cada constante sabe criar o seu próprio gráfico. */
enum TipoGrafico {

    BARRAS("Barras") {
        @Override
        Node criar(Distribuicao distribuicao) {
            NumberAxis eixoPercentual = eixoPercentual();
            BarChart<Number, String> grafico = new BarChart<>(eixoPercentual, new CategoryAxis());
            XYChart.Series<Number, String> serie = new XYChart.Series<>();
            for (ItemResultado item : distribuicao.getItens()) {
                serie.getData().add(new XYChart.Data<>(item.getPercentual(), item.getRotulo()));
            }
            grafico.getData().add(serie);
            return configurar(grafico);
        }
    },

    COLUNAS("Colunas") {
        @Override
        Node criar(Distribuicao distribuicao) {
            BarChart<String, Number> grafico = new BarChart<>(new CategoryAxis(), eixoPercentual());
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            for (ItemResultado item : distribuicao.getItens()) {
                serie.getData().add(new XYChart.Data<>(item.getRotulo(), item.getPercentual()));
            }
            grafico.getData().add(serie);
            return configurar(grafico);
        }
    },

    PIZZA("Pizza") {
        @Override
        Node criar(Distribuicao distribuicao) {
            PieChart grafico = new PieChart(FXCollections.observableArrayList(distribuicao.getItens().stream()
                    .filter(item -> item.getQuantidadeAbsoluta() > 0)
                    .map(item -> new PieChart.Data(
                            item.getRotulo() + " (" + Leitura.percentual(item.getPercentual()) + "%)",
                            item.getQuantidadeAbsoluta()))
                    .toList()));
            grafico.setTitle("Distribuição de respostas válidas");
            grafico.setAnimated(false);
            grafico.setPrefHeight(350);
            return grafico;
        }
    };

    private final String nome;

    TipoGrafico(String nome) {
        this.nome = nome;
    }

    abstract Node criar(Distribuicao distribuicao);

    private static NumberAxis eixoPercentual() {
        NumberAxis eixo = new NumberAxis(0, 100, 20);
        eixo.setLabel("Percentual (%)");
        return eixo;
    }

    private static Node configurar(XYChart<?, ?> grafico) {
        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setPrefHeight(340);
        return grafico;
    }

    @Override
    public String toString() {
        return nome;
    }
}
