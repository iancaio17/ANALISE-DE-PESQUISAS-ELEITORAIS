package infraestrutura.utils;

import dominio.analise.Relatorio;

import java.nio.file.Path;

/** STRATEGY: cada formato de arquivo (CSV, PDF...) é uma implementação desta interface. */
public interface ExportadorRelatorio {

    /** Extensão do arquivo gerado, sem ponto (ex.: "csv"). */
    String getExtensao();

    /** Grava o relatório em arquivo. @throws infraestrutura.excecoes.ExportacaoException em caso de falha. */
    void exportar(Relatorio relatorio, Path destino);
}
