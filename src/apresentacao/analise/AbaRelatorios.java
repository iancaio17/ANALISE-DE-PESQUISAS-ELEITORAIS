package apresentacao.analise;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.BarraExportacao;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.analise.Analise;
import dominio.analise.Relatorio;
import dominio.analise.Resultado;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/** Relatório consolidado de uma pesquisa processada e gráficos das suas perguntas. */
class AbaRelatorios extends Aba {

    private final TextArea texto = new TextArea();
    private final VBox grafico = new VBox(12);
    private final BarraExportacao exportacao = new BarraExportacao(this::janela, this::getRelatorio,
            "relatorio-consolidado", (relatorio, formato, destino) ->
                    analise().exportarRelatorio(relatorio, formato, destino));
    private Relatorio relatorio;

    AbaRelatorios(ModuloView modulo) {
        super(modulo);
    }

    private Relatorio getRelatorio() {
        return relatorio;
    }

    @Override
    public Node construir() {
        List<Pesquisa> processadas = pesquisas().listarTodas().stream()
                .filter(p -> p.getSituacao() == SituacaoPesquisa.PROCESSADA)
                .toList();
        ComboBox<Pesquisa> pesquisa = combo(processadas, "Selecione uma pesquisa processada");
        ComboBox<Pergunta> pergunta = combo(List.<Pergunta>of(), "Pergunta do gráfico");
        ComboBox<TipoGrafico> tipo = combo(TipoGrafico.values(), "Tipo de gráfico");
        tipo.setValue(TipoGrafico.BARRAS);
        texto.setEditable(false);
        texto.setPrefRowCount(15);
        texto.getStyleClass().add("report-text");

        Runnable desenhar = () -> desenharGrafico(pesquisa.getValue(), pergunta.getValue(), tipo.getValue());
        pesquisa.setOnAction(evento -> {
            limpar();
            pergunta.getItems().setAll(pesquisa.getValue() == null ? List.of() : pesquisa.getValue().getPerguntas());
            pergunta.getSelectionModel().selectFirst();
        });
        pergunta.setOnAction(evento -> Dialogos.executar(desenhar));
        tipo.setOnAction(evento -> Dialogos.executar(desenhar));

        return pagina(
                painel(linha(campo("Pesquisa", pesquisa), campo("Pergunta do gráfico", pergunta),
                        campo("Visualização", tipo),
                        botao("Gerar relatório", () -> gerar(Leitura.obrigatorio(pesquisa, "a pesquisa"), desenhar)))),
                painel(titulo("Visualização dos resultados"), grafico),
                painel(titulo("Relatório consolidado"), texto, exportacao),
                aviso("O relatório consolidado inclui todas as perguntas e distribuições. Use a aba Resultados "
                        + "para exportar filtros específicos. CSV e PDF exportam também os gráficos: o PDF "
                        + "desenha as barras e o CSV traz a tabela do gráfico com barras em texto."));
    }

    private void limpar() {
        relatorio = null;
        texto.clear();
        grafico.getChildren().clear();
        exportacao.habilitar(false);
    }

    private void gerar(Pesquisa pesquisa, Runnable desenhar) {
        relatorio = analise().gerarRelatorioConsolidado(pesquisa.getCodigo());
        texto.setText(relatorio.paraTexto());
        desenhar.run();
        exportacao.habilitar(true);
    }

    private void desenharGrafico(Pesquisa pesquisa, Pergunta pergunta, TipoGrafico tipo) {
        if (pesquisa == null || pergunta == null) {
            return;
        }
        Analise resultados = analise().buscarAnalise(pesquisa.getCodigo());
        Resultado resultado = resultados.buscarResultado(pergunta.getTexto()).orElseThrow();
        grafico.getChildren().setAll(tipo.criar(resultado.getGeral()));
    }
}
