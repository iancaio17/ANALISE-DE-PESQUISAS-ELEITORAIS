package apresentacao.admin;

import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;

import aplicacao.servicos.AdministracaoService;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Parametro;
import dominio.admin.TipoParametro;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/** Janela de cadastro e edição de um parâmetro (o tipo não muda na edição). */
class FormularioParametro {

    private final Window janela;
    private final AdministracaoService servico;
    private final Parametro parametro;
    private final ComboBox<TipoParametro> tipo = combo(TipoParametro.values(), "Selecione");
    private final TextField valor = entrada("Valor da categoria");

    /** @param parametro parâmetro a editar, ou {@code null} para cadastrar um novo */
    FormularioParametro(Window janela, AdministracaoService servico, Parametro parametro) {
        this.janela = janela;
        this.servico = servico;
        this.parametro = parametro;
        if (parametro != null) {
            tipo.setValue(parametro.getTipo());
            tipo.setDisable(true);
            valor.setText(parametro.getValor());
        }
    }

    void abrir() {
        Dialogos.formulario(janela, parametro == null ? "Adicionar parâmetro" : "Editar parâmetro",
                conteudo(campo("Tipo", tipo), campo("Valor", valor)), this::salvar);
    }

    private void salvar() {
        int id = parametro == null ? servico.proximoIdParametro() : parametro.getId();
        servico.salvarParametro(id, Leitura.obrigatorio(tipo, "o tipo"), valor.getText());
    }
}
