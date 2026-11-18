package dominio.pesquisa;

import infraestrutura.utils.Validacao;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Pergunta de um questionário, com tipo, obrigatoriedade e opções de resposta. */
public class Pergunta implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String id;
    private final String texto;
    private final TipoPergunta tipo;
    private final boolean obrigatoria;
    private final List<OpcaoResposta> opcoes = new ArrayList<>();

    /** Cria uma pergunta obrigatória com identificador gerado automaticamente. */
    public Pergunta(String texto, TipoPergunta tipo) {
        this(UUID.randomUUID().toString(), texto, tipo, true);
    }

    public Pergunta(String id, String texto, TipoPergunta tipo, boolean obrigatoria) {
        Validacao.exigir(tipo != null, "Selecione o tipo da pergunta.");
        this.id = Validacao.texto(id, "Identificador da pergunta");
        this.texto = Validacao.texto(texto, "Texto da pergunta");
        this.tipo = tipo;
        this.obrigatoria = obrigatoria;
    }

    public void adicionarOpcao(OpcaoResposta opcao) {
        Validacao.exigir(opcao != null, "Opção obrigatória.");
        boolean duplicada = opcoes.stream().anyMatch(o -> o.getTexto().equalsIgnoreCase(opcao.getTexto()));
        Validacao.exigir(!duplicada, "Opção duplicada.");
        opcoes.add(opcao);
    }

    /** Indica se o texto é uma das opções cadastradas (ignora maiúsculas e minúsculas). */
    public boolean possuiOpcao(String textoOpcao) {
        return opcoes.stream().anyMatch(o -> o.getTexto().equalsIgnoreCase(textoOpcao));
    }

    public String getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public TipoPergunta getTipo() {
        return tipo;
    }

    public boolean isObrigatoria() {
        return obrigatoria;
    }

    public List<OpcaoResposta> getOpcoes() {
        return List.copyOf(opcoes);
    }

    @Override
    public String toString() {
        return texto;
    }
}
