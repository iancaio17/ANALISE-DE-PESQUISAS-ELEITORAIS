package dominio.analise;

import dominio.pesquisa.Cargo;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.TipoPesquisa;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Resultado do processamento de uma pesquisa. Guarda um "retrato" dos dados da pesquisa (código, nome,
 * cargo...) em vez da referência ao objeto Pesquisa. A mudança de situação da pesquisa é
 * responsabilidade do AnaliseService.
 */
public class Analise implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String codigoPesquisa;
    private final String nomePesquisa;
    private final Cargo cargo;
    private final TipoPesquisa tipo;
    private final Set<String> regioes = new TreeSet<>();
    private final LocalDateTime dataProcessamento = LocalDateTime.now();
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
        this.regioes.addAll(pesquisa.getRegioes());
        this.resultados = new ArrayList<>(resultados);
    }

    public String getCodigoPesquisa() {
        return codigoPesquisa;
    }

    public String getNomePesquisa() {
        return nomePesquisa;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public TipoPesquisa getTipo() {
        return tipo;
    }

    public Set<String> getRegioes() {
        return Collections.unmodifiableSet(regioes);
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }

    public List<Resultado> getResultados() {
        return Collections.unmodifiableList(resultados);
    }

    public int getTotalEntrevistas() {
        return resultados.get(0).getGeral().getTotalEntrevistas();
    }

    /** Resultado de uma pergunta, procurado pelo texto. */
    public Optional<Resultado> buscarResultado(String textoPergunta) {
        return resultados.stream().filter(r -> r.getTextoPergunta().equals(textoPergunta)).findFirst();
    }
}
