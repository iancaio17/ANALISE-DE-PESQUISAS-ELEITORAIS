package dominio.coleta;

import dominio.base.PerfilDemografico;
import dominio.base.Pessoa;
import infraestrutura.utils.Validacao;

/** Participante fictício de uma entrevista, com suas características de amostra. */
public class Participante extends Pessoa {

    private static final long serialVersionUID = 2L;

    private final PerfilDemografico perfilDemografico;

    public Participante(int id, String nome, PerfilDemografico perfilDemografico) {
        super(id, nome);
        Validacao.exigir(perfilDemografico != null, "Perfil demográfico obrigatório.");
        this.perfilDemografico = perfilDemografico;
    }

    public PerfilDemografico getPerfilDemografico() {
        return perfilDemografico;
    }

    @Override
    public String descrever() {
        return "PT" + getId() + " · " + getNome();
    }
}
