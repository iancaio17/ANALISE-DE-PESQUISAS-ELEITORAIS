package dominio.analise;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resultado estatístico de uma pergunta: distribuição geral + distribuições por grupo da amostra. */
public class Resultado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigoPesquisa;
    private final String textoPergunta;
    private final Distribuicao geral;
    private final List<Distribuicao> porGrupo;

    public Resultado(String codigoPesquisa, String textoPergunta, Distribuicao geral, List<Distribuicao> porGrupo) {
        this.codigoPesquisa = codigoPesquisa;
        this.textoPergunta = textoPergunta;
        this.geral = geral;
        this.porGrupo = new ArrayList<>(porGrupo);
    }

    public String getCodigoPesquisa() { return codigoPesquisa; }
    public String getTextoPergunta() { return textoPergunta; }
    public Distribuicao getGeral() { return geral; }
    public List<Distribuicao> getPorGrupo() { return Collections.unmodifiableList(porGrupo); }

    /** Distribuições de um critério específico (ex.: "Região"). */
    public List<Distribuicao> getPorCriterio(String nomeCriterio) {
        List<Distribuicao> saida = new ArrayList<>();
        for (Distribuicao d : porGrupo) {
            if (d.getCriterio().equalsIgnoreCase(nomeCriterio)) saida.add(d);
        }
        return saida;
    }
}
