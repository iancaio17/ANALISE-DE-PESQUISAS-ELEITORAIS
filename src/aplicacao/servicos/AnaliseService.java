package aplicacao.servicos;

import aplicacao.SistemaContexto;
import dominio.admin.Perfil;
import dominio.analise.Analise;
import dominio.analise.ComparacaoPesquisa;
import dominio.analise.CriterioAgrupamento;
import dominio.analise.CriterioComposto;
import dominio.analise.CriterioDemografico;
import dominio.analise.Distribuicao;
import dominio.analise.ProcessadorEstatistico;
import dominio.analise.Relatorio;
import dominio.analise.RelatorioComparativo;
import dominio.analise.RelatorioConsolidado;
import dominio.analise.RelatorioResultado;
import dominio.analise.Resultado;
import dominio.coleta.Entrevista;
import dominio.pesquisa.TipoPergunta;
import dominio.pesquisa.Pesquisa;
import infraestrutura.excecoes.PesquisaNaoEncerradaException;
import infraestrutura.excecoes.PesquisaNaoProcessadaException;
import infraestrutura.excecoes.SemRespostasValidasException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import persistencia.EstadoSistema;

/** Módulo Análise Estatística e Relatórios: processamento, resultados, comparações e exportação. */
public class AnaliseService extends ServicoBase {

    public static final String CRITERIO_GERAL = "Geral";

    private final ProcessadorEstatistico processador = new ProcessadorEstatistico(false);

    public AnaliseService(SistemaContexto contexto) {
        super(contexto);
    }

    // ------------------------------------------------------------------ processamento

    /**
     * Processa uma pesquisa encerrada: calcula os resultados e muda a situação para "processada".
     * Resultado e nova situação são gravados juntos.
     */
    public Analise processarPesquisa(String codigo) {
        alterar(Perfil.ANALISTA, estado -> {
            Pesquisa pesquisa = estado.exigirPesquisa(codigo);
            exigirProcessavel(pesquisa);
            Analise analise = calcular(estado, pesquisa, entrevista -> true);
            pesquisa.processarDados();
            estado.getAnalises().put(pesquisa.getCodigo(), analise);
        });
        return buscarAnalise(codigo);
    }

    /** Resultado salvo no último processamento da pesquisa. */
    public Analise buscarAnalise(String codigo) {
        EstadoSistema estado = ler(Perfil.ANALISTA);
        Pesquisa pesquisa = estado.exigirPesquisa(codigo);
        Analise analise = estado.getAnalises().get(pesquisa.getCodigo());
        if (analise == null) {
            throw new PesquisaNaoProcessadaException(codigo);
        }
        return analise;
    }

    public List<Analise> listarAnalises() {
        return List.copyOf(ler(Perfil.ANALISTA).getAnalises().values());
    }

    /** Recalcula os resultados apenas com as entrevistas do filtro, sem alterar a análise salva. */
    public Analise analisarComFiltro(String codigo, Predicate<Entrevista> filtro) {
        EstadoSistema estado = ler(Perfil.ANALISTA);
        Pesquisa pesquisa = estado.exigirPesquisa(codigo);
        if (!estado.getAnalises().containsKey(pesquisa.getCodigo())) {
            throw new PesquisaNaoProcessadaException(codigo);
        }
        return calcular(estado, pesquisa, Objects.requireNonNull(filtro));
    }

    /** Usa a análise salva quando não há filtros; caso contrário, recalcula com o filtro. */
    public Analise analisar(String codigo, FiltroEntrevista filtro) {
        return filtro.estaVazio() ? buscarAnalise(codigo) : analisarComFiltro(codigo, filtro);
    }

    private static void exigirProcessavel(Pesquisa pesquisa) {
        if (!pesquisa.getSituacao().permiteProcessamento()) {
            throw new PesquisaNaoEncerradaException(pesquisa.getCodigo(), pesquisa.getSituacao().toString());
        }
    }

    private Analise calcular(EstadoSistema estado, Pesquisa pesquisa, Predicate<Entrevista> filtro) {
        exigirProcessavel(pesquisa);
        List<Entrevista> entrevistas = estado.entrevistasDaPesquisa(pesquisa.getCodigo()).stream()
                .filter(Entrevista::isConcluida)
                .filter(filtro)
                .toList();
        if (entrevistas.isEmpty()) {
            throw new SemRespostasValidasException("Nenhuma entrevista concluída corresponde aos filtros.");
        }
        List<Resultado> resultados = pesquisa.getPerguntas().stream()
                .map(pergunta -> processador.processarPergunta(
                        pesquisa.getCodigo(), pergunta, entrevistas, criterios()))
                .toList();
        return new Analise(pesquisa, resultados);
    }

    // ------------------------------------------------------------------ critérios de agrupamento

    /** Cada característica da amostra e todas as combinações em pares (distribuição conjunta). */
    private static List<CriterioAgrupamento> criterios() {
        CriterioDemografico[] simples = CriterioDemografico.values();
        List<CriterioAgrupamento> lista = new ArrayList<>(List.of(simples));
        for (int i = 0; i < simples.length; i++) {
            for (int j = i + 1; j < simples.length; j++) {
                lista.add(new CriterioComposto(simples[i], simples[j]));
            }
        }
        return lista;
    }

    public List<String> criteriosDisponiveis() {
        return criterios().stream().map(CriterioAgrupamento::getNome).toList();
    }

    // ------------------------------------------------------------------ comparação e relatórios

    public ComparacaoPesquisa compararPesquisas(String codigoA, String codigoB) {
        return new ComparacaoPesquisa(buscarAnalise(codigoA), buscarAnalise(codigoB));
    }

    public RelatorioConsolidado gerarRelatorioConsolidado(String codigo) {
        return new RelatorioConsolidado(buscarAnalise(codigo));
    }

    public RelatorioComparativo gerarRelatorioComparativo(String codigoA, String codigoB) {
        return new RelatorioComparativo(compararPesquisas(codigoA, codigoB));
    }

    /** Relatório exportável de uma pergunta (visão geral e, se pedido, um agrupamento). */
    public RelatorioResultado gerarRelatorioResultado(String codigo, Resultado resultado, String criterio,
            String descricaoFiltros) {
        List<Distribuicao> distribuicoes = new ArrayList<>();
        distribuicoes.add(resultado.getGeral());
        if (!CRITERIO_GERAL.equals(criterio)) {
            distribuicoes.addAll(resultado.getPorCriterio(criterio));
        }
        boolean baseSaoSelecoes = resultado.getTipoPergunta() == TipoPergunta.MULTIPLA_ESCOLHA;
        return new RelatorioResultado(codigo, descricaoFiltros, baseSaoSelecoes,
                buscarAnalise(codigo).getDataProcessamento(), distribuicoes);
    }

    public void exportarRelatorio(Relatorio relatorio, String formato, Path destino) {
        exportar(Perfil.ANALISTA, relatorio, formato, destino);
    }
}
