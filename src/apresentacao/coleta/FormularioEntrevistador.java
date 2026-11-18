package apresentacao.coleta;

import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.codigo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;

import aplicacao.servicos.ColetaService;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.coleta.Entrevistador;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/** Janela de cadastro e edição de entrevistador. */
class FormularioEntrevistador {

    private final Window janela;
    private final ColetaService servico;
    private final TextField id;
    private final TextField nome = entrada("Nome fictício");

    /** @param entrevistador entrevistador a editar, ou {@code null} para cadastrar um novo */
    FormularioEntrevistador(Window janela, ColetaService servico, Entrevistador entrevistador) {
        this.janela = janela;
        this.servico = servico;
        this.id = codigo(entrevistador == null ? servico.proximoIdEntrevistador() : entrevistador.getId());
        if (entrevistador != null) {
            nome.setText(entrevistador.getNome());
        }
    }

    void abrir() {
        Dialogos.formulario(janela, "Entrevistador",
                conteudo(campo("Código numérico", id), campo("Nome fictício", nome)),
                () -> servico.salvarEntrevistador(Leitura.inteiro(id, "Código"), nome.getText()));
    }
}
