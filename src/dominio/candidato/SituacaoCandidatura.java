package dominio.candidato;

/** Situações possíveis de uma candidatura. */
public enum SituacaoCandidatura {
    PRE_CANDIDATO("Pré-candidato", true, false),
    CANDIDATO("Candidato", true, true),
    INATIVA("Inativa", false, false),
    VALIDADA("Validada", true, true);

    private final String texto;
    private final boolean ativa;
    private final boolean podeParticiparDePesquisa;

    SituacaoCandidatura(String texto, boolean ativa, boolean podeParticiparDePesquisa) {
        this.texto = texto;
        this.ativa = ativa;
        this.podeParticiparDePesquisa = podeParticiparDePesquisa;
    }

    /** Candidatura inativa não conta como ativa nos indicadores cadastrais. */
    public boolean isAtiva() {
        return ativa;
    }

    /** Somente candidatos e candidaturas validadas podem ser associados a pesquisas. */
    public boolean podeParticiparDePesquisa() {
        return podeParticiparDePesquisa;
    }

    @Override
    public String toString() {
        return texto;
    }
}
