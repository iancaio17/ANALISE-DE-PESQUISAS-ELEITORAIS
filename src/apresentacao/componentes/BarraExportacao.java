package apresentacao.componentes;

import dominio.analise.Relatorio;
import java.util.function.Supplier;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Window;

/** Par de botões "Exportar CSV" e "Exportar PDF" que começam desabilitados. */
public class BarraExportacao extends FlowPane {

    private final Button csv;
    private final Button pdf;

    /**
     * @param janela    janela dona do seletor de arquivo (lida só no clique, pois a tela ainda pode não existir)
     * @param relatorio fonte do relatório a exportar
     */
    public BarraExportacao(Supplier<Window> janela, Supplier<Relatorio> relatorio, String nomeSugerido,
            ExportadorTela.Gravador gravador) {
        super(12, 12);
        csv = Componentes.botao("Exportar CSV",
                () -> ExportadorTela.exportar(janela.get(), relatorio.get(), "csv", nomeSugerido, gravador));
        pdf = Componentes.secundario("Exportar PDF",
                () -> ExportadorTela.exportar(janela.get(), relatorio.get(), "pdf", nomeSugerido, gravador));
        getChildren().addAll(csv, pdf);
        habilitar(false);
    }

    public void habilitar(boolean habilitado) {
        csv.setDisable(!habilitado);
        pdf.setDisable(!habilitado);
    }
}
