package apresentacao;

import apresentacao.admin.ModuloAdministracao;
import apresentacao.analise.ModuloAnalise;
import apresentacao.candidatos.ModuloCandidatos;
import apresentacao.coleta.ModuloColeta;
import apresentacao.componentes.Componentes;
import apresentacao.pesquisas.ModuloPesquisas;
import aplicacao.SistemaContexto;
import dominio.admin.Perfil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Janela principal: menu lateral com os módulos, barra superior com o usuário e a área de conteúdo. */
public class TelaPrincipal extends BorderPane implements Navegador {

    private static final String INICIO = "Início";
    private static final String PESQUISAS = "Pesquisas";
    private static final String COLETA = "Coleta e amostra";
    private static final String ANALISE = "Análise estatística";
    private static final String CANDIDATOS = "Candidatos e partidos";
    private static final String ADMINISTRACAO = "Administração";

    private final SistemaContexto contexto;
    private final Runnable aoSair;
    private final Label titulo = new Label();
    private final BorderPane area = new BorderPane();
    private final Map<String, Button> botoesDoMenu = new LinkedHashMap<>();

    public TelaPrincipal(SistemaContexto contexto, Runnable aoSair) {
        this.contexto = contexto;
        this.aoSair = aoSair;
        setLeft(montarMenu());
        area.setTop(montarBarraSuperior());
        setCenter(area);
        mostrar(INICIO, new TelaInicio(contexto, this).construir());
    }

    private VBox montarMenu() {
        Label marca = Componentes.texto("Pesquisas eleitorais");
        marca.getStyleClass().add("brand");
        Label subtitulo = Componentes.texto("Sistema acadêmico simulado");
        subtitulo.getStyleClass().add("sidebar-note");

        VBox menu = new VBox(6, marca, subtitulo);
        menu.getStyleClass().add("sidebar");
        menu.setPrefWidth(224);
        menu.setPadding(new Insets(28, 14, 24, 14));

        adicionarItem(menu, INICIO, null, () -> new TelaInicio(contexto, this).construir());
        adicionarItem(menu, PESQUISAS, null, () -> new ModuloPesquisas(contexto));
        adicionarItem(menu, COLETA, Perfil.PESQUISADOR, () -> new ModuloColeta(contexto));
        adicionarItem(menu, ANALISE, Perfil.ANALISTA, () -> new ModuloAnalise(contexto));
        adicionarItem(menu, CANDIDATOS, null, () -> new ModuloCandidatos(contexto));
        adicionarItem(menu, ADMINISTRACAO, Perfil.ADMINISTRADOR, () -> new ModuloAdministracao(contexto));

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        menu.getChildren().addAll(espaco,
                Componentes.texto("Todos os dados são fictícios e usados apenas para fins didáticos."));
        return menu;
    }

    /** Cria o botão do menu; ele fica desabilitado quando o usuário não tem o perfil exigido. */
    private void adicionarItem(VBox menu, String nome, Perfil perfilExigido, Supplier<Node> tela) {
        Button botao = Componentes.botao(nome, () -> mostrar(nome, tela.get()));
        botao.getStyleClass().add("nav-button");
        botao.setMaxWidth(Double.MAX_VALUE);
        if (perfilExigido != null && !contexto.permite(perfilExigido)) {
            botao.setDisable(true);
            botao.setTooltip(new Tooltip("Acesso restrito ao perfil " + perfilExigido));
        }
        botoesDoMenu.put(nome, botao);
        menu.getChildren().add(botao);
    }

    private HBox montarBarraSuperior() {
        titulo.getStyleClass().add("page-title");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        Label usuario = new Label(contexto.getUsuarioLogado().getNome() + " · "
                + contexto.getUsuarioLogado().getPerfis());
        Button sair = Componentes.secundario("Sair", () -> {
            contexto.sair();
            aoSair.run();
        });

        HBox barra = new HBox(18, titulo, espaco, usuario, sair);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(16, 28, 16, 28));
        barra.getStyleClass().add("topbar");
        return barra;
    }

    private void mostrar(String nome, Node conteudo) {
        titulo.setText(nome);
        area.setCenter(conteudo);
        botoesDoMenu.forEach((chave, botao) -> {
            botao.getStyleClass().remove("active");
            if (chave.equals(nome)) {
                botao.getStyleClass().add("active");
            }
        });
    }

    @Override
    public void abrirPesquisas() {
        mostrar(PESQUISAS, new ModuloPesquisas(contexto));
    }

    @Override
    public void abrirRegistroDeEntrevista() {
        ModuloColeta modulo = new ModuloColeta(contexto);
        modulo.abrirAba(ModuloColeta.ABA_REGISTRO);
        mostrar(COLETA, modulo);
    }

    @Override
    public void abrirAnalise() {
        mostrar(ANALISE, new ModuloAnalise(contexto));
    }

    @Override
    public void recarregarInicio() {
        mostrar(INICIO, new TelaInicio(contexto, this).construir());
    }
}
