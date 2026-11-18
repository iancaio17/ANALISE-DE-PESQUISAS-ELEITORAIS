package dominio.analise;

import dominio.base.PerfilDemografico;
import dominio.coleta.Entrevista;
import java.util.function.Function;

/** Critérios simples de agrupamento, baseados no perfil demográfico do participante. */
public enum CriterioDemografico implements CriterioAgrupamento {
    REGIAO("Região", PerfilDemografico::getRegiao),
    FAIXA_ETARIA("Faixa etária", PerfilDemografico::getFaixaEtaria),
    ESCOLARIDADE("Escolaridade", PerfilDemografico::getEscolaridade),
    FAIXA_RENDA("Faixa de renda", PerfilDemografico::getFaixaRenda),
    GENERO("Gênero", PerfilDemografico::getGenero);

    private static final String NAO_INFORMADO = "Não informado";

    private final String nome;
    private final Function<PerfilDemografico, String> extrator;

    CriterioDemografico(String nome, Function<PerfilDemografico, String> extrator) {
        this.nome = nome;
        this.extrator = extrator;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public String grupoDe(Entrevista entrevista) {
        String valor = extrator.apply(entrevista.getParticipante().getPerfilDemografico());
        return valor == null || valor.isBlank() ? NAO_INFORMADO : valor.trim();
    }
}
