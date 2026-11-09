package dominio.analise;

import dominio.coleta.Entrevista;

/** Critérios simples baseados no PerfilDemografico do participante. */
public enum CriterioDemografico implements CriterioAgrupamento {
    REGIAO("Região"),
    FAIXA_ETARIA("Faixa etária"),
    ESCOLARIDADE("Escolaridade"),
    FAIXA_RENDA("Faixa de renda");

    private static final String NAO_INFORMADO = "Não informado";
    private final String nome;

    CriterioDemografico(String nome) { this.nome = nome; }

    @Override
    public String getNome() { return nome; }

    @Override
    public String grupoDe(Entrevista entrevista) {
        PerfilDemografico p = entrevista.getParticipante().getPerfilDemografico();
        String valor;
        switch (this) {
            case REGIAO:       valor = p.getRegiao(); break;
            case FAIXA_ETARIA: valor = p.getFaixaEtaria(); break;
            case ESCOLARIDADE: valor = p.getEscolaridade(); break;
            default:           valor = p.getFaixaRenda(); break;
        }
        return (valor == null || valor.trim().isEmpty()) ? NAO_INFORMADO : valor.trim();
    }
}
