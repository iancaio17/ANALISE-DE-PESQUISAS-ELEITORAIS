package dominio.pesquisa;

/** Tipos de pesquisa eleitoral simulada. */
public enum TipoPesquisa {
    INTENCAO_VOTO("Intenção de voto", true),
    REJEICAO("Rejeição", false),
    CONHECIMENTO("Conhecimento", false),
    SEGUNDO_TURNO("Segundo turno simulado", true),
    APROVACAO_GOVERNO("Aprovação de governo", false),
    OUTRA("Outra", false);

    private final String texto;
    private final boolean exigeCandidatos;

    TipoPesquisa(String texto, boolean exigeCandidatos) {
        this.texto = texto;
        this.exigeCandidatos = exigeCandidatos;
    }

    /** Regra de negócio: pesquisas de intenção de voto precisam de candidatos associados. */
    public boolean exigeCandidatos() {
        return exigeCandidatos;
    }

    @Override
    public String toString() {
        return texto;
    }
}
