package apresentacao.pesquisas;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.perigo;
import static apresentacao.componentes.Componentes.tabela;
import static apresentacao.componentes.Componentes.texto;
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.base.Aba;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Perfil;
import dominio.base.PerfilDemografico;
import dominio.candidato.Candidato;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;

/** Plano amostral (metas por grupo) e candidatos associados à pesquisa. */
class AbaAmostra extends Aba {

    /** Uma linha da tabela de metas: grupo, meta e quantas entrevistas já foram feitas. */
    private record LinhaMeta(PerfilDemografico perfil, int meta, long feitas) {
        String situacao() {
            return feitas < meta ? "Abaixo da meta" : "Atingida";
        }
    }

    private final ModuloPesquisas modulo;

    AbaAmostra(ModuloPesquisas modulo) {
        super(modulo);
        this.modulo = modulo;
    }

    @Override
    public Node construir() {
        ComboBox<Pesquisa> seletor = SeletorPesquisa.criar(modulo, pesquisas().listarTodas());
        if (seletor.getValue() == null) {
            return pagina(campo("Pesquisa", seletor), aviso("Nenhuma pesquisa cadastrada."));
        }
        Pesquisa pesquisa = seletor.getValue();

        TableView<LinhaMeta> metas = montarTabelaMetas(pesquisa);
        TableView<Candidato> candidatosDaPesquisa = montarTabelaCandidatos(pesquisa);

        FlowPane acoesMeta = linha(
                botao("Adicionar / editar meta", () -> editarMeta(pesquisa)),
                perigo("Remover meta", () -> removerMeta(pesquisa, Leitura.selecionado(metas))));
        ComboBox<Candidato> disponiveis = combo(candidatosDisponiveis(pesquisa), "Selecione o candidato");
        FlowPane acoesCandidato = linha(
                campo("Associar candidato", disponiveis),
                botao("Associar", () -> associar(pesquisa, Leitura.obrigatorio(disponiveis, "o candidato"))),
                perigo("Remover associação", () -> desassociar(pesquisa, Leitura.selecionado(candidatosDaPesquisa))));

        boolean bloqueado = !permite(Perfil.PESQUISADOR) || pesquisa.getSituacao() != SituacaoPesquisa.PLANEJADA;
        acoesMeta.setDisable(bloqueado);
        acoesCandidato.setDisable(bloqueado);

        return pagina(
                painel(campo("Pesquisa", seletor),
                        texto("Metodologia: " + pesquisa.getPlanoAmostral().getMetodologia())),
                painel(titulo("Plano amostral"), acoesMeta, metas),
                painel(titulo("Candidatos associados"), acoesCandidato, candidatosDaPesquisa),
                aviso("Somente candidatos do mesmo cargo e em situação permitida podem ser associados. "
                        + "Metas de grupos sobrepostos são acompanhadas de forma independente."));
    }

    private TableView<LinhaMeta> montarTabelaMetas(Pesquisa pesquisa) {
        Map<PerfilDemografico, Long> realizadas = pesquisas().realizadasPorMeta(pesquisa.getCodigo());
        TableView<LinhaMeta> tabela = tabela();
        colunas(tabela,
                coluna("Grupo da amostra", linha -> linha.perfil().descrever()),
                coluna("Meta", LinhaMeta::meta),
                coluna("Feitas", LinhaMeta::feitas),
                coluna("Situação", LinhaMeta::situacao));
        pesquisa.getPlanoAmostral().getMetasPorPerfil().forEach((perfil, meta) ->
                tabela.getItems().add(new LinhaMeta(perfil, meta, realizadas.getOrDefault(perfil, 0L))));
        return tabela;
    }

    private TableView<Candidato> montarTabelaCandidatos(Pesquisa pesquisa) {
        TableView<Candidato> tabela = tabela();
        colunas(tabela,
                coluna("Candidato", Candidato::getNome),
                coluna("Número", Candidato::getNumeroIdentificador),
                coluna("Cargo", Candidato::getCargo));
        tabela.getItems().setAll(pesquisa.getCandidatos());
        return tabela;
    }

    private List<Candidato> candidatosDisponiveis(Pesquisa pesquisa) {
        return candidatos().getBancoCandidatos().stream()
                .filter(c -> c.getCargo() == pesquisa.getCargo())
                .filter(Candidato::podeParticiparDePesquisa)
                .filter(c -> !pesquisa.possuiCandidato(c.getId()))
                .toList();
    }

    private void editarMeta(Pesquisa pesquisa) {
        new FormularioMeta(janela(), contexto, pesquisa).abrir();
        atualizar();
    }

    private void removerMeta(Pesquisa pesquisa, LinhaMeta meta) {
        if (Dialogos.confirmar("Remover esta meta do plano?")) {
            pesquisas().removerMeta(pesquisa.getCodigo(), meta.perfil());
            atualizar();
        }
    }

    private void associar(Pesquisa pesquisa, Candidato candidato) {
        pesquisas().associarCandidato(pesquisa.getCodigo(), candidato.getId());
        atualizar();
    }

    private void desassociar(Pesquisa pesquisa, Candidato candidato) {
        if (Dialogos.confirmar("Remover associação com " + candidato.getNome() + "?")) {
            pesquisas().removerCandidato(pesquisa.getCodigo(), candidato.getId());
            atualizar();
        }
    }
}
