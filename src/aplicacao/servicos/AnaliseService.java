package aplicacao.servicos;

import dominio.analise.Analise;
import dominio.analise.ComparacaoPesquisa;
import dominio.analise.CriterioAgrupamento;
import dominio.analise.CriterioComposto;
import dominio.analise.CriterioDemografico;
import dominio.analise.ProcessadorEstatistico;
import dominio.analise.Relatorio;
import dominio.analise.RelatorioComparativo;
import dominio.analise.RelatorioConsolidado;
import dominio.analise.Resultado;
import dominio.coleta.Entrevista;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import infraestrutura.excecoes.PesquisaNaoEncerradaException;
import infraestrutura.excecoes.PesquisaNaoProcessadaException;
import infraestrutura.excecoes.SemRespostasValidasException;
import infraestrutura.utils.ExportadorFactory;
import persistencia.repositorios.AnaliseRepositorio;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Casos de uso do módulo de Análise Estatística.
 * Dados de entrevistas são obtidos SOMENTE pelo ColetaService (regra de convivência do grupo).
 */
public class AnaliseService {

    private static final String ENCERRADA = "Encerrada";
    private static final String PROCESSADA = "Processada";

    private final AnaliseRepositorio repositorio;
    private final ColetaService coletaService;
    private final ProcessadorEstatistico processador;
    private List<CriterioAgrupamento> criterios;

    public AnaliseService(AnaliseRepositorio repositorio, ColetaService coletaService) {
        this(repositorio, coletaService, true);
    }

    public AnaliseService(AnaliseRepositorio repositorio, ColetaService coletaService, boolean usarConcorrencia) {
        if (repositorio == null || coletaService == null) {
            throw new IllegalArgumentException("Repositório e serviço de coleta são obrigatórios.");
        }
        this.repositorio = repositorio;
        this.coletaService = coletaService;
        this.processador = new ProcessadorEstatistico(usarConcorrencia);
        this.criterios = criteriosPadrao();
    }

    private static List<CriterioAgrupamento> criteriosPadrao() {
        List<CriterioAgrupamento> lista = new ArrayList<CriterioAgrupamento>(Arrays.asList(CriterioDemografico.values()));
        lista.add(new CriterioComposto(CriterioDemografico.REGIAO, CriterioDemografico.FAIXA_ETARIA)); // distribuição conjunta
        return lista;
    }

    /** Permite à tela escolher quais critérios de agrupamento usar nos próximos processamentos. */
    public void definirCriterios(List<CriterioAgrupamento> novos) {
        if (novos == null) throw new IllegalArgumentException("Lista de critérios inválida.");
        this.criterios = new ArrayList<>(novos);
    }

    /**
     * Processa (ou REPROCESSA) uma pesquisa com coleta encerrada, salva o resultado e marca a pesquisa como "Processada".
     */
    public Analise processarPesquisa(Pesquisa pesquisa) {
        exigirEncerradaOuProcessada(pesquisa);
        List<Resultado> resultados = calcular(pesquisa, e -> true);
        Analise analise = new Analise(pesquisa, resultados);
        repositorio.salvar(analise);          // só altera a situação depois de calcular e salvar com sucesso
        pesquisa.setSituacao(PROCESSADA);
        return analise;
    }

    /** Calcula estatísticas com FILTRO (ex.: só uma região) sem salvar e sem alterar a pesquisa. */
    public Analise analisarComFiltro(Pesquisa pesquisa, Predicate<Entrevista> filtro) {
        exigirEncerradaOuProcessada(pesquisa);
        if (filtro == null) throw new IllegalArgumentException("Filtro inválido.");
        return new Analise(pesquisa, calcular(pesquisa, filtro));
    }

    public Analise buscarAnalise(String codigoPesquisa) {
        return repositorio.buscarPorCodigoPesquisa(codigoPesquisa)
                .orElseThrow(() -> new PesquisaNaoProcessadaException(codigoPesquisa));
    }

    public List<Analise> listarAnalises() {
        return repositorio.listarTodas();
    }

    public ComparacaoPesquisa compararPesquisas(String codigoA, String codigoB) {
        return new ComparacaoPesquisa(buscarAnalise(codigoA), buscarAnalise(codigoB));
    }

    public RelatorioConsolidado gerarRelatorioConsolidado(String codigoPesquisa) {
        return new RelatorioConsolidado(buscarAnalise(codigoPesquisa));
    }

    public RelatorioComparativo gerarRelatorioComparativo(String codigoA, String codigoB) {
        return new RelatorioComparativo(compararPesquisas(codigoA, codigoB));
    }

    /** Exporta qualquer relatório para "csv" ou "pdf". */
    public void exportarRelatorio(Relatorio relatorio, String formato, Path destino) {
        ExportadorFactory.criar(formato).exportar(relatorio, destino);
    }

    // ------------------------------------------------------------------ internos

    private void exigirEncerradaOuProcessada(Pesquisa pesquisa) {
        if (pesquisa == null) throw new IllegalArgumentException("Pesquisa inválida para processamento.");
        String s = pesquisa.getSituacao();
        if (!ENCERRADA.equalsIgnoreCase(s) && !PROCESSADA.equalsIgnoreCase(s)) {
            throw new PesquisaNaoEncerradaException(pesquisa.getCodigo(), s);
        }
    }

    private List<Resultado> calcular(Pesquisa pesquisa, Predicate<Entrevista> filtro) {
        if (pesquisa.getPerguntas().isEmpty()) {
            throw new SemRespostasValidasException("A pesquisa '" + pesquisa.getCodigo() + "' não possui perguntas cadastradas.");
        }
        List<Entrevista> entrevistas = coletaService.getEntrevistasPorPesquisa(pesquisa.getCodigo()).stream()
                .filter(Entrevista::isConcluida)
                .filter(filtro)
                .collect(Collectors.toList());
        if (entrevistas.isEmpty()) {
            throw new SemRespostasValidasException("Não há entrevistas concluídas"
                    + " (para o filtro aplicado) na pesquisa '" + pesquisa.getCodigo() + "'.");
        }
        List<Resultado> resultados = new ArrayList<>();
        for (Pergunta p : pesquisa.getPerguntas()) {
            resultados.add(processador.processarPergunta(pesquisa.getCodigo(), p, entrevistas, criterios));
        }
        return resultados;
    }
}
