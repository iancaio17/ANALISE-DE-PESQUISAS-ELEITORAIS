package dominio.analise;

import dominio.pesquisa.TipoPergunta;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Relatório consolidado dos resultados de uma pesquisa processada. */
public class RelatorioConsolidado extends Relatorio {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Analise analise;

    public RelatorioConsolidado(Analise analise) {
        if (analise == null) {
            throw new IllegalArgumentException("O relatório necessita de uma análise válida.");
        }
        this.analise = analise;
    }

    public Analise getAnalise() {
        return analise;
    }

    @Override
    public String getTitulo() {
        return "Relatório consolidado - " + analise.getCodigoPesquisa();
    }

    @Override
    public List<String[]> gerarLinhas() {
        List<String[]> linhas = new ArrayList<>();
        adicionarCabecalho(linhas);
        for (Resultado resultado : analise.getResultados()) {
            adicionarResultado(linhas, resultado);
        }
        return linhas;
    }

    /** Um gráfico por pergunta, com o resultado geral (o mesmo mostrado na tela). */
    @Override
    public List<Grafico> gerarGraficos() {
        List<Grafico> graficos = new ArrayList<>();
        for (Resultado resultado : analise.getResultados()) {
            graficos.add(Grafico.deDistribuicao(resultado.getTextoPergunta(), resultado.getGeral()));
        }
        return graficos;
    }

    private void adicionarCabecalho(List<String[]> linhas) {
        linhas.add(new String[] {"RELATÓRIO CONSOLIDADO DE PESQUISA ELEITORAL (SIMULADA)"});
        linhas.add(new String[] {"Pesquisa", analise.getCodigoPesquisa() + " - " + analise.getNomePesquisa()});
        linhas.add(new String[] {"Cargo", analise.getCargo().toString()});
        linhas.add(new String[] {"Tipo", analise.getTipo().toString()});
        linhas.add(new String[] {"Processada em", analise.getDataProcessamento().format(FORMATO_DATA)});
        linhas.add(new String[] {"Entrevistas consideradas", String.valueOf(analise.getTotalEntrevistas())});
        linhas.add(new String[] {"Obs.",
                "Percentuais calculados sobre as respostas válidas; arredondamento pelo método do maior resto (1 casa)."});
        linhas.add(new String[0]);
    }

    private void adicionarResultado(List<String[]> linhas, Resultado resultado) {
        linhas.add(new String[] {"PERGUNTA: " + resultado.getTextoPergunta()});
        linhas.add(new String[] {"Tipo", resultado.getTipoPergunta().toString()});
        if (resultado.getTipoPergunta() == TipoPergunta.MULTIPLA_ESCOLHA) {
            linhas.add(new String[] {"Base", "Seleções válidas; uma entrevista pode selecionar várias opções."});
        }
        adicionarDistribuicao(linhas, resultado.getGeral());
        linhas.add(new String[0]);
        for (Distribuicao grupo : resultado.getPorGrupo()) {
            linhas.add(new String[] {"[" + grupo.getCriterio() + ": " + grupo.getGrupo() + "]"});
            adicionarDistribuicao(linhas, grupo);
            linhas.add(new String[0]);
        }
    }
}
