package dominio.candidato;

import dominio.base.Pessoa;

public class Candidato extends Pessoa {
    private int numeroIdentificador;
    private String cargo;
    private String situacao;
    private Partido partido;

    public Candidato(int id, String nome, int numeroIdentificador, String cargo, String situacao) {
        super(id, nome);
        this.numeroIdentificador = numeroIdentificador;
        this.cargo = cargo;
        this.situacao = situacao;
    }

    public int getNumeroIdentificador() { return numeroIdentificador; }
    public void setNumeroIdentificador(int numeroIdentificador) { this.numeroIdentificador = numeroIdentificador; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public Partido getPartido() { return partido; }
    public void setPartido(Partido partido) { this.partido = partido; }

    @Override
    public void exibirDetalhes() {
        System.out.println("=== Detalhes do Candidato ===");
        System.out.println("ID: " + getId());
        System.out.println("Nome: " + getNome());
        System.out.println("Número: " + numeroIdentificador);
        System.out.println("Cargo: " + cargo);
        System.out.println("Situação: " + situacao);

        if (partido != null) {
            System.out.println("Partido: " + partido.getSigla() + " - " + partido.getNumero());
        } else {
            System.out.println("Partido: Nenhum associado");
        }
    }
}