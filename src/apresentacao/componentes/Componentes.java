package apresentacao.componentes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Fábrica dos componentes visuais padronizados do sistema. */
public final class Componentes {

    private Componentes() {
    }

    // ------------------------------------------------------------------ textos

    public static Label titulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("section-title");
        return rotulo;
    }

    public static Label texto(String texto) {
        Label rotulo = new Label(texto);
        rotulo.setWrapText(true);
        rotulo.setMinHeight(Region.USE_PREF_SIZE);
        return rotulo;
    }

    public static Label aviso(String texto) {
        Label rotulo = texto(texto);
        rotulo.getStyleClass().add("notice");
        rotulo.setMaxWidth(Double.MAX_VALUE);
        return rotulo;
    }

    // ------------------------------------------------------------------ campos

    public static TextField entrada(String dica) {
        TextField campo = new TextField();
        campo.setPromptText(dica);
        campo.setPrefWidth(210);
        return campo;
    }

    /** Campo numérico que mostra um código gerado pelo sistema e não pode ser editado. */
    public static TextField codigo(int valor) {
        TextField campo = entrada("");
        campo.setText(String.valueOf(valor));
        campo.setEditable(false);
        return campo;
    }

    public static <T> ComboBox<T> combo(Collection<T> opcoes, String dica) {
        ComboBox<T> combo = new ComboBox<>(FXCollections.observableArrayList(opcoes));
        combo.setPromptText(dica);
        combo.setPrefWidth(220);
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    public static <T> ComboBox<T> combo(T[] opcoes, String dica) {
        return combo(Arrays.asList(opcoes), dica);
    }

    /** Combo com a opção "Todas" selecionada, usado nos filtros de consulta. */
    public static ComboBox<String> filtro(Collection<String> valores) {
        ComboBox<String> combo = combo(valores, Leitura.TODAS);
        combo.getItems().add(0, Leitura.TODAS);
        combo.setValue(Leitura.TODAS);
        return combo;
    }

    // ------------------------------------------------------------------ botões

    public static Button botao(String texto, Runnable acao) {
        Button botao = new Button(texto);
        botao.setOnAction(evento -> Dialogos.executar(acao));
        return botao;
    }

    public static Button secundario(String texto, Runnable acao) {
        Button botao = botao(texto, acao);
        botao.getStyleClass().add("secondary");
        return botao;
    }

    public static Button perigo(String texto, Runnable acao) {
        Button botao = botao(texto, acao);
        botao.getStyleClass().add("danger");
        return botao;
    }

    // ------------------------------------------------------------------ layouts

    public static VBox campo(String titulo, Node controle) {
        VBox caixa = new VBox(6, new Label(titulo), controle);
        caixa.getStyleClass().add("field");
        return caixa;
    }

    public static FlowPane linha(Node... nos) {
        FlowPane linha = new FlowPane(12, 12, nos);
        linha.setAlignment(Pos.BOTTOM_LEFT);
        return linha;
    }

    public static VBox painel(Node... nos) {
        VBox painel = new VBox(14, nos);
        painel.getStyleClass().add("panel");
        return painel;
    }

    /** Coluna de conteúdo com margens, usada dentro de formulários e páginas. */
    public static VBox conteudo(Node... nos) {
        VBox coluna = new VBox(18, nos);
        coluna.setPadding(new Insets(24, 28, 28, 28));
        return coluna;
    }

    public static ScrollPane pagina(Node... nos) {
        ScrollPane rolagem = new ScrollPane(conteudo(nos));
        rolagem.setFitToWidth(true);
        rolagem.getStyleClass().add("page");
        return rolagem;
    }

    /** Grade de duas colunas para formulários. */
    public static GridPane grade() {
        GridPane grade = new GridPane();
        grade.setHgap(18);
        grade.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints coluna = new ColumnConstraints();
            coluna.setPercentWidth(50);
            grade.getColumnConstraints().add(coluna);
        }
        return grade;
    }

    public static void campo(GridPane grade, int linha, int coluna, String titulo, Node controle) {
        grade.add(campo(titulo, controle), coluna, linha);
        if (controle instanceof Region regiao) {
            regiao.setMaxWidth(Double.MAX_VALUE);
        }
    }

    public static VBox indicador(Object valor, String descricao) {
        Label numero = new Label(String.valueOf(valor));
        numero.getStyleClass().add("metric-number");
        VBox caixa = painel(numero, texto(descricao));
        caixa.setMinSize(180, 126);
        caixa.setPrefSize(180, 126);
        caixa.setMaxWidth(180);
        return caixa;
    }

    // ------------------------------------------------------------------ tabelas

    public static <T> TableView<T> tabela() {
        TableView<T> tabela = new TableView<>();
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPlaceholder(texto("Nenhum registro encontrado."));
        tabela.setPrefHeight(330);
        tabela.setMinHeight(200);
        return tabela;
    }

    public static <T> TableColumn<T, String> coluna(String titulo, Function<T, ?> valor) {
        TableColumn<T, String> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(
                celula -> new ReadOnlyStringWrapper(Objects.toString(valor.apply(celula.getValue()), "—")));
        return coluna;
    }

    @SafeVarargs
    public static <T> void colunas(TableView<T> tabela, TableColumn<T, ?>... colunas) {
        tabela.getColumns().addAll(colunas);
    }
}
