package apresentacao.pesquisas;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.perigo;
import static apresentacao.componentes.Componentes.secundario;
import static apresentacao.componentes.Componentes.tabela;

import apresentacao.base.Aba;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Perfil;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;

/** Questionário da pesquisa. Só pode ser alterado enquanto a pesquisa está planejada. */
class AbaPerguntas extends Aba {

    private final ModuloPesquisas modulo;

    AbaPerguntas(ModuloPesquisas modulo) {
        super(modulo);
        this.modulo = modulo;
    }

    @Override
    public Node construir() {
        ComboBox<Pesquisa> seletor = SeletorPesquisa.criar(modulo, pesquisas().listarTodas());
        if (seletor.getValue() == null) {
            return pagina(campo("Pesquisa", seletor), aviso("Cadastre uma pesquisa para configurar seu questionário."));
        }
        Pesquisa pesquisa = seletor.getValue();

        TableView<Pergunta> tabela = tabela();
        colunas(tabela,
                coluna("Pergunta", Pergunta::getTexto),
                coluna("Tipo", Pergunta::getTipo),
                coluna("Obrigatória", pergunta -> pergunta.isObrigatoria() ? "Sim" : "Não"),
                coluna("Opções", pergunta -> pergunta.getOpcoes().stream().map(OpcaoResposta::getTexto).toList()));
        tabela.getItems().setAll(pesquisa.getPerguntas());

        FlowPane acoes = linha(
                botao("Adicionar pergunta", () -> editar(pesquisa, null)),
                secundario("Editar", () -> editar(pesquisa, Leitura.selecionado(tabela))),
                perigo("Excluir", () -> excluir(pesquisa, Leitura.selecionado(tabela))));
        acoes.setDisable(!permite(Perfil.PESQUISADOR) || pesquisa.getSituacao() != SituacaoPesquisa.PLANEJADA);

        return pagina(painel(campo("Pesquisa", seletor)), painel(acoes, tabela),
                aviso("As respostas especiais Não sabe, Não respondeu e Nenhuma das opções ficam disponíveis "
                        + "automaticamente durante a entrevista."));
    }

    private void editar(Pesquisa pesquisa, Pergunta pergunta) {
        new FormularioPergunta(janela(), pesquisas(), pesquisa, pergunta).abrir();
        atualizar();
    }

    private void excluir(Pesquisa pesquisa, Pergunta pergunta) {
        if (Dialogos.confirmar("Excluir a pergunta selecionada?")) {
            pesquisas().excluirPergunta(pesquisa.getCodigo(), pergunta.getId());
            atualizar();
        }
    }
}
