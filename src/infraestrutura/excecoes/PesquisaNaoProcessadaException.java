package infraestrutura.excecoes;

/** Lançada ao pedir relatório ou comparação de uma pesquisa ainda não processada. */
public class PesquisaNaoProcessadaException extends AnaliseException {

    private static final long serialVersionUID = 1L;

    public PesquisaNaoProcessadaException(String codigo) {
        super("A pesquisa '" + codigo + "' ainda não foi processada. "
                + "Processe-a antes de gerar relatórios ou comparações.");
    }
}
