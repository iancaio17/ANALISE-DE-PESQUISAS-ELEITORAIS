package dominio.analise;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Relatório do resultado de uma pergunta com filtros aplicados (visão exportável da tela de Resultados). */
public class RelatorioResultado extends Relatorio {

    private final String codigoPesquisa;
    private final String descricaoFiltros;
    private final boolean baseSaoSelecoes;
    private final LocalDateTime processadaEm;
    private final List<Distribuicao> distribuicoes;

    public RelatorioResultado(String codigoPesquisa, String descricaoFiltros, boolean baseSaoSelecoes,
            LocalDateTime processadaEm, List<Distribuicao> distribuicoes) {
        this.codigoPesquisa = codigoPesquisa;
        this.descricaoFiltros = descricaoFiltros;
        this.baseSaoSelecoes = baseSaoSelecoes;
        this.processadaEm = processadaEm;
        this.distribuicoes = List.copyOf(distribuicoes);
    }

    @Override
    public String getTitulo() {
        return "Resultado - " + codigoPesquisa;
    }

    @Override
    public List<String[]> gerarLinhas() {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[] {"RESULTADO FILTRADO · PESQUISA SIMULADA"});
        linhas.add(new String[] {"Filtros", descricaoFiltros});
        linhas.add(new String[] {"Base", baseSaoSelecoes
                ? "Seleções válidas (pode haver várias por entrevista)" : "Respostas válidas"});
        linhas.add(new String[] {"Processamento original", processadaEm.toString()});
        for (Distribuicao distribuicao : distribuicoes) {
            linhas.add(new String[] {distribuicao.getCriterio(), distribuicao.getGrupo()});
            adicionarDistribuicao(linhas, distribuicao);
            linhas.add(new String[0]);
        }
        return linhas;
    }

    @Override
    public List<Grafico> gerarGraficos() {
        List<Grafico> graficos = new ArrayList<>();
        for (Distribuicao distribuicao : distribuicoes) {
            graficos.add(Grafico.deDistribuicao(distribuicao.getCriterio() + " · " + distribuicao.getGrupo(),
                    distribuicao));
        }
        return graficos;
    }
}
