package infraestrutura.utils;

import infraestrutura.excecoes.ExportacaoException;

/** FACTORY: devolve o exportador correto a partir do nome do formato escolhido na tela. */
public final class ExportadorFactory {

    private ExportadorFactory() { }

    public static ExportadorRelatorio criar(String formato) {
        if (formato == null) {
            throw new ExportacaoException("Formato de exportação não informado.");
        }
        switch (formato.trim().toLowerCase()) {
            case "csv": return new ExportadorCSV();
            case "pdf": return new ExportadorPDF();
            default:
                throw new ExportacaoException("Formato de exportação não suportado: '" + formato + "'. Use CSV ou PDF.");
        }
    }
}
