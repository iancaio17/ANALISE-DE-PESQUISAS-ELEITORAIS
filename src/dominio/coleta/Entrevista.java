package dominio.coleta;

import dominio.pesquisa.Pesquisa;
import java.util.ArrayList;
import java.util.List;

public class Entrevista {
    private int idUnico;
    private Pesquisa pesquisa;
    private Participante participante;
    private Entrevistador entrevistador;

    private List<Resposta> respostas;
    private boolean concluida;

    public Entrevista(int idUnico, Pesquisa pesquisa, Participante participante, Entrevistador entrevistador) {
        this.idUnico = idUnico;

        if (pesquisa == null || participante == null || entrevistador == null) {
            throw new IllegalArgumentException("Pesquisa, Participante e Entrevistador são obrigatórios.");
        }

        this.pesquisa = pesquisa;
        this.participante = participante;
        this.entrevistador = entrevistador;
        this.respostas = new ArrayList<>();
        this.concluida = false;
    }

    public int getIdUnico() { return idUnico; }
    public Pesquisa getPesquisa() { return pesquisa; }
    public Participante getParticipante() { return participante; }
    public Entrevistador getEntrevistador() { return entrevistador; }
    public List<Resposta> getRespostas() { return respostas; }
    public boolean isConcluida() { return concluida; }

    public void registrarResposta(Resposta resposta) {
        if (concluida) {
            throw new IllegalStateException("Esta entrevista já foi concluída e não pode receber novas respostas.");
        }
        if (resposta != null) {
            this.respostas.add(resposta);
        }
    }

    public void concluirEntrevista() {
        this.concluida = true;
    }
}