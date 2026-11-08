package dominio.analise;

import infraestrutura.excecoes.ComparacaoIncompativelException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compara duas pesquisas JÁ PROCESSADAS.
 * Critério de compatibilidade definido pelo sistema: mesmo cargo, mesmo tipo, pesquisas diferentes,
 * regiões em comum (quando ambas definem regiões) e ao menos uma pergunta em comum.
 */
public class ComparacaoPesquisa {

    /** Linha da comparação: um rótulo (ex.: candidato) em uma pergunta, nas duas pesquisas. */
    public static class LinhaComparacao {
        private final String pergunta;
        private final String rotulo;
        private final int quantidadeA;
        private final double percentualA;
        private final int quantidadeB;
        private final double percentualB;
        private final double diferenca;

        LinhaComparacao(String pergunta, String rotulo, int qA, double pA, int qB, double pB) {
            this.pergunta = pergunta;
            this.rotulo = rotulo;
            this.quantidadeA = qA;
            this.percentualA = pA;
            this.quantidadeB = qB;
            this.percentualB = pB;
            this.diferenca = Math.round((pB - pA) * 10.0) / 10.0;
        }

        public String getPergunta() { return pergunta; }
        public String getRotulo() { return rotulo; }
        public int getQuantidadeA() { return quantidadeA; }
        public double getPercentualA() { return percentualA; }
        public int getQuantidadeB() { return quantidadeB; }
        public double getPercentualB() { return percentualB; }
        /** Diferença em pontos percentuais (B − A). */
        public double getDiferenca() { return diferenca; }
    }

    private final Analise analiseA;
    private final Analise analiseB;
    private final List<LinhaComparacao> linhas;

    public ComparacaoPesquisa(Analise a, Analise b) {
        if (a == null || b == null) throw new IllegalArgumentException("As duas análises são obrigatórias.");
        validarCompatibilidade(a, b);
        this.analiseA = a;
        this.analiseB = b;
        this.linhas = montarLinhas(a, b);
        if (linhas.isEmpty()) {
            throw new ComparacaoIncompativelException("as pesquisas não possuem nenhuma pergunta em comum.");
        }
    }

    public Analise getAnaliseA() { return analiseA; }
    public Analise getAnaliseB() { return analiseB; }
    public List<LinhaComparacao> getLinhas() { return Collections.unmodifiableList(linhas); }

    private static void validarCompatibilidade(Analise a, Analise b) {
        if (a.getCodigoPesquisa().equalsIgnoreCase(b.getCodigoPesquisa())) {
            throw new ComparacaoIncompativelException("não é possível comparar uma pesquisa com ela mesma.");
        }
        if (!a.getCargo().equalsIgnoreCase(b.getCargo())) {
            throw new ComparacaoIncompativelException("os cargos são diferentes ('" + a.getCargo() + "' e '" + b.getCargo() + "').");
        }
        if (!a.getTipo().equalsIgnoreCase(b.getTipo())) {
            throw new ComparacaoIncompativelException("os tipos de pesquisa são diferentes ('" + a.getTipo() + "' e '" + b.getTipo() + "').");
        }
        if (!a.getRegioes().isEmpty() && !b.getRegioes().isEmpty()) {
            Set<String> comuns = new LinkedHashSet<>(a.getRegioes());
            comuns.retainAll(b.getRegioes());
            if (comuns.isEmpty()) {
                throw new ComparacaoIncompativelException("as pesquisas não possuem regiões em comum.");
            }
        }
    }

    private static List<LinhaComparacao> montarLinhas(Analise a, Analise b) {
        List<LinhaComparacao> saida = new ArrayList<>();
        for (Resultado ra : a.getResultados()) {
            Resultado rb = null;
            for (Resultado candidato : b.getResultados()) {
                if (candidato.getTextoPergunta().trim().equalsIgnoreCase(ra.getTextoPergunta().trim())) {
                    rb = candidato;
                    break;
                }
            }
            if (rb == null) continue;

            Map<String, ItemResultado> ma = indexar(ra.getGeral());
            Map<String, ItemResultado> mb = indexar(rb.getGeral());
            Set<String> rotulos = new LinkedHashSet<>(ma.keySet());
            rotulos.addAll(mb.keySet());

            for (String rotulo : rotulos) {
                ItemResultado ia = ma.get(rotulo);
                ItemResultado ib = mb.get(rotulo);
                saida.add(new LinhaComparacao(ra.getTextoPergunta(), rotulo,
                        ia == null ? 0 : ia.getQuantidadeAbsoluta(), ia == null ? 0.0 : ia.getPercentual(),
                        ib == null ? 0 : ib.getQuantidadeAbsoluta(), ib == null ? 0.0 : ib.getPercentual()));
            }
        }
        return saida;
    }

    private static Map<String, ItemResultado> indexar(Distribuicao d) {
        Map<String, ItemResultado> mapa = new LinkedHashMap<>();
        for (ItemResultado i : d.getItens()) mapa.put(i.getRotulo(), i);
        return mapa;
    }
}
