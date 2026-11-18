package dominio.admin;

/** Tipos de categoria que o administrador pode cadastrar como parâmetro das pesquisas. */
public enum TipoParametro {
    FAIXA_ETARIA("Faixa etária"),
    ESCOLARIDADE("Escolaridade"),
    REGIAO("Região"),
    FAIXA_RENDA("Faixa de renda");

    private final String texto;

    TipoParametro(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
