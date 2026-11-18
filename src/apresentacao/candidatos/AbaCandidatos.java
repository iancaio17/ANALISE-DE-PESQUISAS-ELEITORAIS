package apresentacao.candidatos;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.perigo;
import static apresentacao.componentes.Componentes.secundario;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Perfil;
import dominio.candidato.Candidato;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;

/** Cadastro de candidatos (alterações somente pelo administrador). */
class AbaCandidatos extends Aba {

    AbaCandidatos(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        TableView<Candidato> tabela = TabelasCandidato.criarTabelaCandidatos();
        tabela.setId("tabela-candidatos");
        tabela.getItems().setAll(candidatos().getBancoCandidatos());

        FlowPane acoes = linha(
                botao("Novo candidato", () -> editar(null)),
                secundario("Editar", () -> editar(Leitura.selecionado(tabela))),
                perigo("Excluir", () -> excluir(Leitura.selecionado(tabela))));
        acoes.setDisable(!permite(Perfil.ADMINISTRADOR));

        return pagina(painel(acoes, tabela),
                aviso("Cadastre somente candidatos fictícios. Pré-candidato e Inativa não são situações "
                        + "permitidas para associação a pesquisas."));
    }

    private void editar(Candidato candidato) {
        new FormularioCandidato(janela(), candidatos(), candidato).abrir();
        atualizar();
    }

    private void excluir(Candidato candidato) {
        if (Dialogos.confirmar("Excluir o candidato " + candidato.getNome() + "?")) {
            candidatos().excluirCandidato(candidato.getId());
            atualizar();
        }
    }
}
