package dominio.analise;

import java.text.Normalizer;

/**
 * Valores convencionados para respostas que NÃO são atribuídas a candidatos.
 * Regra de negócio: essas respostas são contabilizadas separadamente e ficam fora da base dos percentuais.
 *
 * ATENÇÃO: o módulo de Coleta (Aluno 3) deve registrar exatamente estes textos em Resposta.opcaoEscolhida.
 */
public final class RespostasEspeciais {

    public static final String NAO_SABE = "Não sabe";
    public static final String NAO_RESPONDEU = "Não respondeu";
    public static final String NENHUMA = "Nenhuma das opções";

    private RespostasEspeciais() { }

    public static boolean isNaoSabe(String opcao) { return igual(opcao, NAO_SABE); }
    public static boolean isNaoRespondeu(String opcao) { return igual(opcao, NAO_RESPONDEU); }
    public static boolean isNenhuma(String opcao) { return igual(opcao, NENHUMA); }

    public static boolean isEspecial(String opcao) {
        return isNaoSabe(opcao) || isNaoRespondeu(opcao) || isNenhuma(opcao);
    }

    /** Devolve o texto padronizado se for resposta especial; caso contrário, devolve o próprio texto (sem espaços nas pontas). */
    public static String padronizar(String opcao) {
        if (isNaoSabe(opcao)) return NAO_SABE;
        if (isNaoRespondeu(opcao)) return NAO_RESPONDEU;
        if (isNenhuma(opcao)) return NENHUMA;
        return opcao == null ? null : opcao.trim();
    }

    private static boolean igual(String a, String b) {
        return a != null && normalizar(a).equals(normalizar(b));
    }

    private static String normalizar(String s) {
        String semAcento = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase();
    }
}
