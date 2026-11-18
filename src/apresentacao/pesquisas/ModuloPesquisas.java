package apresentacao.pesquisas;

import aplicacao.SistemaContexto;
import apresentacao.base.ModuloView;

/** Módulo Pesquisas: consulta, questionário e amostra. Guarda a pesquisa em foco entre as abas. */
public class ModuloPesquisas extends ModuloView {

    private String codigoSelecionado;

    public ModuloPesquisas(SistemaContexto contexto) {
        super(contexto);
        adicionar("Consulta e acompanhamento", () -> new AbaConsultaPesquisas(this));
        adicionar("Perguntas", () -> new AbaPerguntas(this));
        adicionar("Amostra e candidatos", () -> new AbaAmostra(this));
    }

    String getCodigoSelecionado() {
        return codigoSelecionado;
    }

    void setCodigoSelecionado(String codigo) {
        this.codigoSelecionado = codigo;
    }
}
