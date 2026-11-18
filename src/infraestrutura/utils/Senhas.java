package infraestrutura.utils;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Política de senhas e geração de hash.
 * O arquivo de dados nunca guarda a senha em texto: usa-se PBKDF2 com sal individual.
 */
public final class Senhas {

    private static final int TAMANHO_MINIMO = 8;
    private static final int ITERACOES = 120000;
    private static final int BITS_DA_CHAVE = 256;
    private static final int BYTES_DO_SAL = 16;
    private static final String PREFIXO = "pbkdf2";

    private Senhas() {
    }

    /** Valida a política mínima (8 caracteres, uma letra e um número) e devolve o hash. */
    public static String gerar(String senha) {
        Validacao.exigir(respeitaPolitica(senha),
                "A senha precisa de pelo menos 8 caracteres, uma letra e um número.");
        byte[] sal = new byte[BYTES_DO_SAL];
        new SecureRandom().nextBytes(sal);
        return PREFIXO + "$" + ITERACOES + "$" + codificar(sal) + "$" + codificar(derivar(senha, sal, ITERACOES));
    }

    /** Confere uma senha digitada com o hash guardado. */
    public static boolean confere(String senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }
        try {
            String[] partes = hash.split("\\$");
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(senha, sal, Integer.parseInt(partes[1]));
            return MessageDigest.isEqual(esperado, calculado);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean respeitaPolitica(String senha) {
        return senha != null
                && senha.length() >= TAMANHO_MINIMO
                && senha.chars().anyMatch(Character::isLetter)
                && senha.chars().anyMatch(Character::isDigit);
    }

    private static String codificar(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] derivar(String senha, byte[] sal, int iteracoes) {
        PBEKeySpec especificacao = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, BITS_DA_CHAVE);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 indisponível neste ambiente.", e);
        } finally {
            especificacao.clearPassword();
        }
    }
}
