package dominio.admin;

/** Perfis de acesso do sistema. O administrador tem acesso a todas as funcionalidades. */
public enum Perfil {
    ADMINISTRADOR("Administrador"),
    PESQUISADOR("Pesquisador"),
    ANALISTA("Analista");

    private final String texto;

    Perfil(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
