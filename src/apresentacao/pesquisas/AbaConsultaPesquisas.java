package apresentacao.pesquisas;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.filtro;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.perigo;
import static apresentacao.componentes.Componentes.secundario;
import static apresentacao.componentes.Componentes.tabela;

import aplicacao.servicos.FiltroPesquisa;
import apresentacao.base.Aba;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Perfil;
import dominio.admin.TipoParametro;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import dominio.pesquisa.TipoPesquisa;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

/** Consulta com filtros e ações de ciclo de vida das pesquisas (criar, editar, iniciar, encerrar, excluir). */
class AbaConsultaPesquisas extends Aba {

    private final ModuloPesquisas modulo;

    private final TextField busca = entrada("Nome ou código");
    private final ComboBox<String> regiao = filtro(administracao().valores(TipoParametro.REGIAO));
    private final ComboBox<Cargo> cargo = combo(Cargo.values(), "Todos os cargos");
    private final ComboBox<TipoPesquisa> tipo = combo(TipoPesquisa.values(), "Todos os tipos");
    private final ComboBox<SituacaoPesquisa> situacao = combo(SituacaoPesquisa.values(), "Todas as situações");
    private final DatePicker inicio = new DatePicker();
    private final DatePicker fim = new DatePicker();
    private final TextField minimo = entrada("Mínimo");
    private final TextField maximo = entrada("Máximo");
    private final CheckBox abaixoDaMeta = new CheckBox("Apenas grupos abaixo da meta");

    AbaConsultaPesquisas(ModuloPesquisas modulo) {
        super(modulo);
        this.modulo = modulo;
        minimo.setPrefWidth(90);
        maximo.setPrefWidth(90);
    }

    @Override
    public Node construir() {
        TableView<Pesquisa> tabela = criarTabela();
        Runnable filtrar = () -> tabela.getItems().setAll(pesquisas().consultar(montarFiltro()));
        filtrar.run();

        FlowPane acoes = linha(
                botao("Nova pesquisa", () -> editar(null)),
                secundario("Editar", () -> editar(Leitura.selecionado(tabela))),
                secundario("Iniciar coleta", () -> iniciarColeta(Leitura.selecionado(tabela))),
                secundario("Encerrar coleta", () -> encerrarColeta(Leitura.selecionado(tabela))),
                secundario("Alterar previsão", () -> alterarPrevisao(Leitura.selecionado(tabela))),
                perigo("Excluir", () -> excluir(Leitura.selecionado(tabela))));
        acoes.setDisable(!permite(Perfil.PESQUISADOR));

        return pagina(
                painel(linha(campo("Buscar", busca), campo("Região", regiao), campo("Cargo", cargo),
                        campo("Tipo", tipo), campo("Situação", situacao), campo("Período: de", inicio),
                        campo("Até", fim), campo("Entrevistas previstas: mín.", minimo), campo("Máx.", maximo),
                        abaixoDaMeta, botao("Filtrar", filtrar), secundario("Limpar filtros", this::atualizar))),
                painel(acoes, tabela, secundario("Configurar selecionada", () -> configurar(Leitura.selecionado(tabela)))),
                aviso("A estrutura é configurada no planejamento. Durante a coleta, usuários autorizados "
                        + "podem aumentar a quantidade prevista."));
    }

    private TableView<Pesquisa> criarTabela() {
        TableView<Pesquisa> tabela = tabela();
        tabela.setId("tabela-pesquisas");
        colunas(tabela,
                coluna("Código", Pesquisa::getCodigo),
                coluna("Nome", Pesquisa::getNome),
                coluna("Cargo", Pesquisa::getCargo),
                coluna("Tipo", Pesquisa::getTipo),
                coluna("Situação", Pesquisa::getSituacao),
                coluna("Entrevistas", p -> p.getQuantidadeRealizada() + " / " + p.getQuantidadePrevista()),
                coluna("Conclusão", p -> Leitura.percentual(p.getPercentualConclusao()) + "%"));
        return tabela;
    }

    private FiltroPesquisa montarFiltro() {
        return new FiltroPesquisa()
                .texto(busca.getText())
                .regiao(Leitura.valorFiltro(regiao))
                .cargo(cargo.getValue())
                .tipo(tipo.getValue())
                .situacao(situacao.getValue())
                .periodo(inicio.getValue(), fim.getValue())
                .entrevistados(Leitura.limite(minimo, 0), Leitura.limite(maximo, Integer.MAX_VALUE))
                .apenasAbaixoDaMeta(abaixoDaMeta.isSelected());
    }

    private void editar(Pesquisa pesquisa) {
        FormularioPesquisa formulario = new FormularioPesquisa(janela(), contexto, pesquisa);
        formulario.abrir();
        formulario.codigoSalvo().ifPresent(modulo::setCodigoSelecionado);
        atualizar();
    }

    private void iniciarColeta(Pesquisa pesquisa) {
        pesquisas().iniciarColeta(pesquisa.getCodigo());
        atualizar();
    }

    private void encerrarColeta(Pesquisa pesquisa) {
        if (Dialogos.confirmar("Encerrar a coleta de " + pesquisa.getCodigo()
                + "? Ela não poderá receber novas entrevistas.")) {
            pesquisas().encerrarColeta(pesquisa.getCodigo());
            atualizar();
        }
    }

    private void alterarPrevisao(Pesquisa pesquisa) {
        String digitado = Dialogos.pedir(janela(), "Nova quantidade prevista",
                String.valueOf(pesquisa.getQuantidadePrevista()));
        if (digitado != null) {
            pesquisas().alterarMeta(pesquisa.getCodigo(), Leitura.inteiro(new TextField(digitado), "Quantidade"));
            atualizar();
        }
    }

    private void excluir(Pesquisa pesquisa) {
        if (Dialogos.confirmar("Excluir a pesquisa planejada " + pesquisa.getCodigo() + "?")) {
            pesquisas().excluirPesquisa(pesquisa.getCodigo());
            atualizar();
        }
    }

    private void configurar(Pesquisa pesquisa) {
        modulo.setCodigoSelecionado(pesquisa.getCodigo());
        modulo.abrirAba(1);
    }
}
