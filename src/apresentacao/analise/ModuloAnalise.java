package apresentacao.analise;

import aplicacao.SistemaContexto;
import apresentacao.base.ModuloView;

/** Módulo Análise Estatística e Relatórios. */
public class ModuloAnalise extends ModuloView {

    public ModuloAnalise(SistemaContexto contexto) {
        super(contexto);
        adicionar("Processar pesquisa", () -> new AbaProcessar(this));
        adicionar("Resultados", () -> new AbaResultados(this));
        adicionar("Comparar pesquisas", () -> new AbaComparar(this));
        adicionar("Relatórios e gráficos", () -> new AbaRelatorios(this));
    }
}
