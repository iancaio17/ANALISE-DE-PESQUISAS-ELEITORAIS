package apresentacao.admin;

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
import static apresentacao.componentes.Componentes.secundario;
import static apresentacao.componentes.Componentes.tabela;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Parametro;
import dominio.admin.TipoParametro;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

/** Categorias usadas nos cadastros (regiões, faixas etárias, escolaridades e faixas de renda). */
class AbaParametros extends Aba {

    AbaParametros(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        ComboBox<TipoParametro> tipo = combo(TipoParametro.values(), "Todos os tipos");
        TableView<Parametro> tabela = tabela();
        colunas(tabela,
                coluna("ID", Parametro::getId),
                coluna("Categoria", Parametro::getTipo),
                coluna("Valor", Parametro::getValor));

        Runnable carregar = () -> tabela.getItems().setAll(administracao().listarParametros().stream()
                .filter(parametro -> tipo.getValue() == null || parametro.getTipo() == tipo.getValue())
                .toList());
        carregar.run();
        tipo.setOnAction(evento -> carregar.run());

        return pagina(
                painel(linha(campo("Tipo de parâmetro", tipo),
                        secundario("Todos", () -> {
                            tipo.setValue(null);
                            carregar.run();
                        }),
                        botao("Adicionar parâmetro", () -> editar(null)),
                        secundario("Editar", () -> editar(Leitura.selecionado(tabela))),
                        perigo("Excluir", () -> excluir(Leitura.selecionado(tabela)))), tabela),
                aviso("Categorias já usadas por participantes, pesquisas ou metas são preservadas. "
                        + "Cadastre uma nova categoria para ampliar as opções."));
    }

    private void editar(Parametro parametro) {
        new FormularioParametro(janela(), administracao(), parametro).abrir();
        atualizar();
    }

    private void excluir(Parametro parametro) {
        if (Dialogos.confirmar("Excluir a categoria " + parametro.getValor() + "?")) {
            administracao().excluirParametro(parametro.getId());
            atualizar();
        }
    }
}
