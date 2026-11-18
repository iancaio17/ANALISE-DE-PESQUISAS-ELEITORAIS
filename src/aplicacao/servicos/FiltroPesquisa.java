package aplicacao.servicos;

import dominio.pesquisa.Cargo;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import dominio.pesquisa.TipoPesquisa;
import infraestrutura.utils.Validacao;
import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Critérios combinados para consultar pesquisas (código ou nome, região, cargo, tipo, situação,
 * período e quantidade de entrevistados). Critério não informado (nulo ou vazio) não filtra.
 */
public class FiltroPesquisa implements Predicate<Pesquisa> {

    private String texto = "";
    private String regiao = "";
    private Cargo cargo;
    private TipoPesquisa tipo;
    private SituacaoPesquisa situacao;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private int minimoEntrevistados;
    private int maximoEntrevistados = Integer.MAX_VALUE;
    private boolean apenasAbaixoDaMeta;

    public FiltroPesquisa texto(String texto) {
        this.texto = texto == null ? "" : texto;
        return this;
    }

    public FiltroPesquisa regiao(String regiao) {
        this.regiao = regiao == null ? "" : regiao;
        return this;
    }

    public FiltroPesquisa cargo(Cargo cargo) {
        this.cargo = cargo;
        return this;
    }

    public FiltroPesquisa tipo(TipoPesquisa tipo) {
        this.tipo = tipo;
        return this;
    }

    public FiltroPesquisa situacao(SituacaoPesquisa situacao) {
        this.situacao = situacao;
        return this;
    }

    public FiltroPesquisa periodo(LocalDate inicio, LocalDate fim) {
        Validacao.exigir(inicio == null || fim == null || !fim.isBefore(inicio), "Período de filtro inválido.");
        this.periodoInicio = inicio;
        this.periodoFim = fim;
        return this;
    }

    /** Intervalo de entrevistas realizadas (entrevistados). */
    public FiltroPesquisa entrevistados(int minimo, int maximo) {
        Validacao.exigir(minimo >= 0 && maximo >= minimo, "Intervalo de quantidade inválido.");
        this.minimoEntrevistados = minimo;
        this.maximoEntrevistados = maximo;
        return this;
    }

    public FiltroPesquisa apenasAbaixoDaMeta(boolean apenasAbaixoDaMeta) {
        this.apenasAbaixoDaMeta = apenasAbaixoDaMeta;
        return this;
    }

    public boolean isApenasAbaixoDaMeta() {
        return apenasAbaixoDaMeta;
    }

    /** Aplica todos os critérios, exceto o das metas por grupo (que depende das entrevistas). */
    @Override
    public boolean test(Pesquisa pesquisa) {
        return contemTexto(pesquisa)
                && (regiao.isBlank() || pesquisa.getRegioes().contains(regiao))
                && (cargo == null || pesquisa.getCargo() == cargo)
                && (tipo == null || pesquisa.getTipo() == tipo)
                && (situacao == null || pesquisa.getSituacao() == situacao)
                && (periodoInicio == null || !pesquisa.getFimColeta().isBefore(periodoInicio))
                && (periodoFim == null || !pesquisa.getInicioColeta().isAfter(periodoFim))
                && pesquisa.getQuantidadeRealizada() >= minimoEntrevistados
                && pesquisa.getQuantidadeRealizada() <= maximoEntrevistados;
    }

    private boolean contemTexto(Pesquisa pesquisa) {
        String procurado = Validacao.chave(texto);
        return Validacao.chave(pesquisa.getNome() + " " + pesquisa.getCodigo()).contains(procurado);
    }
}
