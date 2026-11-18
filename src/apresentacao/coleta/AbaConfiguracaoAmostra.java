package apresentacao.coleta;

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
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.coleta.Entrevistador;
import dominio.coleta.Participante;
import javafx.scene.Node;
import javafx.scene.control.TableView;

/** Cadastro de entrevistadores e participantes fictícios. */
class AbaConfiguracaoAmostra extends Aba {

    AbaConfiguracaoAmostra(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        TableView<Entrevistador> entrevistadores = tabelaEntrevistadores();
        TableView<Participante> participantes = tabelaParticipantes();
        return pagina(
                painel(titulo("Entrevistadores fictícios"),
                        linha(botao("Adicionar entrevistador", () -> editarEntrevistador(null)),
                                secundario("Editar", () -> editarEntrevistador(Leitura.selecionado(entrevistadores))),
                                perigo("Excluir", () -> excluir(Leitura.selecionado(entrevistadores)))),
                        entrevistadores),
                painel(titulo("Participantes fictícios"),
                        linha(botao("Novo participante", () -> editarParticipante(null)),
                                secundario("Editar", () -> editarParticipante(Leitura.selecionado(participantes))),
                                perigo("Excluir", () -> excluir(Leitura.selecionado(participantes)))),
                        participantes),
                aviso("As categorias demográficas vêm de Administração · Parâmetros. "
                        + "Dados vinculados a entrevistas concluídas são preservados."));
    }

    private TableView<Entrevistador> tabelaEntrevistadores() {
        TableView<Entrevistador> tabela = tabela();
        colunas(tabela,
                coluna("Código", e -> "E" + e.getId()),
                coluna("Nome", Entrevistador::getNome));
        tabela.getItems().setAll(coleta().listarEntrevistadores());
        return tabela;
    }

    private TableView<Participante> tabelaParticipantes() {
        TableView<Participante> tabela = tabela();
        colunas(tabela,
                coluna("Código", p -> "PT" + p.getId()),
                coluna("Nome fictício", Participante::getNome),
                coluna("Região", p -> p.getPerfilDemografico().getRegiao()),
                coluna("Faixa etária", p -> p.getPerfilDemografico().getFaixaEtaria()),
                coluna("Escolaridade", p -> p.getPerfilDemografico().getEscolaridade()),
                coluna("Renda", p -> p.getPerfilDemografico().getFaixaRenda()),
                coluna("Gênero", p -> p.getPerfilDemografico().getGenero()));
        tabela.getItems().setAll(coleta().listarParticipantes());
        return tabela;
    }

    private void editarEntrevistador(Entrevistador entrevistador) {
        new FormularioEntrevistador(janela(), coleta(), entrevistador).abrir();
        atualizar();
    }

    private void editarParticipante(Participante participante) {
        new FormularioParticipante(janela(), contexto, participante).abrir();
        atualizar();
    }

    private void excluir(Entrevistador entrevistador) {
        if (Dialogos.confirmar("Excluir o entrevistador " + entrevistador.getNome() + "?")) {
            coleta().excluirEntrevistador(entrevistador.getId());
            atualizar();
        }
    }

    private void excluir(Participante participante) {
        if (Dialogos.confirmar("Excluir o participante " + participante.getNome() + "?")) {
            coleta().excluirParticipante(participante.getId());
            atualizar();
        }
    }
}
