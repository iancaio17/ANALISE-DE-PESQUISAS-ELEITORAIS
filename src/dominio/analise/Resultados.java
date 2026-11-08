package dominio.analise;

public class Resultados {
    private String rotulo;
    private int quantidadeAbsoluta;
    private double percentual;

    public Resultados(String rotulo, int quantidadeAbsoluta, double percentual) {
        this.rotulo = rotulo;
        this.quantidadeAbsoluta = quantidadeAbsoluta;
        this.percentual = percentual;
    }

    public String getRotulo() { return rotulo; }
    public void setRotulo(String rotulo) { this.rotulo = rotulo; }

    public int getQuantidadeAbsoluta() { return quantidadeAbsoluta; }
    public void setQuantidadeAbsoluta(int quantidadeAbsoluta) { this.quantidadeAbsoluta = quantidadeAbsoluta; }

    public double getPercentual() { return percentual; }
    public void setPercentual(double percentual) { this.percentual = percentual; }
}