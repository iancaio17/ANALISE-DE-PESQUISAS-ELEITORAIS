package infraestrutura.excecoes;

/** Lançada quando o usuário não está autenticado ou não possui o perfil exigido. */
public class AcessoNegadoException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
