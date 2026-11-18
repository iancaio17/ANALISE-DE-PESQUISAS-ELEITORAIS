package apresentacao.coleta;

import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.secundario;

import dominio.coleta.RespostaEspecial;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/** Texto livre, com a opção de marcar uma resposta especial no lugar do texto. */
class CampoDiscursivo implements CampoResposta {

    private final TextArea texto = new TextArea();
    private final ComboBox<String> especial;
    private final VBox caixa;

    CampoDiscursivo() {
        texto.setPromptText("Resposta fictícia");
        texto.setPrefRowCount(3);
        especial = combo(Arrays.stream(RespostaEspecial.values()).map(RespostaEspecial::getTexto).toList(),
                "Ou selecione uma resposta especial");
        especial.setOnAction(evento -> texto.setDisable(especial.getValue() != null));
        caixa = new VBox(6, texto, linha(especial, secundario("Limpar resposta especial", this::limparEspecial)));
    }

    private void limparEspecial() {
        especial.setValue(null);
        texto.setDisable(false);
    }

    @Override
    public Node getNo() {
        return caixa;
    }

    @Override
    public List<String> getValores() {
        if (especial.getValue() != null) {
            return List.of(especial.getValue());
        }
        return texto.getText().isBlank() ? List.of() : List.of(texto.getText());
    }
}
