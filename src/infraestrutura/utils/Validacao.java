package infraestrutura.utils;

import infraestrutura.excecoes.RegraNegocioException;
import java.util.Locale;

/** Validações reutilizadas pelas classes de domínio e pelos serviços. */
public final class Validacao {

    private static final int TAMANHO_MAXIMO_TEXTO = 500;

    private Validacao() {
    }

    /** Exige texto não vazio com até 500 caracteres e devolve o texto sem espaços nas pontas. */
    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(campo + " é obrigatório.");
        }
        String limpo = valor.trim();
        if (limpo.length() > TAMANHO_MAXIMO_TEXTO) {
            throw new RegraNegocioException(campo + " deve ter até " + TAMANHO_MAXIMO_TEXTO + " caracteres.");
        }
        return limpo;
    }

    /** Exige número inteiro maior que zero. */
    public static int positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new RegraNegocioException(campo + " deve ser positivo.");
        }
        return valor;
    }

    /** Lança {@link RegraNegocioException} com a mensagem informada se a condição for falsa. */
    public static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }

    /** Normaliza um texto para comparações sem diferenciar maiúsculas e minúsculas. */
    public static String chave(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }
}
