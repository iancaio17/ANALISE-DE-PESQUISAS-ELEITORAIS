package dominio.analise;

import java.util.Arrays;

/**
 * Regra de arredondamento do sistema: MÉTODO DO MAIOR RESTO, com 1 casa decimal.
 * Garante que a soma dos percentuais apresentados seja exatamente 100,0 (quando há respostas válidas).
 */
public final class CalculadoraPercentual {

    private static final int ESCALA = 10; // 1 casa decimal

    private CalculadoraPercentual() { }

    /**
     * @param quantidades quantidade absoluta de cada item
     * @param total       soma das quantidades (base de cálculo = respostas válidas)
     */
    public static double[] calcular(int[] quantidades, int total) {
        int n = quantidades.length;
        double[] resultado = new double[n];
        if (total <= 0 || n == 0) return resultado;

        long unidades = 100L * ESCALA;
        long[] base = new long[n];
        long[] resto = new long[n];
        long soma = 0;
        for (int i = 0; i < n; i++) {
            long numerador = (long) quantidades[i] * unidades;
            base[i] = numerador / total;
            resto[i] = numerador % total;
            soma += base[i];
        }

        Integer[] ordem = new Integer[n];
        for (int i = 0; i < n; i++) ordem[i] = i;
        Arrays.sort(ordem, (a, b) -> {
            int c = Long.compare(resto[b], resto[a]);
            return c != 0 ? c : Integer.compare(a, b);
        });

        long faltam = unidades - soma;
        for (int k = 0; k < faltam && k < n; k++) base[ordem[k]]++;

        for (int i = 0; i < n; i++) resultado[i] = base[i] / (double) ESCALA;
        return resultado;
    }
}
