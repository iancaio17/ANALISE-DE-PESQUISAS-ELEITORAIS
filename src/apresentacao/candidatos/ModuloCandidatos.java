package apresentacao.candidatos;

import aplicacao.SistemaContexto;
import apresentacao.base.ModuloView;

/** Módulo Candidatos e Partidos. Todos consultam; somente o administrador altera. */
public class ModuloCandidatos extends ModuloView {

    public ModuloCandidatos(SistemaContexto contexto) {
        super(contexto);
        adicionar("Candidatos", () -> new AbaCandidatos(this));
        adicionar("Partidos", () -> new AbaPartidos(this));
        adicionar("Consulta de candidatos", () -> new AbaConsultaCandidatos(this));
        adicionar("Indicadores", () -> new AbaIndicadores(this));
    }
}
