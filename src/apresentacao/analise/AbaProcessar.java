package apresentacao.analise;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.secundario;
import static apresentacao.componentes.Componentes.tabela;

import aplicacao.servicos.FiltroPesquisa;
import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** Processamento das pesquisas encerradas. Pesquisas já processadas podem ser processadas de novo. */
class AbaProcessar extends Aba {

    AbaProcessar(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        TextField busca = entrada("Nome ou código");
        ComboBox<Cargo> cargo = combo(Cargo.values(), "Todos os cargos");
        ComboBox<SituacaoPesquisa> situacao = combo(SituacaoPesquisa.values(), "Todas as situações");
        TableView<Pesquisa> tabela = criarTabela();
        Label mensagem = aviso("Selecione uma pesquisa encerrada ou processada.");

        Runnable filtrar = () -> tabela.getItems().setAll(pesquisas().consultar(
                new FiltroPesquisa().texto(busca.getText()).cargo(cargo.getValue()).situacao(situacao.getValue())));
        filtrar.run();

        Button executar = botao("Processar / processar novamente", () -> {
            Pesquisa pesquisa = Leitura.selecionado(tabela);
            boolean jaProcessada = pesquisa.getSituacao() == SituacaoPesquisa.PROCESSADA;
            if (jaProcessada && !Dialogos.confirmar("Substituir os resultados processados de "
                    + pesquisa.getCodigo() + "?")) {
                return;
            }
            analise().processarPesquisa(pesquisa.getCodigo());
            filtrar.run();
            mensagem.setText("Pesquisa " + pesquisa.getCodigo()
                    + " processada com sucesso. Consulte a aba Resultados.");
        });
        executar.setDisable(true);
        tabela.getSelectionModel().selectedItemProperty().addListener((observado, anterior, atual) ->
                executar.setDisable(atual == null || !atual.getSituacao().permiteProcessamento()));

        return pagina(
                aviso("Somente pesquisas com a coleta encerrada podem ser processadas. "
                        + "O processamento salva os resultados e a situação da pesquisa juntos."),
                painel(linha(campo("Buscar", busca), campo("Cargo", cargo), campo("Situação", situacao),
                        botao("Filtrar", filtrar), secundario("Limpar filtros", this::atualizar))),
                painel(tabela, executar),
                mensagem);
    }

    private TableView<Pesquisa> criarTabela() {
        TableView<Pesquisa> tabela = tabela();
        tabela.setId("tabela-processar");
        colunas(tabela,
                coluna("Código", Pesquisa::getCodigo),
                coluna("Nome", Pesquisa::getNome),
                coluna("Cargo", Pesquisa::getCargo),
                coluna("Situação", Pesquisa::getSituacao),
                coluna("Entrevistas", p -> p.getQuantidadeRealizada() + " / " + p.getQuantidadePrevista()),
                coluna("Conclusão", p -> Leitura.percentual(p.getPercentualConclusao()) + "%"));
        return tabela;
    }
}
