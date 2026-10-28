package dominio.analise;

import dominio.pesquisa.Pesquisa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Resultado do processamento de uma pesquisa. Guarda um "retrato" (snapshot) dos dados da pesquisa
 * em vez da referência ao objeto Pesquisa, para poder ser salvo em arquivo sem arrastar todo o grafo
 * de objetos (Candidato, Partido, etc.).
 *
 * Esta classe NÃO altera a situação da pesquisa; isso é responsabilidade do AnaliseService.
 */
public class Analise implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigoPesquisa;
    private final String nomePesquisa;
    private final String cargo;
    private final String tipo;
    private final Set<String> regioes;
    private final Date dataProcessamento;
    private final List<Resultado> resultados;

    public Analise(Pesquisa pesquisa, List<Resultado> resultados) {
        if (pesquisa == null) {
            throw new IllegalArgumentException("A análise requer uma pesquisa válida.");
        }
        if (resultados == null || resultados.isEmpty()) {
            throw new IllegalArgumentException("A análise requer ao menos um resultado calculado.");
        }
        this.codigoPesquisa = pesquisa.getCodigo();
        this.nomePesquisa = pesquisa.getNome();
        this.cargo = pesquisa.getCargo();
        this.tipo = pesquisa.getTipo();
        this.regioes = new TreeSet<>();
        for (PerfilDemografico p : pesquisa.getPlanoAmostral().getMetasPorPerfil().keySet()) {
            if (p.getRegiao() != null) this.regioes.add(p.getRegiao());
        }
        this.dataProcessamento = new Date();
        this.resultados = new ArrayList<>(resultados);
    }

    public String getCodigoPesquisa() { return codigoPesquisa; }
    public String getNomePesquisa() { return nomePesquisa; }
    public String getCargo() { return cargo; }
    public String getTipo() { return tipo; }
    public Set<String> getRegioes() { return Collections.unmodifiableSet(regioes); }
    public Date getDataProcessamento() { return new Date(dataProcessamento.getTime()); }
    public List<Resultado> getResultados() { return Collections.unmodifiableList(resultados); }

    public int getTotalEntrevistas() {
        return resultados.get(0).getGeral().getTotalEntrevistas();
    }
}
