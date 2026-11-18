package apresentacao.coleta;

import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.texto;
import static apresentacao.componentes.Componentes.titulo;

import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.VBox;

/** Formulário com um campo para cada pergunta da pesquisa. */
class QuestionarioEntrevista extends VBox {

    private final Map<String, CampoResposta> campos = new LinkedHashMap<>();

    QuestionarioEntrevista(Pesquisa pesquisa) {
        super(16);
        for (Pergunta pergunta : pesquisa.getPerguntas()) {
            CampoResposta campo = CampoResposta.para(pergunta);
            campos.put(pergunta.getId(), campo);
            String sufixo = pergunta.isObrigatoria() ? " *" : " (opcional)";
            getChildren().add(painel(titulo(pergunta.getTexto() + sufixo), texto(pergunta.getTipo().toString()),
                    campo.getNo()));
        }
    }

    /** Respostas digitadas, por identificador de pergunta. */
    Map<String, List<String>> coletarRespostas() {
        Map<String, List<String>> respostas = new LinkedHashMap<>();
        campos.forEach((id, campo) -> respostas.put(id, campo.getValores()));
        return respostas;
    }
}
