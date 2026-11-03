package aplicacao.servicos;

import dominio.pesquisa.Pesquisa;
import dominio.candidato.Candidato; // Assumindo a existência baseada na estrutura
import infraestrutura.excecoes.PesquisaRegraNegocioException;
import persistencia.repositorios.PesquisaRepository; // Assumindo a existência da interface no pacote correspondente
import java.util.List;

public class PesquisaService {

    private final PesquisaRepository repository;

    public PesquisaService(PesquisaRepository repository) {
        this.repository = repository;
    }

    public Pesquisa criarPesquisa(Pesquisa pesquisa) {
        if (repository.existePorCodigo(pesquisa.getCodigo())) {
            throw new PesquisaRegraNegocioException("Já existe uma pesquisa registada com este código.");
        }
        return repository.salvar(pesquisa);
    }

    public void associarCandidato(String codigoPesquisa, Candidato candidato) {
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);
        pesquisa.associarCandidato(candidato);
        repository.atualizar(pesquisa);
    }

    public void iniciarColeta(String codigoPesquisa) {
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);
        pesquisa.iniciarColeta();
        repository.atualizar(pesquisa);
    }

    public void encerrarColeta(String codigoPesquisa) {
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);
        pesquisa.encerrarColeta();
        repository.atualizar(pesquisa);
    }

    public void processarPesquisa(String codigoPesquisa) {
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);
        pesquisa.processarDados();
        repository.atualizar(pesquisa);
    }

    public List<Pesquisa> listarTodas() {
        return repository.buscarTodas();
    }

    public List<Pesquisa> consultarFiltros(String regiao, String cargo, String situacao) {
        return repository.buscarPorFiltros(regiao, cargo, situacao);
    }

    private Pesquisa buscarPesquisaPorCodigo(String codigo) {
        return repository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new PesquisaRegraNegocioException("Pesquisa não encontrada com o código: " + codigo));
    }
}