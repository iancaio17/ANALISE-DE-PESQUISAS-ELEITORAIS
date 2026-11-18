package apresentacao.coleta;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/** Uma opção entre várias (botões de rádio). */
class CampoUnicaEscolha implements CampoResposta {

    private final ToggleGroup grupo = new ToggleGroup();
    private final VBox caixa = new VBox(6);

    CampoUnicaEscolha(List<String> opcoes) {
        for (String opcao : opcoes) {
            RadioButton radio = new RadioButton(opcao);
            radio.setToggleGroup(grupo);
            radio.setUserData(opcao);
            caixa.getChildren().add(radio);
        }
    }

    @Override
    public Node getNo() {
        return caixa;
    }

    @Override
    public List<String> getValores() {
        if (grupo.getSelectedToggle() == null) {
            return List.of();
        }
        return List.of(grupo.getSelectedToggle().getUserData().toString());
    }
}
