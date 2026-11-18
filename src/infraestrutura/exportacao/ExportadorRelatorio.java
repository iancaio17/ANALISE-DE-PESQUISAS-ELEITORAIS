package infraestrutura.exportacao;

import dominio.analise.Relatorio;
import java.nio.file.Path;

/** Estratégia de exportação: cada formato de arquivo (CSV, PDF...) é uma implementação. */
public interface ExportadorRelatorio {

    /** Extensão do arquivo gerado, sem ponto (ex.: "csv"). */
    String getExtensao();

    /**
     * Grava o relatório em arquivo.
     *
     * @throws infraestrutura.excecoes.ExportacaoException se o arquivo não puder ser gravado
     */
    void exportar(Relatorio relatorio, Path destino);
}
