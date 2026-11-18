package apresentacao;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.texto;
import static apresentacao.componentes.Componentes.titulo;

import aplicacao.SistemaContexto;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Tela de entrada: e-mail e senha. */
class TelaLogin extends HBox {

    private final SistemaContexto contexto;
    private final Runnable aoEntrar;

    private final TextField email = entrada("seu.email@simulado.local");
    private final PasswordField senha = new PasswordField();
    private final TextField senhaVisivel = entrada("Sua senha");
    private final Label erro = texto("");

    TelaLogin(SistemaContexto contexto, Runnable aoEntrar) {
        this.contexto = contexto;
        this.aoEntrar = aoEntrar;
        StackPane area = new StackPane(montarCartao());
        area.setPadding(new Insets(36));
        HBox.setHgrow(area, Priority.ALWAYS);
        getChildren().addAll(montarLateral(), area);
    }

    private VBox montarLateral() {
        Label marca = new Label("Pesquisas\neleitorais");
        marca.getStyleClass().add("login-brand");
        Label subtitulo = texto("Organize a coleta.\nTransforme respostas em resultados.");
        subtitulo.getStyleClass().add("login-subtitle");
        Label nota = texto("Sistema acadêmico simulado\nTodos os dados devem ser fictícios.");
        nota.getStyleClass().add("sidebar-note");

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        VBox lateral = new VBox(28, marca, subtitulo, espaco, nota);
        lateral.setPadding(new Insets(60));
        lateral.setPrefWidth(510);
        lateral.getStyleClass().add("sidebar");
        return lateral;
    }

    private VBox montarCartao() {
        email.setId("login-email");
        senha.setPromptText("Sua senha");
        senha.setId("login-senha");
        senhaVisivel.textProperty().bindBidirectional(senha.textProperty());
        alternarSenhaVisivel(false);

        CheckBox mostrar = new CheckBox("Mostrar senha");
        mostrar.setOnAction(evento -> alternarSenhaVisivel(mostrar.isSelected()));

        erro.getStyleClass().add("error-label");
        erro.setId("login-erro");

        Button entrar = new Button("Entrar no sistema");
        entrar.setId("login-entrar");
        entrar.setDefaultButton(true);
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.setOnAction(evento -> entrar());

        VBox cartao = painel(titulo("Bem-vindo ao SAPE"), texto("Entre para acessar suas pesquisas e análises."),
                campo("E-mail", email), campo("Senha", new VBox(0, senha, senhaVisivel)), mostrar, erro, entrar,
                aviso("Primeiro acesso\nadmin@simulado.local  ·  Admin12345\n"
                        + "Altere a senha em Administração após entrar."));
        cartao.setMaxWidth(440);
        cartao.setMaxHeight(Region.USE_PREF_SIZE);
        return cartao;
    }

    private void alternarSenhaVisivel(boolean visivel) {
        senhaVisivel.setManaged(visivel);
        senhaVisivel.setVisible(visivel);
        senha.setManaged(!visivel);
        senha.setVisible(!visivel);
    }

    private void entrar() {
        try {
            contexto.getAdministracao().autenticar(email.getText(), senha.getText());
            aoEntrar.run();
        } catch (Exception e) {
            erro.setText(e.getMessage());
        }
    }
}
