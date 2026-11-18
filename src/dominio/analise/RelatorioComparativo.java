package dominio.analise;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Relatório que compara os resultados gerais de duas pesquisas compatíveis. */
public class RelatorioComparativo extends Relatorio {

    private final ComparacaoPesquisa comparacao;

    public RelatorioComparativo(ComparacaoPesquisa comparacao) {
        if (comparacao == null) {
            throw new IllegalArgumentException("O relatório necessita de uma comparação válida.");
        }
        this.comparacao = comparacao;
    }

    @Override
    public String getTitulo() {
        return "Comparação - " + comparacao.getAnaliseA().getCodigoPesquisa()
                + " x " + comparacao.getAnaliseB().getCodigoPesquisa();
    }

    @Override
    public List<String[]> gerarLinhas() {
        Analise a = comparacao.getAnaliseA();
        Analise b = comparacao.getAnaliseB();
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[] {"RELATÓRIO COMPARATIVO DE PESQUISAS (SIMULADAS)"});
        linhas.add(new String[] {"Pesquisa A", a.getCodigoPesquisa() + " - " + a.getNomePesquisa()});
        linhas.add(new String[] {"Pesquisa B", b.getCodigoPesquisa() + " - " + b.getNomePesquisa()});
        linhas.add(new String[] {"Cargo / Tipo", a.getCargo() + " / " + a.getTipo()});
        linhas.add(new String[] {"Obs.", "Diferença em pontos percentuais (B - A), sobre respostas válidas."});
        linhas.add(new String[0]);

        String perguntaAtual = null;
        for (ComparacaoPesquisa.LinhaComparacao linha : comparacao.getLinhas()) {
            if (!linha.getPergunta().equals(perguntaAtual)) {
                if (perguntaAtual != null) {
                    linhas.add(new String[0]);
                }
                perguntaAtual = linha.getPergunta();
                linhas.add(new String[] {"PERGUNTA: " + perguntaAtual});
                linhas.add(new String[] {"Opção", "A: respostas", "A: %", "B: respostas", "B: %", "Diferença (p.p.)"});
            }
            linhas.add(new String[] {linha.getRotulo(),
                    String.valueOf(linha.getQuantidadeA()), percentual(linha.getPercentualA()),
                    String.valueOf(linha.getQuantidadeB()), percentual(linha.getPercentualB()),
                    String.format(PT_BR, "%+.1f", linha.getDiferenca())});
        }
        return linhas;
    }

    /** Um gráfico por pergunta, com uma série para cada pesquisa comparada. */
    @Override
    public List<Grafico> gerarGraficos() {
        String nomeA = comparacao.getAnaliseA().getCodigoPesquisa();
        String nomeB = comparacao.getAnaliseB().getCodigoPesquisa();
        Map<String, List<ComparacaoPesquisa.LinhaComparacao>> porPergunta = new LinkedHashMap<>();
        for (ComparacaoPesquisa.LinhaComparacao linha : comparacao.getLinhas()) {
            porPergunta.computeIfAbsent(linha.getPergunta(), pergunta -> new ArrayList<>()).add(linha);
        }

        List<Grafico> graficos = new ArrayList<>();
        porPergunta.forEach((pergunta, linhasDaPergunta) -> {
            List<String> rotulos = new ArrayList<>();
            List<Double> percentuaisA = new ArrayList<>();
            List<Double> percentuaisB = new ArrayList<>();
            for (ComparacaoPesquisa.LinhaComparacao linha : linhasDaPergunta) {
                rotulos.add(linha.getRotulo());
                percentuaisA.add(linha.getPercentualA());
                percentuaisB.add(linha.getPercentualB());
            }
            graficos.add(new Grafico(pergunta, rotulos, List.of(
                    new Grafico.Serie("Pesquisa " + nomeA + " (%)", percentuaisA),
                    new Grafico.Serie("Pesquisa " + nomeB + " (%)", percentuaisB))));
        });
        return graficos;
    }
}
