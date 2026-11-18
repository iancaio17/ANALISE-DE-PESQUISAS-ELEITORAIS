package infraestrutura.excecoes;

/** Lançada quando não há entrevistas suficientes para calcular estatísticas. */
public class SemRespostasValidasException extends AnaliseException {

    private static final long serialVersionUID = 1L;

    public SemRespostasValidasException(String motivo) {
        super(motivo);
    }
}
