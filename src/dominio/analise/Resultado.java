package dominio.analise;

import dominio.pesquisa.TipoPergunta;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resultado estatístico de uma pergunta: distribuição geral e distribuições por grupo da amostra. */
public class Resultado implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String codigoPesquisa;
    private final String textoPergunta;
    private final TipoPergunta tipoPergunta;
    private final Distribuicao geral;
    private final List<Distribuicao> porGrupo;

    public Resultado(String codigoPesquisa, String textoPergunta, TipoPergunta tipoPergunta,
            Distribuicao geral, List<Distribuicao> porGrupo) {
        this.codigoPesquisa = codigoPesquisa;
        this.textoPergunta = textoPergunta;
        this.tipoPergunta = tipoPergunta;
        this.geral = geral;
        this.porGrupo = new ArrayList<>(porGrupo);
    }

    public String getCodigoPesquisa() {
        return codigoPesquisa;
    }

    public String getTextoPergunta() {
        return textoPergunta;
    }

    public TipoPergunta getTipoPergunta() {
        return tipoPergunta;
    }

    public Distribuicao getGeral() {
        return geral;
    }

    public List<Distribuicao> getPorGrupo() {
        return Collections.unmodifiableList(porGrupo);
    }

    /** Distribuições de um critério específico (ex.: "Região"). */
    public List<Distribuicao> getPorCriterio(String nomeCriterio) {
        List<Distribuicao> saida = new ArrayList<>();
        for (Distribuicao distribuicao : porGrupo) {
            if (distribuicao.getCriterio().equalsIgnoreCase(nomeCriterio)) {
                saida.add(distribuicao);
            }
        }
        return saida;
    }
}
