package dominio.analise;

import dominio.coleta.RespostaEspecial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resultado de uma pergunta para um grupo (ex.: critério "Região", grupo "Norte", ou "Geral" / "Todos").
 * Os percentuais dos itens usam apenas as respostas válidas; as respostas especiais
 * (não sabe, não respondeu, nenhuma das opções) são guardadas à parte.
 */
public class Distribuicao implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String criterio;
    private final String grupo;
    private final List<ItemResultado> itens;
    private final int totalValidas;
    private final Map<RespostaEspecial, Integer> especiais;
    private final int totalEntrevistas;

    public Distribuicao(String criterio, String grupo, List<ItemResultado> itens, int totalValidas,
            Map<RespostaEspecial, Integer> especiais, int totalEntrevistas) {
        this.criterio = criterio;
        this.grupo = grupo;
        this.itens = new ArrayList<>(itens);
        this.totalValidas = totalValidas;
        this.especiais = new EnumMap<>(RespostaEspecial.class);
        this.especiais.putAll(especiais);
        this.totalEntrevistas = totalEntrevistas;
    }

    public String getCriterio() {
        return criterio;
    }

    public String getGrupo() {
        return grupo;
    }

    public List<ItemResultado> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /** Base do cálculo dos percentuais: respostas (ou seleções) válidas. */
    public int getTotalValidas() {
        return totalValidas;
    }

    /** Quantidade de respostas especiais de um tipo. */
    public int getEspecial(RespostaEspecial tipo) {
        return especiais.getOrDefault(tipo, 0);
    }

    /** Número de entrevistas consideradas no grupo. */
    public int getTotalEntrevistas() {
        return totalEntrevistas;
    }
}
