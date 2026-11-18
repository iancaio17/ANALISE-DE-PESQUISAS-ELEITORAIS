package dominio.coleta;

import dominio.pesquisa.Pergunta;
import dominio.pesquisa.TipoPergunta;
import infraestrutura.utils.Validacao;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** Resposta de um participante a uma pergunta (uma ou várias opções, conforme o tipo da pergunta). */
public final class Resposta implements Serializable {

    private static final long serialVersionUID = 2L;

    private final Pergunta pergunta;
    private final List<String> opcoes;

    public Resposta(Pergunta pergunta, String opcao) {
        this(pergunta, Collections.singletonList(opcao));
    }

    public Resposta(Pergunta pergunta, List<String> opcoes) {
        Validacao.exigir(pergunta != null, "Pergunta obrigatória.");
        Validacao.exigir(opcoes != null && !opcoes.isEmpty(), "Selecione uma resposta.");
        this.pergunta = pergunta;
        this.opcoes = opcoes.stream()
                .map(o -> RespostaEspecial.padronizar(Validacao.texto(o, "Resposta")))
                .toList();
        validar();
    }

    private void validar() {
        boolean semRepeticao = opcoes.stream().map(Validacao::chave).distinct().count() == opcoes.size();
        Validacao.exigir(semRepeticao, "Resposta duplicada.");

        boolean varias = opcoes.size() > 1;
        Validacao.exigir(!varias || pergunta.getTipo() == TipoPergunta.MULTIPLA_ESCOLHA,
                "A pergunta aceita uma única resposta.");
        Validacao.exigir(!varias || opcoes.stream().noneMatch(RespostaEspecial::ehEspecial),
                "Uma resposta especial não pode ser combinada com outra opção.");

        for (String opcao : opcoes) {
            boolean valida = RespostaEspecial.ehEspecial(opcao)
                    || pergunta.getTipo() == TipoPergunta.DISCURSIVA
                    || pergunta.possuiOpcao(opcao);
            Validacao.exigir(valida, "Opção inválida: " + opcao);
        }
    }

    public Pergunta getPergunta() {
        return pergunta;
    }

    public List<String> getOpcoesEscolhidas() {
        return opcoes;
    }

    /** Opções escolhidas em um único texto, separadas por " / ". */
    public String getOpcaoEscolhida() {
        return String.join(" / ", opcoes);
    }

    public boolean isRespostaEspecial() {
        return opcoes.size() == 1 && RespostaEspecial.ehEspecial(opcoes.get(0));
    }

    /** Indica se a resposta pertence à pergunta informada. */
    public boolean responde(Pergunta outra) {
        return pergunta.getId().equals(outra.getId());
    }
}
