package apresentacao.coleta;

import dominio.coleta.RespostaEspecial;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/**
 * Várias opções (caixas de seleção). Uma resposta especial não pode ser combinada
 * com outras opções: marcar uma desmarca as demais.
 */
class CampoMultiplaEscolha implements CampoResposta {

    private final List<CheckBox> caixas = new ArrayList<>();
    private final VBox caixa = new VBox(6);

    CampoMultiplaEscolha(List<String> opcoes) {
        for (String opcao : opcoes) {
            CheckBox selecao = new CheckBox(opcao);
            selecao.setOnAction(evento -> desmarcarIncompativeis(selecao));
            caixas.add(selecao);
            caixa.getChildren().add(selecao);
        }
    }

    private void desmarcarIncompativeis(CheckBox marcada) {
        if (!marcada.isSelected()) {
            return;
        }
        for (CheckBox outra : caixas) {
            boolean algumaEspecial = RespostaEspecial.ehEspecial(marcada.getText())
                    || RespostaEspecial.ehEspecial(outra.getText());
            if (outra != marcada && algumaEspecial) {
                outra.setSelected(false);
            }
        }
    }

    @Override
    public Node getNo() {
        return caixa;
    }

    @Override
    public List<String> getValores() {
        return caixas.stream().filter(CheckBox::isSelected).map(CheckBox::getText).toList();
    }
}
