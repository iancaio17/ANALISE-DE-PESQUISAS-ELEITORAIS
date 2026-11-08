package infraestrutura.excecoes;

/** Lançada quando a exportação de um relatório (CSV/PDF) falha ou o formato é desconhecido. */
public class ExportacaoException extends AnaliseException {
    private static final long serialVersionUID = 1L;

    public ExportacaoException(String mensagem) {
        super(mensagem);
    }

    public ExportacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
