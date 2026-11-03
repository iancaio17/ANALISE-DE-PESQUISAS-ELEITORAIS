package dominio.pesquisa;

import infraestrutura.excecoes.PesquisaRegraNegocioException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pergunta {
    private final String id;
    private String texto;
    private TipoPergunta tipo;
    private final List<OpcaoResposta> opcoes;

    public Pergunta(String texto, TipoPergunta tipo) {
        this.id = UUID.randomUUID().toString();
        this.texto = texto;
        this.tipo = tipo;
        this.opcoes = new ArrayList<>();
    }

    public void adicionarOpcao(OpcaoResposta opcao) {
        if (this.opcoes.stream().anyMatch(o -> o.getTexto().equalsIgnoreCase(opcao.getTexto()))) {
            throw new PesquisaRegraNegocioException("A opção de resposta já existe nesta pergunta.");
        }
        this.opcoes.add(opcao);
    }

    public void removerOpcao(String idOpcao) {
        this.opcoes.removeIf(o -> o.getId().equals(idOpcao));
    }

    public String getId() { return id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public TipoPergunta getTipo() { return tipo; }
    public void setTipo(TipoPergunta tipo) { this.tipo = tipo; }
    public List<OpcaoResposta> getOpcoes() { return List.copyOf(opcoes); }
}

enum TipoPergunta { MULTIPLA_ESCOLHA, UNICA_ESCOLHA, DISCURSIVA }