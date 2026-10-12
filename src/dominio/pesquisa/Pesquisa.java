package dominio.pesquisa;

import dominio.candidato.Candidato;
import java.util.ArrayList;
import java.util.List;

public class Pesquisa {
    private String codigo;
    private String nome;
    private String cargo;
    private String tipo;
    private String situacao;
    private int quantidadePrevista;

    private PlanoAmostral planoAmostral;
    private List<Pergunta> perguntas;
    private List<Candidato> candidatosAssociados;

    public Pesquisa(String codigo, String nome, String cargo, String tipo, int quantidadePrevista, PlanoAmostral planoAmostral) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargo = cargo;
        this.tipo = tipo;
        this.quantidadePrevista = quantidadePrevista;
        this.situacao = "Planejada"; // Toda pesquisa nasce como planejada

        if (planoAmostral == null) {
            throw new IllegalArgumentException("Toda pesquisa precisa de um plano amostral definido.");
        }
        this.planoAmostral = planoAmostral;
        this.perguntas = new ArrayList<>();
        this.candidatosAssociados = new ArrayList<>();
    }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getCargo() { return cargo; }
    public String getTipo() { return tipo; }
    public String getSituacao() { return situacao; }
    public int getQuantidadePrevista() { return quantidadePrevista; }
    public PlanoAmostral getPlanoAmostral() { return planoAmostral; }
    public List<Pergunta> getPerguntas() { return perguntas; }
    public List<Candidato> getCandidatosAssociados() { return candidatosAssociados; }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public void associarCandidato(Candidato candidato) {
        if (candidato != null && candidato.getCargo().equals(this.cargo) && !candidatosAssociados.contains(candidato)) {
            candidatosAssociados.add(candidato);
        }
    }

    public void adicionarPergunta(Pergunta pergunta) {
        if (pergunta != null) {
            this.perguntas.add(pergunta);
        }
    }

    public void iniciarColeta() {
        if (this.situacao.equals("Planejada")) {
            this.situacao = "Em coleta";
        }
    }

    public void encerrarColeta() {
        if (this.situacao.equals("Em coleta")) {
            this.situacao = "Encerrada";
        }
    }
}