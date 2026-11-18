package apresentacao.pesquisas;

import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.secundario;

import aplicacao.servicos.PesquisaService;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.candidato.Candidato;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.TipoPergunta;
import infraestrutura.utils.Validacao;
import java.util.UUID;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/** Janela de cadastro e edição de uma pergunta e das suas opções de resposta. */
class FormularioPergunta {

    private final Window janela;
    private final PesquisaService servico;
    private final Pesquisa pesquisa;
    private final Pergunta pergunta;

    private final TextField texto = entrada("Texto da pergunta");
    private final ComboBox<TipoPergunta> tipo = combo(TipoPergunta.values(), "Selecione");
    private final CheckBox obrigatoria = new CheckBox("Resposta obrigatória");
    private final TextArea opcoes = new TextArea();

    /** @param pergunta pergunta a editar, ou {@code null} para cadastrar uma nova */
    FormularioPergunta(Window janela, PesquisaService servico, Pesquisa pesquisa, Pergunta pergunta) {
        this.janela = janela;
        this.servico = servico;
        this.pesquisa = pesquisa;
        this.pergunta = pergunta;

        tipo.setValue(pergunta == null ? TipoPergunta.UNICA_ESCOLHA : pergunta.getTipo());
        obrigatoria.setSelected(pergunta == null || pergunta.isObrigatoria());
        opcoes.setPromptText("Uma opção por linha");
        opcoes.setPrefRowCount(7);
        if (pergunta != null) {
            texto.setText(pergunta.getTexto());
            opcoes.setText(String.join("\n", pergunta.getOpcoes().stream().map(OpcaoResposta::getTexto).toList()));
        }
        tipo.setOnAction(evento -> atualizarOpcoesHabilitadas());
        atualizarOpcoesHabilitadas();
    }

    private void atualizarOpcoesHabilitadas() {
        opcoes.setDisable(!tipo.getValue().possuiOpcoes());
    }

    void abrir() {
        Dialogos.formulario(janela, pergunta == null ? "Adicionar pergunta" : "Editar pergunta",
                conteudo(campo("Texto", texto), campo("Tipo", tipo), obrigatoria,
                        campo("Opções de resposta (uma por linha)", opcoes),
                        secundario("Usar candidatos associados", this::usarCandidatosComoOpcoes)),
                this::salvar);
    }

    private void usarCandidatosComoOpcoes() {
        opcoes.setText(String.join("\n", pesquisa.getCandidatos().stream().map(Candidato::getNome).toList()));
    }

    private void salvar() {
        String id = pergunta == null ? UUID.randomUUID().toString() : pergunta.getId();
        TipoPergunta tipoEscolhido = Leitura.obrigatorio(tipo, "o tipo");
        Pergunta nova = new Pergunta(id, texto.getText(), tipoEscolhido, obrigatoria.isSelected());
        if (tipoEscolhido.possuiOpcoes()) {
            for (String linha : opcoes.getText().split("\\R")) {
                if (!linha.isBlank()) {
                    nova.adicionarOpcao(new OpcaoResposta(linha));
                }
            }
            Validacao.exigir(!nova.getOpcoes().isEmpty(), "Informe pelo menos uma opção de resposta.");
        }
        servico.salvarPergunta(pesquisa.getCodigo(), nova);
    }
}
