package dominio.candidato;

import dominio.base.Pessoa;
import dominio.pesquisa.Cargo;
import infraestrutura.utils.Validacao;

/** Candidato fictício, com número, cargo, situação e (opcionalmente) um partido. */
public class Candidato extends Pessoa {

    private static final long serialVersionUID = 3L;

    private int numeroIdentificador;
    private Cargo cargo;
    private SituacaoCandidatura situacao;
    private Partido partido;

    public Candidato(int id, String nome, int numeroIdentificador, Cargo cargo, SituacaoCandidatura situacao) {
        super(id, nome);
        setNumeroIdentificador(numeroIdentificador);
        setCargo(cargo);
        setSituacao(situacao);
    }

    public int getNumeroIdentificador() {
        return numeroIdentificador;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public SituacaoCandidatura getSituacao() {
        return situacao;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setNumeroIdentificador(int numero) {
        this.numeroIdentificador = Validacao.positivo(numero, "Número do candidato");
    }

    public void setCargo(Cargo cargo) {
        Validacao.exigir(cargo != null, "O cargo do candidato é obrigatório.");
        this.cargo = cargo;
    }

    public void setSituacao(SituacaoCandidatura situacao) {
        Validacao.exigir(situacao != null, "Situação da candidatura inválida.");
        this.situacao = situacao;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public boolean pertenceAoPartido(String idPartido) {
        return partido != null && partido.getId().equals(idPartido);
    }

    /** Candidatura permitida em pesquisa: situação adequada e, se houver partido, partido ativo. */
    public boolean podeParticiparDePesquisa() {
        boolean partidoAceita = partido == null || partido.isAtivo();
        return situacao.podeParticiparDePesquisa() && partidoAceita;
    }

    public boolean isAtivo() {
        return situacao.isAtiva();
    }

    @Override
    public String descrever() {
        return getNome() + " · " + numeroIdentificador;
    }
}
