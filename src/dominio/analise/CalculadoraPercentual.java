package dominio.analise;

import java.util.Arrays;

/**
 * Regra de arredondamento do sistema: método do maior resto, com uma casa decimal.
 * Garante que a soma dos percentuais apresentados seja exatamente 100,0 quando há respostas válidas.
 */
public final class CalculadoraPercentual {

    /** Décimos de ponto percentual: 100% = 1000 unidades. */
    private static final long UNIDADES_TOTAIS = 1000;
    private static final double UNIDADES_POR_PONTO = 10.0;

    private CalculadoraPercentual() {
    }

    /**
     * @param quantidades quantidade absoluta de cada item
     * @param total soma das quantidades (base de cálculo = respostas válidas)
     * @return percentual de cada item, na mesma ordem das quantidades
     */
    public static double[] calcular(int[] quantidades, int total) {
        validar(quantidades, total);
        int n = quantidades.length;
        double[] percentuais = new double[n];
        if (total == 0 || n == 0) {
            return percentuais;
        }

        long[] unidades = new long[n];
        long[] restos = new long[n];
        long somaDasUnidades = 0;
        for (int i = 0; i < n; i++) {
            long numerador = quantidades[i] * UNIDADES_TOTAIS;
            unidades[i] = numerador / total;
            restos[i] = numerador % total;
            somaDasUnidades += unidades[i];
        }

        distribuirSobras(unidades, restos, UNIDADES_TOTAIS - somaDasUnidades);

        for (int i = 0; i < n; i++) {
            percentuais[i] = unidades[i] / UNIDADES_POR_PONTO;
        }
        return percentuais;
    }

    /** Entrega uma unidade extra a cada item, começando pelos maiores restos. */
    private static void distribuirSobras(long[] unidades, long[] restos, long sobras) {
        Integer[] ordem = new Integer[unidades.length];
        for (int i = 0; i < ordem.length; i++) {
            ordem[i] = i;
        }
        Arrays.sort(ordem, (a, b) -> {
            int porResto = Long.compare(restos[b], restos[a]);
            return porResto != 0 ? porResto : Integer.compare(a, b);
        });
        for (int k = 0; k < sobras && k < ordem.length; k++) {
            unidades[ordem[k]]++;
        }
    }

    private static void validar(int[] quantidades, int total) {
        boolean valido = quantidades != null
                && total >= 0
                && Arrays.stream(quantidades).allMatch(q -> q >= 0)
                && Arrays.stream(quantidades).asLongStream().sum() == total;
        if (!valido) {
            throw new IllegalArgumentException("A base dos percentuais deve ser a soma das quantidades válidas.");
        }
    }
}
