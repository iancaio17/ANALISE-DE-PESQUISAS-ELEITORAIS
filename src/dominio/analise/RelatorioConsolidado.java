package dominio.analise;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** Relatório consolidado dos resultados de UMA pesquisa processada. */
public class RelatorioConsolidado extends Relatorio {

    private final Analise analise;

    public RelatorioConsolidado(Analise analise) {
        if (analise == null) throw new IllegalArgumentException("O relatório necessita de uma análise válida.");
        this.analise = analise;
    }

    public Analise getAnalise() { return analise; }

    @Override
    public String getTitulo() {
        return "Relatório consolidado - " + analise.getCodigoPesquisa();
    }

    @Override
    public List<String[]> gerarLinhas() {
        List<String[]> l = new ArrayList<>();
        l.add(new String[]{"RELATÓRIO CONSOLIDADO DE PESQUISA ELEITORAL (SIMULADA)"});
        l.add(new String[]{"Pesquisa", analise.getCodigoPesquisa() + " - " + analise.getNomePesquisa()});
        l.add(new String[]{"Cargo", analise.getCargo()});
        l.add(new String[]{"Tipo", analise.getTipo()});
        l.add(new String[]{"Processada em", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(analise.getDataProcessamento())});
        l.add(new String[]{"Entrevistas consideradas", String.valueOf(analise.getTotalEntrevistas())});
        l.add(new String[]{"Obs.", "Percentuais calculados sobre as respostas válidas; arredondamento pelo método do maior resto (1 casa)."});
        l.add(new String[0]);

        for (Resultado r : analise.getResultados()) {
            l.add(new String[]{"PERGUNTA: " + r.getTextoPergunta()});
            adicionarDistribuicao(l, r.getGeral());
            l.add(new String[0]);
            for (Distribuicao d : r.getPorGrupo()) {
                l.add(new String[]{"[" + d.getCriterio() + ": " + d.getGrupo() + "]"});
                adicionarDistribuicao(l, d);
                l.add(new String[0]);
            }
        }
        return l;
    }

    private void adicionarDistribuicao(List<String[]> l, Distribuicao d) {
        l.add(new String[]{"Opção", "Respostas", "Percentual (%)"});
        for (ItemResultado i : d.getItens()) {
            l.add(new String[]{i.getRotulo(), String.valueOf(i.getQuantidadeAbsoluta()), pct(i.getPercentual())});
        }
        l.add(new String[]{"Respostas válidas (base do cálculo)", String.valueOf(d.getTotalValidas()), ""});
        l.add(new String[]{RespostasEspeciais.NAO_SABE, String.valueOf(d.getNaoSabe()), ""});
        l.add(new String[]{RespostasEspeciais.NAO_RESPONDEU, String.valueOf(d.getNaoRespondeu()), ""});
        l.add(new String[]{RespostasEspeciais.NENHUMA, String.valueOf(d.getNenhuma()), ""});
        l.add(new String[]{"Total de entrevistas", String.valueOf(d.getTotalEntrevistas()), ""});
    }
}
