package apresentacao.analise;

import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.indicador;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.tabela;
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.componentes.Leitura;
import dominio.analise.Distribuicao;
import dominio.analise.ItemResultado;
import dominio.analise.Resultado;
import dominio.coleta.RespostaEspecial;
import dominio.pesquisa.TipoPergunta;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/** Mostra o resultado de uma pergunta: indicadores, tabela geral, gráfico e, se pedido, um agrupamento. */
class PainelResultado extends VBox {

    private static final String SEM_PERCENTUAL = "—";

    /** Linha da tabela geral. */
    private record LinhaGeral(String opcao, int quantidade, String percentual) {
    }

    /** Linha da tabela por grupo. */
    private record LinhaGrupo(String grupo, String opcao, int quantidade, String percentual, int base) {
    }

    PainelResultado(Resultado resultado, String criterio) {
        super(18);
        Distribuicao geral = resultado.getGeral();
        String nomeBase = resultado.getTipoPergunta() == TipoPergunta.MULTIPLA_ESCOLHA
                ? "Seleções válidas (base)" : "Respostas válidas (base)";
        getChildren().addAll(
                linha(indicador(geral.getTotalEntrevistas(), "Entrevistas consideradas"),
                        indicador(geral.getTotalValidas(), nomeBase),
                        indicador(geral.getEspecial(RespostaEspecial.NAO_SABE), "Não sabe"),
                        indicador(geral.getEspecial(RespostaEspecial.NAO_RESPONDEU), "Não respondeu"),
                        indicador(geral.getEspecial(RespostaEspecial.NENHUMA_DAS_OPCOES), "Nenhuma das opções")),
                painel(titulo("Resultado geral · " + resultado.getTextoPergunta()), tabelaGeral(geral),
                        TipoGrafico.BARRAS.criar(geral)));
        if (!AbaResultados.GERAL.equals(criterio)) {
            getChildren().add(painel(titulo("Distribuição por " + criterio), tabelaPorGrupo(resultado, criterio)));
        }
    }

    private static TableView<LinhaGeral> tabelaGeral(Distribuicao distribuicao) {
        TableView<LinhaGeral> tabela = tabela();
        colunas(tabela,
                coluna("Opção", LinhaGeral::opcao),
                coluna("Quantidade", LinhaGeral::quantidade),
                coluna("Percentual", LinhaGeral::percentual));
        for (ItemResultado item : distribuicao.getItens()) {
            tabela.getItems().add(new LinhaGeral(item.getRotulo(), item.getQuantidadeAbsoluta(),
                    Leitura.percentual(item.getPercentual()) + "%"));
        }
        for (RespostaEspecial especial : RespostaEspecial.values()) {
            tabela.getItems().add(new LinhaGeral(especial.getTexto(), distribuicao.getEspecial(especial),
                    SEM_PERCENTUAL));
        }
        return tabela;
    }

    private static TableView<LinhaGrupo> tabelaPorGrupo(Resultado resultado, String criterio) {
        TableView<LinhaGrupo> tabela = tabela();
        colunas(tabela,
                coluna("Grupo", LinhaGrupo::grupo),
                coluna("Opção", LinhaGrupo::opcao),
                coluna("Quantidade", LinhaGrupo::quantidade),
                coluna("%", LinhaGrupo::percentual),
                coluna("Base válida", LinhaGrupo::base));
        for (Distribuicao grupo : resultado.getPorCriterio(criterio)) {
            for (ItemResultado item : grupo.getItens()) {
                tabela.getItems().add(new LinhaGrupo(grupo.getGrupo(), item.getRotulo(), item.getQuantidadeAbsoluta(),
                        Leitura.percentual(item.getPercentual()) + "%", grupo.getTotalValidas()));
            }
            for (RespostaEspecial especial : RespostaEspecial.values()) {
                tabela.getItems().add(new LinhaGrupo(grupo.getGrupo(), especial.getTexto(),
                        grupo.getEspecial(especial), SEM_PERCENTUAL, grupo.getTotalValidas()));
            }
        }
        return tabela;
    }
}
