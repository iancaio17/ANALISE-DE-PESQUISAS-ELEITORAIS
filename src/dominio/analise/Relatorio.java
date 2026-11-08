package dominio.analise;

public class Relatorio {
    private Analise analise;
    private String formatoVisual;

    public Relatorio(Analise analise, String formatoVisual) {
        if (analise == null) {
            throw new IllegalArgumentException("O relatório necessita de uma análise válida.");
        }
        this.analise = analise;
        this.formatoVisual = formatoVisual;
    }

    public Analise getAnalise() { return analise; }
    public String getFormatoVisual() { return formatoVisual; }

    public void exportar() {
        System.out.println("=== A Gerar Relatório (" + formatoVisual + ") ===");
        System.out.println("Pesquisa: " + analise.getPesquisa().getNome());
        System.out.println("Data de Processamento: " + analise.getDataProcessamento());
        System.out.println("-----------------------------------");

        for (Resultados r : analise.getResultados()) {
            System.out.println(r.getRotulo() + ": " + r.getQuantidadeAbsoluta() + " respostas (" + r.getPercentual() + "%)");
        }
        System.out.println("-----------------------------------");
    }
}