package persistencia.repositorios;

import dominio.pesquisa.Pesquisa;

import java.util.List;
import java.util.Optional;

public interface PesquisaRepository {

    boolean existePorCodigo(String codigo);

    Pesquisa salvar(Pesquisa pesquisa);

    void atualizar(Pesquisa pesquisa);

    List<Pesquisa> buscarTodas();

    List<Pesquisa> buscarPorFiltros(String regiao, String cargo, String situacao);

    Optional<Pesquisa> buscarPorCodigo(String codigo);
}