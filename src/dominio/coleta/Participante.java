package dominio.coleta;

import dominio.base.Pessoa;

public class Participante extends Pessoa {

    private PerfilDemografico perfilDemografico;

    public Participante(int id, String nome, PerfilDemografico perfilDemografico) {
        super(id, nome);
        if (perfilDemografico == null) {
            throw new IllegalArgumentException("O perfil demográfico do participante é obrigatório.");
        }

        this.perfilDemografico = perfilDemografico;
    }

    public PerfilDemografico getPerfilDemografico() {
        return perfilDemografico;
    }

    public void setPerfilDemografico(PerfilDemografico perfilDemografico) {
        if (perfilDemografico == null) {
            throw new IllegalArgumentException("O perfil demográfico do participante é obrigatório.");
        }
        this.perfilDemografico = perfilDemografico;
    }

    @Override
    public void exibirDetalhes() {
        System.out.println("=== Detalhes do Participante ===");
        System.out.println("ID: " + getId());
        System.out.println("Nome: " + getNome());

        System.out.println("Perfil: " + perfilDemografico.exibirPerfil());
    }
}