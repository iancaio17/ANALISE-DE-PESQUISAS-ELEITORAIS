package apresentacao.pesquisas;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.grade;

import aplicacao.SistemaContexto;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.TipoParametro;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.TipoPesquisa;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Janela de cadastro e edição dos dados gerais de uma pesquisa. */
class FormularioPesquisa {

    private final Window janela;
    private final SistemaContexto contexto;
    private final Pesquisa pesquisa;

    private final TextField codigo = entrada("P001");
    private final TextField nome = entrada("Nome da pesquisa");
    private final TextField abrangencia = entrada("Abrangência fictícia");
    private final TextField quantidade = entrada("30");
    private final TextField metodologia = entrada("Metodologia / plano amostral");
    private final ComboBox<Cargo> cargo = combo(Cargo.values(), "Selecione");
    private final ComboBox<TipoPesquisa> tipo = combo(TipoPesquisa.values(), "Selecione");
    private final DatePicker inicio = new DatePicker(LocalDate.now());
    private final DatePicker fim = new DatePicker(LocalDate.now().plusDays(30));
    private final ListView<String> regioes;

    private String codigoSalvo;

    /** @param pesquisa pesquisa a editar, ou {@code null} para cadastrar uma nova */
    FormularioPesquisa(Window janela, SistemaContexto contexto, Pesquisa pesquisa) {
        this.janela = janela;
        this.contexto = contexto;
        this.pesquisa = pesquisa;
        this.regioes = new ListView<>(FXCollections.observableArrayList(
                contexto.getAdministracao().valores(TipoParametro.REGIAO)));
        regioes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        regioes.setPrefHeight(120);
        if (pesquisa != null) {
            preencher(pesquisa);
        }
    }

    private void preencher(Pesquisa p) {
        codigo.setText(p.getCodigo());
        codigo.setEditable(false);
        nome.setText(p.getNome());
        abrangencia.setText(p.getAbrangencia());
        quantidade.setText(String.valueOf(p.getQuantidadePrevista()));
        metodologia.setText(p.getPlanoAmostral().getMetodologia());
        cargo.setValue(p.getCargo());
        tipo.setValue(p.getTipo());
        inicio.setValue(p.getInicioColeta());
        fim.setValue(p.getFimColeta());
        p.getRegioes().forEach(regioes.getSelectionModel()::select);
    }

    void abrir() {
        Dialogos.formulario(janela, pesquisa == null ? "Cadastro de pesquisa" : "Editar pesquisa", montar(),
                this::salvar);
    }

    /** Código da pesquisa gravada, se o formulário foi salvo. */
    Optional<String> codigoSalvo() {
        return Optional.ofNullable(codigoSalvo);
    }

    private Node montar() {
        GridPane grade = grade();
        campo(grade, 0, 0, "Código único", codigo);
        campo(grade, 0, 1, "Nome", nome);
        campo(grade, 1, 0, "Cargo", cargo);
        campo(grade, 1, 1, "Tipo de pesquisa", tipo);
        campo(grade, 2, 0, "Início da coleta", inicio);
        campo(grade, 2, 1, "Fim da coleta", fim);
        campo(grade, 3, 0, "Abrangência", abrangencia);
        campo(grade, 3, 1, "Entrevistas previstas", quantidade);
        return conteudo(grade, campo("Metodologia", metodologia),
                campo("Regiões (Ctrl + clique para selecionar várias)", regioes),
                aviso("Perguntas, candidatos e metas são configurados nas próximas abas, antes de iniciar a coleta."));
    }

    private void salvar() {
        contexto.getPesquisas().salvarPesquisa(codigo.getText(), pesquisa == null, nome.getText(),
                Leitura.obrigatorio(cargo, "o cargo"), inicio.getValue(), fim.getValue(), abrangencia.getText(),
                Leitura.inteiro(quantidade, "Entrevistas previstas"), Leitura.obrigatorio(tipo, "o tipo"),
                metodologia.getText(), List.copyOf(regioes.getSelectionModel().getSelectedItems()));
        codigoSalvo = codigo.getText().trim().toUpperCase(Locale.ROOT);
    }
}
