package infraestrutura.excecoes;

/** Lançada ao pedir relatório/comparação de uma pesquisa que ainda não foi processada. */
public class PesquisaNaoProcessadaException extends AnaliseException {
    private static final long serialVersionUID = 1L;

    public PesquisaNaoProcessadaException(String codigo) {
        super("A pesquisa '" + codigo + "' ainda não foi processada. Processe-a antes de gerar relatórios ou comparações.");
    }
}
