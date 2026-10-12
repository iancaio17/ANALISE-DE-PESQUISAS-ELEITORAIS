package dominio.candidato;

import java.util.ArrayList;
import java.util.List;

public class Partido {
    private String nome;
    private String sigla;
    private int numero;
    private String situacao;

    private List<Candidato> candidatos;

    public Partido(String nome, String sigla, int numero, String situacao) {
        this.nome = nome;
        this.sigla = sigla;
        this.numero = numero;
        this.situacao = situacao;
        this.candidatos = new ArrayList<>();
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public List<Candidato> getCandidatos() { return candidatos; }

    public void adicionarCandidato(Candidato candidato) {
        if (candidato != null && !this.candidatos.contains(candidato)) {
            this.candidatos.add(candidato);
        }
    }
}