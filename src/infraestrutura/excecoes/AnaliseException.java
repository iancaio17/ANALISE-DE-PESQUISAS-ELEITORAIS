package infraestrutura.excecoes;

/** Exceção base dos erros de regra de negócio do módulo de Análise Estatística. */
public class AnaliseException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public AnaliseException(String mensagem) {
        super(mensagem);
    }

    public AnaliseException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
