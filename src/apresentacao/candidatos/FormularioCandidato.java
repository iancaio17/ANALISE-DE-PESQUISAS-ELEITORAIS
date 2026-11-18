package apresentacao.candidatos;

import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.codigo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.grade;
import static apresentacao.componentes.Componentes.secundario;

import aplicacao.servicos.CandidatoService;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.candidato.Candidato;
import dominio.candidato.Partido;
import dominio.candidato.SituacaoCandidatura;
import dominio.pesquisa.Cargo;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Janela de cadastro e edição de candidato. O partido é opcional. */
class FormularioCandidato {

    private final Window janela;
    private final CandidatoService servico;
    private final Candidato candidato;

    private final TextField id;
    private final TextField nome = entrada("Nome fictício");
    private final TextField numero = entrada("Número fictício");
    private final ComboBox<Cargo> cargo = combo(Cargo.values(), "Selecione");
    private final ComboBox<SituacaoCandidatura> situacao = combo(SituacaoCandidatura.values(), "Selecione");
    private final ComboBox<Partido> partido;

    /** @param candidato candidato a editar, ou {@code null} para cadastrar um novo */
    FormularioCandidato(Window janela, CandidatoService servico, Candidato candidato) {
        this.janela = janela;
        this.servico = servico;
        this.candidato = candidato;
        this.id = codigo(candidato == null ? servico.proximoId() : candidato.getId());
        this.partido = combo(servico.getBancoPartidos(), "Sem partido");
        if (candidato != null) {
            preencher(candidato);
        }
    }

    private void preencher(Candidato c) {
        nome.setText(c.getNome());
        numero.setText(String.valueOf(c.getNumeroIdentificador()));
        cargo.setValue(c.getCargo());
        situacao.setValue(c.getSituacao());
        if (c.getPartido() != null) {
            partido.getItems().stream()
                    .filter(p -> p.getId().equals(c.getPartido().getId()))
                    .findFirst()
                    .ifPresent(partido::setValue);
        }
    }

    void abrir() {
        GridPane grade = grade();
        campo(grade, 0, 0, "ID", id);
        campo(grade, 0, 1, "Nome", nome);
        campo(grade, 1, 0, "Número", numero);
        campo(grade, 1, 1, "Cargo", cargo);
        campo(grade, 2, 0, "Situação", situacao);
        campo(grade, 2, 1, "Partido (opcional)", partido);
        Dialogos.formulario(janela, candidato == null ? "Cadastro de candidato" : "Editar candidato",
                conteudo(grade, secundario("Desvincular partido", () -> partido.setValue(null))), this::salvar);
    }

    private void salvar() {
        Partido escolhido = partido.getValue();
        servico.salvarCandidato(Leitura.inteiro(id, "ID"), nome.getText(), Leitura.inteiro(numero, "Número"),
                Leitura.obrigatorio(cargo, "o cargo"), Leitura.obrigatorio(situacao, "a situação"),
                escolhido == null ? null : escolhido.getId());
    }
}
