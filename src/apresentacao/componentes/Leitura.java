package apresentacao.componentes;

import infraestrutura.excecoes.RegraNegocioException;
import java.util.Locale;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;

/** Leitura e validação dos valores digitados ou selecionados nas telas. */
public final class Leitura {

    public static final String TODAS = "Todas";
    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    private Leitura() {
    }

    public static <T> T selecionado(TableView<T> tabela) {
        T valor = tabela.getSelectionModel().getSelectedItem();
        if (valor == null) {
            throw new RegraNegocioException("Selecione uma linha na tabela.");
        }
        return valor;
    }

    public static <T> T obrigatorio(ComboBox<T> combo, String nome) {
        T valor = combo.getValue();
        if (valor == null) {
            throw new RegraNegocioException("Selecione " + nome + ".");
        }
        return valor;
    }

    public static int inteiro(TextInputControl campo, String nome) {
        try {
            int valor = Integer.parseInt(campo.getText().trim());
            if (valor <= 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new RegraNegocioException(nome + " deve ser um número inteiro positivo.");
        }
    }

    /** Lê um limite opcional: campo vazio devolve o valor padrão. */
    public static int limite(TextInputControl campo, int padrao) {
        if (campo.getText().isBlank()) {
            return padrao;
        }
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new RegraNegocioException("Informe um número inteiro no filtro de quantidade.");
        }
    }

    /** Valor de um filtro "Todas": devolve texto vazio quando nada foi escolhido. */
    public static String valorFiltro(ComboBox<String> combo) {
        String valor = combo.getValue();
        return valor == null || valor.equals(TODAS) ? "" : valor;
    }

    public static String percentual(double valor) {
        return String.format(BRASIL, "%.1f", valor);
    }
}
