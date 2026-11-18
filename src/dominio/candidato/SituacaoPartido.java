package dominio.candidato;

/** Situações possíveis de um partido. */
public enum SituacaoPartido {
    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String texto;

    SituacaoPartido(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
