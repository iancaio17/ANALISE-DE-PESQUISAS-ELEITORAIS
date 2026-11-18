package infraestrutura.excecoes;

/** Lançada quando os dados não podem ser lidos ou gravados em arquivo. */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
