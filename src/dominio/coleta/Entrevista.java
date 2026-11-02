package dominio.coleta;

import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import infraestrutura.excecoes.RegraNegocioException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Entrevista implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idUnico;
    private Pesquisa pesquisa;
    private Participante participante;
    private Entrevistador entrevistador;

    private List<Resposta> respostas;
    private boolean concluida;

    public Entrevista(int idUnico, Pesquisa pesquisa, Participante participante, Entrevistador entrevistador) {
        if (idUnico <= 0) {
            throw new RegraNegocioException("O identificador único da entrevista deve ser positivo.");
        }
        if (pesquisa == null || participante == null || entrevistador == null) {
            throw new RegraNegocioException("Pesquisa, Participante e Entrevistador são obrigatórios para criar uma entrevista.");
        }

        this.idUnico = idUnico;
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
    public List<Resposta> getRespostas() { return Collections.unmodifiableList(respostas); }
    public boolean isConcluida() { return concluida; }

    public void registrarResposta(Resposta resposta) {
        if (concluida) {
            throw new RegraNegocioException("Erro: Uma entrevista concluída não pode ser alterada.");
        }
        if (resposta == null) {
            throw new RegraNegocioException("A resposta não pode ser nula.");
        }

        Pergunta perguntaDaResposta = resposta.getPergunta();


        boolean perguntaPertenceAPesquisa = pesquisa.getPerguntas().stream()
                .anyMatch(p -> p.getTexto().equalsIgnoreCase(perguntaDaResposta.getTexto()));
        if (!perguntaPertenceAPesquisa && !pesquisa.getPerguntas().isEmpty()) {
            throw new RegraNegocioException("A pergunta '" + perguntaDaResposta.getTexto() + "' não pertence a esta pesquisa.");
        }


        boolean perguntaJaRespondida = respostas.stream()
                .anyMatch(r -> r.getPergunta().getTexto().equalsIgnoreCase(perguntaDaResposta.getTexto()));
        if (perguntaJaRespondida) {
            throw new RegraNegocioException("A pergunta '" + perguntaDaResposta.getTexto() + "' já foi respondida nesta entrevista.");
        }


        if (!resposta.isRespostaEspecial() && !perguntaDaResposta.getOpcoes().isEmpty()) {
            boolean opcaoValida = perguntaDaResposta.getOpcoes().stream()
                    .map(OpcaoResposta::getTextoOpcao)
                    .anyMatch(texto -> texto.equalsIgnoreCase(resposta.getOpcaoEscolhida()));
            if (!opcaoValida) {
                throw new RegraNegocioException("A opção '" + resposta.getOpcaoEscolhida() + "' não é válida para a pergunta informada.");
            }
        }

        this.respostas.add(resposta);
    }

    public void concluirEntrevista() {
        if (concluida) {
            throw new RegraNegocioException("Esta entrevista já se encontra concluída.");
        }

        if (!pesquisa.getPerguntas().isEmpty() && respostas.size() < pesquisa.getPerguntas().size()) {
            throw new RegraNegocioException("Não é possível concluir a entrevista: todas as perguntas obrigatórias da pesquisa devem ser respondidas.");
        }
        this.concluida = true;
    }
}