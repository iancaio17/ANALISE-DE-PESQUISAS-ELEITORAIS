package infraestrutura.excecoes;

/** Lançada ao tentar processar uma pesquisa cuja coleta ainda não foi encerrada. */
public class PesquisaNaoEncerradaException extends AnaliseException {
    private static final long serialVersionUID = 1L;

    public PesquisaNaoEncerradaException(String codigo, String situacaoAtual) {
        super("A pesquisa '" + codigo + "' está com situação '" + situacaoAtual
                + "'. Uma pesquisa somente pode ser processada após o encerramento da coleta.");
    }
}
