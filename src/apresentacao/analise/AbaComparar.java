package apresentacao.analise;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.tabela;
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.BarraExportacao;
import apresentacao.componentes.Leitura;
import dominio.analise.ComparacaoPesquisa;
import dominio.analise.ComparacaoPesquisa.LinhaComparacao;
import dominio.analise.Relatorio;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import java.util.List;
import java.util.Locale;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/** Comparação de duas pesquisas processadas e compatíveis. */
class AbaComparar extends Aba {

    private final VBox resultado = new VBox(16);
    private final BarraExportacao exportacao = new BarraExportacao(this::janela, this::getRelatorio, "comparacao",
            (relatorio, formato, destino) -> analise().exportarRelatorio(relatorio, formato, destino));
    private Relatorio relatorio;

    AbaComparar(ModuloView modulo) {
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
        ComboBox<Pesquisa> pesquisaA = combo(processadas, "Pesquisa A");
        ComboBox<Pesquisa> pesquisaB = combo(processadas, "Pesquisa B");
        pesquisaA.setOnAction(evento -> limpar());
        pesquisaB.setOnAction(evento -> limpar());

        return pagina(
                aviso("Compare pesquisas diferentes e processadas, com mesmo cargo, tipo, regiões, perguntas, "
                        + "tipos de pergunta e opções. Diferença = B − A em pontos percentuais."),
                painel(linha(campo("Pesquisa A", pesquisaA), campo("Pesquisa B", pesquisaB),
                        botao("Comparar", () -> comparar(Leitura.obrigatorio(pesquisaA, "a pesquisa A"),
                                Leitura.obrigatorio(pesquisaB, "a pesquisa B"))))),
                resultado,
                exportacao);
    }

    private void limpar() {
        resultado.getChildren().clear();
        relatorio = null;
        exportacao.habilitar(false);
    }

    private void comparar(Pesquisa a, Pesquisa b) {
        ComparacaoPesquisa comparacao = analise().compararPesquisas(a.getCodigo(), b.getCodigo());
        resultado.getChildren().setAll(
                painel(titulo(a.getCodigo() + " × " + b.getCodigo()), criarTabela(comparacao)));
        relatorio = analise().gerarRelatorioComparativo(a.getCodigo(), b.getCodigo());
        exportacao.habilitar(true);
    }

    private static TableView<LinhaComparacao> criarTabela(ComparacaoPesquisa comparacao) {
        TableView<LinhaComparacao> tabela = tabela();
        colunas(tabela,
                coluna("Pergunta", LinhaComparacao::getPergunta),
                coluna("Opção", LinhaComparacao::getRotulo),
                coluna("A: qtd.", LinhaComparacao::getQuantidadeA),
                coluna("A: %", linha -> Leitura.percentual(linha.getPercentualA())),
                coluna("B: qtd.", LinhaComparacao::getQuantidadeB),
                coluna("B: %", linha -> Leitura.percentual(linha.getPercentualB())),
                coluna("Diferença (p.p.)",
                        linha -> String.format(Locale.forLanguageTag("pt-BR"), "%+.1f", linha.getDiferenca())));
        tabela.getItems().setAll(comparacao.getLinhas());
        return tabela;
    }
}
