package dominio.coleta;

import java.text.Normalizer;

/**
 * Respostas que não são atribuídas a candidatos ou opções.
 * Regra de negócio: são contabilizadas separadamente e ficam fora da base dos percentuais.
 */
public enum RespostaEspecial {
    NAO_SABE("Não sabe"),
    NAO_RESPONDEU("Não respondeu"),
    NENHUMA_DAS_OPCOES("Nenhuma das opções");

    private final String texto;

    RespostaEspecial(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    /** Procura a resposta especial equivalente ao texto (ignora acentos e maiúsculas); null se não houver. */
    public static RespostaEspecial de(String opcao) {
        if (opcao == null) {
            return null;
        }
        for (RespostaEspecial especial : values()) {
            if (normalizar(especial.texto).equals(normalizar(opcao))) {
                return especial;
            }
        }
        return null;
    }

    public static boolean ehEspecial(String opcao) {
        return de(opcao) != null;
    }

    /** Devolve o texto padronizado da resposta especial ou o próprio texto sem espaços nas pontas. */
    public static String padronizar(String opcao) {
        RespostaEspecial especial = de(opcao);
        if (especial != null) {
            return especial.texto;
        }
        return opcao == null ? null : opcao.trim();
    }

    private static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase();
    }

    @Override
    public String toString() {
        return texto;
    }
}
