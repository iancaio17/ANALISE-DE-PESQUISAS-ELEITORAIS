package dominio.pesquisa;

/** Ciclo de vida da pesquisa: planejada → em coleta → encerrada → processada. */
public enum SituacaoPesquisa {
    PLANEJADA("Planejada"),
    EM_COLETA("Em coleta"),
    ENCERRADA("Encerrada"),
    PROCESSADA("Processada");

    private final String texto;

    SituacaoPesquisa(String texto) {
        this.texto = texto;
    }

    /** Somente pesquisas encerradas (ou já processadas, para reprocessar) podem ser processadas. */
    public boolean permiteProcessamento() {
        return this == ENCERRADA || this == PROCESSADA;
    }

    @Override
    public String toString() {
        return texto;
    }
}
