package apresentacao.componentes;

import dominio.analise.Relatorio;
import infraestrutura.utils.Validacao;
import java.io.File;
import java.nio.file.Path;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/** Pede ao usuário o destino do arquivo e delega a gravação ao serviço do módulo. */
public final class ExportadorTela {

    /** Grava o relatório no destino escolhido. */
    @FunctionalInterface
    public interface Gravador {
        void gravar(Relatorio relatorio, String formato, Path destino);
    }

    private ExportadorTela() {
    }

    public static void exportar(Window janela, Relatorio relatorio, String formato, String nomeSugerido,
            Gravador gravador) {
        Validacao.exigir(relatorio != null, "Gere os resultados antes de exportar.");
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Exportar relatório");
        seletor.setInitialFileName(nomeSugerido + "." + formato);
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter(formato.toUpperCase(), "*." + formato));
        File arquivo = seletor.showSaveDialog(janela);
        if (arquivo != null) {
            gravador.gravar(relatorio, formato, arquivo.toPath());
            Dialogos.sucesso("Relatório salvo em " + arquivo);
        }
    }
}
