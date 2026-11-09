package infraestrutura.excecoes;

public class PesquisaRegraNegocioException extends RuntimeException {
    public PesquisaRegraNegocioException(String mensagem) {
        super(mensagem);
    }
}