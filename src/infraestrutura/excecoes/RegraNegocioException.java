package infraestrutura.excecoes;

/**
 * Exceção base para qualquer violação de regra de negócio do sistema.
 * A interface gráfica mostra a mensagem ao usuário sem encerrar a aplicação.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    public RegraNegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
