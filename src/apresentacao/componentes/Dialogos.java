package apresentacao.componentes;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import javafx.stage.Window;

/** Janelas de aviso, confirmação e formulário usadas por todas as telas. */
public final class Dialogos {

    private static final String FOLHA_DE_ESTILO = "/apresentacao/sape.css";

    private Dialogos() {
    }

    /** Executa a ação mostrando qualquer erro em uma janela, sem derrubar a aplicação. */
    public static void executar(Runnable acao) {
        try {
            acao.run();
        } catch (Exception e) {
            erro(e);
        }
    }

    public static void erro(Throwable erro) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Não foi possível concluir");
        alerta.setHeaderText("Confira os dados e tente novamente");
        String mensagem = erro.getMessage();
        alerta.setContentText(mensagem == null ? erro.getClass().getSimpleName() : mensagem);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alerta.showAndWait();
    }

    public static boolean confirmar(String texto) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar operação");
        alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static void sucesso(String texto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, texto, ButtonType.OK);
        alerta.setTitle("Operação concluída");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    public static String pedir(Window dono, String titulo, String valorInicial) {
        TextInputDialog dialogo = new TextInputDialog(valorInicial);
        dialogo.initOwner(dono);
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(titulo);
        return dialogo.showAndWait().orElse(null);
    }

    /** Abre um formulário; se {@code salvar} lançar erro, a janela continua aberta. */
    public static void formulario(Window dono, String titulo, Node conteudo, Runnable salvar) {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        dialogo.initOwner(dono);
        dialogo.setResizable(true);
        ButtonType salvarBotao = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelarBotao = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(salvarBotao, cancelarBotao);

        ScrollPane rolagem = new ScrollPane(conteudo);
        rolagem.setFitToWidth(true);
        rolagem.setPrefViewportWidth(660);
        rolagem.setMaxHeight(570);
        dialogo.getDialogPane().setContent(rolagem);
        dialogo.getDialogPane().getStylesheets().add(Dialogos.class.getResource(FOLHA_DE_ESTILO).toExternalForm());

        dialogo.getDialogPane().lookupButton(salvarBotao).addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                salvar.run();
            } catch (Exception e) {
                evento.consume();
                erro(e);
            }
        });
        dialogo.showAndWait();
    }

    /** Abre uma janela apenas de leitura, com botão Fechar. */
    public static void visualizar(Window dono, String titulo, Node conteudo) {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        dialogo.initOwner(dono);
        dialogo.setResizable(true);
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialogo.showAndWait();
    }
}
