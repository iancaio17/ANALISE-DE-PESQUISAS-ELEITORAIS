package apresentacao;

import aplicacao.SistemaContexto;
import apresentacao.componentes.Dialogos;
import java.util.Locale;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Aplicação JavaFX: abre a janela, mostra o login e depois a tela principal. */
public class SapeApp extends Application {

    private static final String FOLHA_DE_ESTILO = "/apresentacao/sape.css";

    private SistemaContexto contexto;
    private Stage palco;

    @Override
    public void start(Stage stage) {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        palco = stage;
        stage.setTitle("SAPE · Pesquisas eleitorais simuladas");
        stage.setMinWidth(1040);
        stage.setMinHeight(700);
        try {
            contexto = new SistemaContexto();
            mostrarLogin();
            stage.show();
        } catch (Exception e) {
            Dialogos.erro(e);
            Platform.exit();
        }
    }

    private void mostrarLogin() {
        trocarTela(new TelaLogin(contexto, () -> trocarTela(new TelaPrincipal(contexto, this::mostrarLogin))));
    }

    private void trocarTela(Parent raiz) {
        Scene cena = new Scene(raiz, 1280, 800);
        cena.getStylesheets().add(SapeApp.class.getResource(FOLHA_DE_ESTILO).toExternalForm());
        palco.setScene(cena);
    }

    @Override
    public void stop() {
        if (contexto != null) {
            contexto.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
