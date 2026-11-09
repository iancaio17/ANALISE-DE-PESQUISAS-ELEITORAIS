package dominio.analise;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de UMA pergunta para UM grupo (ex.: criterio "Região", grupo "Norte"; ou "Geral"/"Todos").
 * Os percentuais dos itens são calculados sobre as respostas VÁLIDAS; "não sabe", "não respondeu"
 * e "nenhuma das opções" são guardados à parte.
 */
public class Distribuicao implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String criterio;
    private final String grupo;
    private final List<ItemResultado> itens;
    private final int totalValidas;
    private final int naoSabe;
    private final int naoRespondeu;
    private final int nenhuma;

    public Distribuicao(String criterio, String grupo, List<ItemResultado> itens,
                        int totalValidas, int naoSabe, int naoRespondeu, int nenhuma) {
        this.criterio = criterio;
        this.grupo = grupo;
        this.itens = new ArrayList<>(itens);
        this.totalValidas = totalValidas;
        this.naoSabe = naoSabe;
        this.naoRespondeu = naoRespondeu;
        this.nenhuma = nenhuma;
    }

    public String getCriterio() { return criterio; }
    public String getGrupo() { return grupo; }
    public List<ItemResultado> getItens() { return Collections.unmodifiableList(itens); }
    public int getTotalValidas() { return totalValidas; }
    public int getNaoSabe() { return naoSabe; }
    public int getNaoRespondeu() { return naoRespondeu; }
    public int getNenhuma() { return nenhuma; }

    /** Número de entrevistas consideradas (válidas + especiais). */
    public int getTotalEntrevistas() { return totalValidas + naoSabe + naoRespondeu + nenhuma; }
}
