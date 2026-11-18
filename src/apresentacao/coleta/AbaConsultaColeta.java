package apresentacao.coleta;

import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.filtro;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.secundario;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.tabela;
import static apresentacao.componentes.Componentes.texto;
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.TipoParametro;
import dominio.base.Genero;
import dominio.base.PerfilDemografico;
import dominio.coleta.Entrevista;
import dominio.coleta.Resposta;
import dominio.pesquisa.Pesquisa;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/** Consulta das entrevistas concluídas, com filtros por pesquisa e características da amostra. */
class AbaConsultaColeta extends Aba {

    private final ComboBox<String> regiao = filtro(administracao().valores(TipoParametro.REGIAO));
    private final ComboBox<String> faixaEtaria = filtro(administracao().valores(TipoParametro.FAIXA_ETARIA));
    private final ComboBox<String> escolaridade = filtro(administracao().valores(TipoParametro.ESCOLARIDADE));
    private final ComboBox<String> renda = filtro(administracao().valores(TipoParametro.FAIXA_RENDA));
    private final ComboBox<String> genero = filtro(Genero.textos());
    private final ComboBox<Pesquisa> pesquisa = combo(pesquisas().listarTodas(), "Todas as pesquisas");

    AbaConsultaColeta(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        TableView<Entrevista> tabela = criarTabela();
        Label total = texto("");
        Runnable filtrar = () -> {
            String codigo = pesquisa.getValue() == null ? null : pesquisa.getValue().getCodigo();
            tabela.getItems().setAll(coleta().consultarEntrevistas(codigo, montarPerfilFiltro()));
            total.setText("Mostrando " + tabela.getItems().size() + " de "
                    + coleta().getBancoEntrevistas().size() + " entrevistas cadastradas.");
        };
        filtrar.run();

        return pagina(
                painel(linha(campo("Pesquisa", pesquisa), campo("Região", regiao), campo("Faixa etária", faixaEtaria),
                        campo("Escolaridade", escolaridade), campo("Renda", renda), campo("Gênero", genero),
                        botao("Filtrar", filtrar), secundario("Limpar filtros", this::atualizar))),
                painel(tabela, total,
                        secundario("Ver respostas", () -> verRespostas(Leitura.selecionado(tabela)))));
    }

    private PerfilDemografico montarPerfilFiltro() {
        return new PerfilDemografico(Leitura.valorFiltro(faixaEtaria), Leitura.valorFiltro(regiao),
                Leitura.valorFiltro(escolaridade), Leitura.valorFiltro(renda), Leitura.valorFiltro(genero));
    }

    private TableView<Entrevista> criarTabela() {
        TableView<Entrevista> tabela = tabela();
        tabela.setId("tabela-entrevistas");
        colunas(tabela,
                coluna("Pesquisa", e -> e.getPesquisa().getCodigo()),
                coluna("Nº", Entrevista::getIdUnico),
                coluna("Participante", e -> "PT" + e.getParticipante().getId()),
                coluna("Região", e -> e.getParticipante().getPerfilDemografico().getRegiao()),
                coluna("Faixa etária", e -> e.getParticipante().getPerfilDemografico().getFaixaEtaria()),
                coluna("Escolaridade", e -> e.getParticipante().getPerfilDemografico().getEscolaridade()),
                coluna("Renda", e -> e.getParticipante().getPerfilDemografico().getFaixaRenda()),
                coluna("Entrevistador", e -> "E" + e.getEntrevistador().getId()),
                coluna("Situação", e -> e.isConcluida() ? "Concluída" : "Em andamento"));
        return tabela;
    }

    private void verRespostas(Entrevista entrevista) {
        VBox detalhes = conteudo(titulo(entrevista.getParticipante().toString()),
                texto(entrevista.getParticipante().getPerfilDemografico().descrever()));
        for (Resposta resposta : entrevista.getRespostas()) {
            detalhes.getChildren().add(painel(texto(resposta.getPergunta().getTexto()),
                    texto(String.join(", ", resposta.getOpcoesEscolhidas()))));
        }
        Dialogos.visualizar(janela(), "Entrevista " + entrevista.getIdUnico() + " · "
                + entrevista.getPesquisa().getCodigo(), pagina(detalhes));
    }
}
