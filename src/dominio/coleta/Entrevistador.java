package dominio.coleta;

import dominio.base.Pessoa;

public class Entrevistador extends Pessoa {
    private static final long serialVersionUID = 1L;

    public Entrevistador(int id, String nome) {
        super(id, nome);
    }

    @Override
    public void exibirDetalhes() {
        System.out.println("=== Detalhes do Entrevistador ===");
        System.out.println("ID: " + getId());
        System.out.println("Nome: " + getNome());
    }
}