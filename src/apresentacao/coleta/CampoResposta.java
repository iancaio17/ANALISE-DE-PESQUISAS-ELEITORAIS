package apresentacao.coleta;

import dominio.coleta.RespostaEspecial;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.TipoPergunta;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;

/**
 * Campo de tela que recebe a resposta de uma pergunta. Cada tipo de pergunta tem uma
 * implementação (polimorfismo): o questionário não precisa saber como cada campo funciona.
 */
interface CampoResposta {

    /** Componente visual que será colocado no questionário. */
    Node getNo();

    /** Valores informados; lista vazia quando a pergunta ficou sem resposta. */
    List<String> getValores();

    /** Escolhe o campo adequado ao tipo da pergunta. */
    static CampoResposta para(Pergunta pergunta) {
        if (pergunta.getTipo() == TipoPergunta.UNICA_ESCOLHA) {
            return new CampoUnicaEscolha(opcoesComEspeciais(pergunta));
        }
        if (pergunta.getTipo() == TipoPergunta.MULTIPLA_ESCOLHA) {
            return new CampoMultiplaEscolha(opcoesComEspeciais(pergunta));
        }
        return new CampoDiscursivo();
    }

    /** Opções da pergunta seguidas das respostas especiais, que existem em toda pergunta objetiva. */
    private static List<String> opcoesComEspeciais(Pergunta pergunta) {
        List<String> opcoes = new ArrayList<>(pergunta.getOpcoes().stream().map(o -> o.getTexto()).toList());
        for (RespostaEspecial especial : RespostaEspecial.values()) {
            if (!opcoes.contains(especial.getTexto())) {
                opcoes.add(especial.getTexto());
            }
        }
        return opcoes;
    }
}
