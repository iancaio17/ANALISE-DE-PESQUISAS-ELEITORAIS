package apresentacao.candidatos;

import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.grade;

import aplicacao.servicos.CandidatoService;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.candidato.Partido;
import dominio.candidato.SituacaoPartido;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Janela de cadastro e edição de partido. */
class FormularioPartido {

    private final Window janela;
    private final CandidatoService servico;
    private final Partido partido;

    private final TextField nome = entrada("Nome fictício");
    private final TextField sigla = entrada("Sigla");
    private final TextField numero = entrada("Número");
    private final ComboBox<SituacaoPartido> situacao = combo(SituacaoPartido.values(), "Situação");

    /** @param partido partido a editar, ou {@code null} para cadastrar um novo */
    FormularioPartido(Window janela, CandidatoService servico, Partido partido) {
        this.janela = janela;
        this.servico = servico;
        this.partido = partido;
        situacao.setValue(SituacaoPartido.ATIVO);
        if (partido != null) {
            nome.setText(partido.getNome());
            sigla.setText(partido.getSigla());
            numero.setText(String.valueOf(partido.getNumero()));
            situacao.setValue(partido.getSituacao());
        }
    }

    void abrir() {
        GridPane grade = grade();
        campo(grade, 0, 0, "Nome", nome);
        campo(grade, 0, 1, "Sigla", sigla);
        campo(grade, 1, 0, "Número", numero);
        campo(grade, 1, 1, "Situação", situacao);
        Dialogos.formulario(janela, partido == null ? "Cadastro de partido" : "Editar partido", conteudo(grade),
                this::salvar);
    }

    private void salvar() {
        servico.salvarPartido(partido == null ? null : partido.getId(), nome.getText(), sigla.getText(),
                Leitura.inteiro(numero, "Número"), Leitura.obrigatorio(situacao, "a situação"));
    }
}
