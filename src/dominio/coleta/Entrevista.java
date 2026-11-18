package dominio.coleta;

import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import infraestrutura.utils.Validacao;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Entrevista de um participante em uma pesquisa. Depois de concluída, não pode mais ser alterada. */
public class Entrevista implements Serializable {

    private static final long serialVersionUID = 2L;

    private final int idUnico;
    private final Pesquisa pesquisa;
    private final Participante participante;
    private final Entrevistador entrevistador;
    private final List<Resposta> respostas = new ArrayList<>();
    private boolean concluida;
    private LocalDateTime data;

    public Entrevista(int idUnico, Pesquisa pesquisa, Participante participante, Entrevistador entrevistador) {
        Validacao.exigir(pesquisa != null && participante != null && entrevistador != null,
                "Pesquisa, participante e entrevistador são obrigatórios.");
        this.idUnico = Validacao.positivo(idUnico, "Número da entrevista");
        this.pesquisa = pesquisa;
        this.participante = participante;
        this.entrevistador = entrevistador;
    }

    public void registrarResposta(Resposta resposta) {
        Validacao.exigir(!concluida, "Uma entrevista concluída não pode ser alterada.");
        Validacao.exigir(resposta != null, "Resposta obrigatória.");
        boolean perguntaDaPesquisa = pesquisa.getPerguntas().stream().anyMatch(resposta::responde);
        Validacao.exigir(perguntaDaPesquisa, "Pergunta não pertence à pesquisa.");
        Validacao.exigir(buscarResposta(resposta.getPergunta()).isEmpty(), "Pergunta já respondida.");
        respostas.add(resposta);
    }

    /** Conclui a entrevista, exigindo uma resposta para cada pergunta obrigatória. */
    public void concluirEntrevista() {
        Validacao.exigir(!concluida, "Entrevista já concluída.");
        Validacao.exigir(!pesquisa.getPerguntas().isEmpty(), "Pesquisa sem perguntas.");
        for (Pergunta pergunta : pesquisa.getPerguntas()) {
            boolean respondida = buscarResposta(pergunta).isPresent();
            Validacao.exigir(!pergunta.isObrigatoria() || respondida, "Responda todas as perguntas obrigatórias.");
        }
        concluida = true;
        data = LocalDateTime.now();
    }

    /** Resposta dada a uma pergunta, se existir. */
    public Optional<Resposta> buscarResposta(Pergunta pergunta) {
        return respostas.stream().filter(r -> r.responde(pergunta)).findFirst();
    }

    public int getIdUnico() {
        return idUnico;
    }

    public Pesquisa getPesquisa() {
        return pesquisa;
    }

    public Participante getParticipante() {
        return participante;
    }

    public Entrevistador getEntrevistador() {
        return entrevistador;
    }

    public List<Resposta> getRespostas() {
        return List.copyOf(respostas);
    }

    public boolean isConcluida() {
        return concluida;
    }

    public LocalDateTime getData() {
        return data;
    }
}
