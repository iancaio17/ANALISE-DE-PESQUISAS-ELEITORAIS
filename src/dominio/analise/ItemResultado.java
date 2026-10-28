package dominio.analise;

import java.io.Serializable;

/** Uma linha de resultado: rótulo (ex.: nome do candidato), quantidade absoluta e percentual. */
public class ItemResultado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String rotulo;
    private final int quantidadeAbsoluta;
    private final double percentual;

    public ItemResultado(String rotulo, int quantidadeAbsoluta, double percentual) {
        this.rotulo = rotulo;
        this.quantidadeAbsoluta = quantidadeAbsoluta;
        this.percentual = percentual;
    }

    public String getRotulo() { return rotulo; }
    public int getQuantidadeAbsoluta() { return quantidadeAbsoluta; }
    public double getPercentual() { return percentual; }
}
