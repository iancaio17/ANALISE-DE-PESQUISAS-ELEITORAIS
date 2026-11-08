package infraestrutura.excecoes;

/** Lançada quando ocorre erro ao ler ou gravar os arquivos de dados do sistema. */
public class PersistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
