package dominio.base;

import java.util.Arrays;
import java.util.List;

/** Gêneros disponíveis para a caracterização da amostra. */
public enum Genero {
    FEMININO("Feminino"),
    MASCULINO("Masculino"),
    OUTRO("Outro"),
    NAO_INFORMADO("Não informado");

    private final String texto;

    Genero(String texto) {
        this.texto = texto;
    }

    /** Textos dos gêneros, na ordem de declaração, para preencher listas da interface. */
    public static List<String> textos() {
        return Arrays.stream(values()).map(Genero::toString).toList();
    }

    @Override
    public String toString() {
        return texto;
    }
}
