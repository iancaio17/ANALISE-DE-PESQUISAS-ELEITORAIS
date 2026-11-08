package infraestrutura.excecoes;

/** Lançada quando duas pesquisas não possuem critérios compatíveis para comparação. */
public class ComparacaoIncompativelException extends AnaliseException {
    private static final long serialVersionUID = 1L;

    public ComparacaoIncompativelException(String motivo) {
        super("Comparação não permitida: " + motivo);
    }
}
