package apresentacao.analise;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.filtro;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.secundario;

import aplicacao.servicos.FiltroEntrevista;
import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.BarraExportacao;
import apresentacao.componentes.Leitura;
import dominio.admin.TipoParametro;
import dominio.analise.Analise;
import dominio.analise.Relatorio;
import dominio.analise.Resultado;
import dominio.base.Genero;
import dominio.base.PerfilDemografico;
import dominio.candidato.Candidato;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/** Resultados de uma pesquisa processada, com agrupamento, filtros e exportação. */
class AbaResultados extends Aba {

    static final String GERAL = "Geral";

    private final ComboBox<Pesquisa> pesquisa = combo(processadas(), "Selecione uma pesquisa processada");
    private final ComboBox<Pergunta> pergunta = combo(List.<Pergunta>of(), "Selecione a pergunta");
    private final ComboBox<String> agrupar = combo(criteriosDeAgrupamento(), GERAL);
    private final ComboBox<String> regiao = filtro(administracao().valores(TipoParametro.REGIAO));
    private final ComboBox<String> faixaEtaria = filtro(administracao().valores(TipoParametro.FAIXA_ETARIA));
    private final ComboBox<String> escolaridade = filtro(administracao().valores(TipoParametro.ESCOLARIDADE));
    private final ComboBox<String> renda = filtro(administracao().valores(TipoParametro.FAIXA_RENDA));
    private final ComboBox<String> genero = filtro(Genero.textos());
    private final ComboBox<String> candidato = filtro(
            candidatos().getBancoCandidatos().stream().map(Candidato::getNome).toList());
    private final DatePicker inicio = new DatePicker();
    private final DatePicker fim = new DatePicker();

    private final VBox conteudo = new VBox(18);
    private final BarraExportacao exportacao = new BarraExportacao(this::janela, this::getRelatorio, "resultado",
            (relatorio, formato, destino) -> analise().exportarRelatorio(relatorio, formato, destino));
    private Relatorio relatorio;

    AbaResultados(ModuloView modulo) {
        super(modulo);
    }

    private Relatorio getRelatorio() {
        return relatorio;
    }

    private List<Pesquisa> processadas() {
        return pesquisas().listarTodas().stream()
                .filter(p -> p.getSituacao() == SituacaoPesquisa.PROCESSADA)
                .toList();
    }

    private List<String> criteriosDeAgrupamento() {
        List<String> criterios = new ArrayList<>();
        criterios.add(GERAL);
        criterios.addAll(analise().criteriosDisponiveis());
        return criterios;
    }

    @Override
    public Node construir() {
        agrupar.setValue(GERAL);
        pesquisa.setOnAction(evento -> {
            pergunta.getItems().setAll(pesquisa.getValue() == null ? List.of() : pesquisa.getValue().getPerguntas());
            pergunta.getSelectionModel().selectFirst();
            invalidar();
        });
        for (ComboBox<?> combo : List.of(pergunta, agrupar, regiao, faixaEtaria, escolaridade, renda, genero,
                candidato)) {
            combo.valueProperty().addListener((observado, anterior, atual) -> invalidar());
        }
        inicio.valueProperty().addListener((observado, anterior, atual) -> invalidar());
        fim.valueProperty().addListener((observado, anterior, atual) -> invalidar());

        if (!pesquisa.getItems().isEmpty()) {
            pesquisa.getSelectionModel().selectFirst();
            pergunta.getItems().setAll(pesquisa.getValue().getPerguntas());
            pergunta.getSelectionModel().selectFirst();
            aplicarFiltros();
        }

        TitledPane avancados = new TitledPane("Filtros da amostra, candidato e período",
                linha(campo("Região", regiao), campo("Faixa etária", faixaEtaria),
                        campo("Escolaridade", escolaridade), campo("Renda", renda), campo("Gênero", genero),
                        campo("Entrevistas que escolheram", candidato), campo("Entrevistas: de", inicio),
                        campo("Até", fim)));
        avancados.setExpanded(false);
        avancados.setAnimated(false);

        return pagina(
                painel(linha(campo("Pesquisa", pesquisa), campo("Pergunta", pergunta),
                        campo("Agrupar por", agrupar)),
                        avancados,
                        linha(botao("Aplicar filtros", this::aplicarFiltros),
                                secundario("Limpar filtros", this::atualizar))),
                conteudo,
                exportacao,
                aviso("Percentuais sobre respostas válidas; respostas especiais são separadas. Múltipla escolha "
                        + "usa o total de seleções válidas. Maior resto com uma casa decimal: soma de 100,0% "
                        + "quando a base é positiva."));
    }

    private void invalidar() {
        relatorio = null;
        conteudo.getChildren().clear();
        exportacao.habilitar(false);
    }

    private void aplicarFiltros() {
        Pesquisa escolhida = Leitura.obrigatorio(pesquisa, "a pesquisa");
        Pergunta perguntaEscolhida = Leitura.obrigatorio(pergunta, "a pergunta");
        FiltroEntrevista filtroEntrevistas = montarFiltro(perguntaEscolhida);

        Analise resultados = analise().analisar(escolhida.getCodigo(), filtroEntrevistas);
        Resultado resultado = resultados.buscarResultado(perguntaEscolhida.getTexto()).orElseThrow();
        String descricao = descreverFiltros(escolhida, perguntaEscolhida, filtroEntrevistas);

        conteudo.getChildren().setAll(aviso(descricao), new PainelResultado(resultado, agrupar.getValue()));
        relatorio = analise().gerarRelatorioResultado(escolhida.getCodigo(), resultado, agrupar.getValue(), descricao);
        exportacao.habilitar(true);
    }

    private FiltroEntrevista montarFiltro(Pergunta perguntaEscolhida) {
        PerfilDemografico perfil = new PerfilDemografico(Leitura.valorFiltro(faixaEtaria),
                Leitura.valorFiltro(regiao), Leitura.valorFiltro(escolaridade), Leitura.valorFiltro(renda),
                Leitura.valorFiltro(genero));
        return new FiltroEntrevista()
                .perfil(perfil)
                .periodo(inicio.getValue(), fim.getValue())
                .opcaoEscolhida(perguntaEscolhida, Leitura.valorFiltro(candidato));
    }

    private String descreverFiltros(Pesquisa escolhida, Pergunta perguntaEscolhida, FiltroEntrevista filtro) {
        String opcao = filtro.getOpcaoEscolhida().isEmpty() ? "qualquer opção" : filtro.getOpcaoEscolhida();
        return "Pesquisa: " + escolhida.getCodigo()
                + " | Pergunta: " + perguntaEscolhida.getTexto()
                + " | Agrupamento: " + agrupar.getValue()
                + " | " + filtro.getPerfil().descrever()
                + " | Entrevistas que escolheram: " + opcao
                + " | Período das entrevistas: " + textoData(filtro.getInicio(), "sem início")
                + " a " + textoData(filtro.getFim(), "sem fim");
    }

    private static String textoData(LocalDate data, String padrao) {
        return data == null ? padrao : data.toString();
    }
}
