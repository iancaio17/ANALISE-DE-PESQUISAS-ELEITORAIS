package persistencia.repositorios;

import dominio.analise.Analise;

import java.util.List;
import java.util.Optional;

/** Contrato de persistência das análises processadas. A camada de aplicação só conhece esta interface. */
public interface AnaliseRepositorio {

    /** Salva a análise; se já existir uma para a mesma pesquisa, ela é substituída (reprocessamento). */
    void salvar(Analise analise);

    Optional<Analise> buscarPorCodigoPesquisa(String codigoPesquisa);

    List<Analise> listarTodas();
}
