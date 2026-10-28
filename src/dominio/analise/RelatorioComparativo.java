package dominio.analise;

import java.util.ArrayList;
import java.util.List;

/** Relatório que compara os resultados gerais de duas pesquisas compatíveis. */
public class RelatorioComparativo extends Relatorio {

    private final ComparacaoPesquisa comparacao;

    public RelatorioComparativo(ComparacaoPesquisa comparacao) {
        if (comparacao == null) throw new IllegalArgumentException("O relatório necessita de uma comparação válida.");
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
        List<String[]> l = new ArrayList<>();
        l.add(new String[]{"RELATÓRIO COMPARATIVO DE PESQUISAS (SIMULADAS)"});
        l.add(new String[]{"Pesquisa A", a.getCodigoPesquisa() + " - " + a.getNomePesquisa()});
        l.add(new String[]{"Pesquisa B", b.getCodigoPesquisa() + " - " + b.getNomePesquisa()});
        l.add(new String[]{"Cargo / Tipo", a.getCargo() + " / " + a.getTipo()});
        l.add(new String[]{"Obs.", "Diferença em pontos percentuais (B - A), sobre respostas válidas."});
        l.add(new String[0]);

        String perguntaAtual = null;
        for (ComparacaoPesquisa.LinhaComparacao x : comparacao.getLinhas()) {
            if (!x.getPergunta().equals(perguntaAtual)) {
                if (perguntaAtual != null) l.add(new String[0]);
                perguntaAtual = x.getPergunta();
                l.add(new String[]{"PERGUNTA: " + perguntaAtual});
                l.add(new String[]{"Opção", "A: respostas", "A: %", "B: respostas", "B: %", "Diferença (p.p.)"});
            }
            l.add(new String[]{x.getRotulo(),
                    String.valueOf(x.getQuantidadeA()), pct(x.getPercentualA()),
                    String.valueOf(x.getQuantidadeB()), pct(x.getPercentualB()),
                    String.format(PT_BR, "%+.1f", x.getDiferenca())});
        }
        return l;
    }
}
