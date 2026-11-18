package apresentacao.candidatos;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.perigo;
import static apresentacao.componentes.Componentes.secundario;
import static apresentacao.componentes.Componentes.tabela;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Perfil;
import dominio.candidato.Candidato;
import dominio.candidato.Partido;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;

/** Cadastro de partidos (alterações somente pelo administrador). */
class AbaPartidos extends Aba {

    AbaPartidos(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        List<Candidato> todos = candidatos().getBancoCandidatos();
        TableView<Partido> tabela = tabela();
        tabela.setId("tabela-partidos");
        colunas(tabela,
                coluna("Nome", Partido::getNome),
                coluna("Sigla", Partido::getSigla),
                coluna("Número", Partido::getNumero),
                coluna("Situação", Partido::getSituacao),
                coluna("Candidatos", p -> todos.stream().filter(c -> c.pertenceAoPartido(p.getId())).count()));
        tabela.getItems().setAll(candidatos().getBancoPartidos());

        FlowPane acoes = linha(
                botao("Novo partido", () -> editar(null)),
                secundario("Editar", () -> editar(Leitura.selecionado(tabela))),
                perigo("Excluir", () -> excluir(Leitura.selecionado(tabela))));
        acoes.setDisable(!permite(Perfil.ADMINISTRADOR));

        return pagina(painel(acoes, tabela),
                aviso("Sigla e número são únicos. Um partido só pode ser excluído depois que seus candidatos "
                        + "forem desvinculados."));
    }

    private void editar(Partido partido) {
        new FormularioPartido(janela(), candidatos(), partido).abrir();
        atualizar();
    }

    private void excluir(Partido partido) {
        if (Dialogos.confirmar("Excluir o partido " + partido.getSigla() + "?")) {
            candidatos().excluirPartido(partido.getId());
            atualizar();
        }
    }
}
