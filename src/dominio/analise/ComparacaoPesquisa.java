package dominio.analise;

import infraestrutura.excecoes.ComparacaoIncompativelException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Compara duas pesquisas já processadas.
 * Critério de compatibilidade do sistema: pesquisas diferentes, com mesmo cargo, mesmo tipo,
 * mesmas regiões e mesmas perguntas (texto, tipo e opções de resposta).
 */
public class ComparacaoPesquisa {

    /** Linha da comparação: uma opção de uma pergunta, nas duas pesquisas. */
    public static class LinhaComparacao {

        private final String pergunta;
        private final String rotulo;
        private final int quantidadeA;
        private final double percentualA;
        private final int quantidadeB;
        private final double percentualB;

        LinhaComparacao(String pergunta, String rotulo, ItemResultado itemA, ItemResultado itemB) {
            this.pergunta = pergunta;
            this.rotulo = rotulo;
            this.quantidadeA = itemA.getQuantidadeAbsoluta();
            this.percentualA = itemA.getPercentual();
            this.quantidadeB = itemB.getQuantidadeAbsoluta();
            this.percentualB = itemB.getPercentual();
        }

        public String getPergunta() {
            return pergunta;
        }

        public String getRotulo() {
            return rotulo;
        }

        public int getQuantidadeA() {
            return quantidadeA;
        }

        public double getPercentualA() {
            return percentualA;
        }

        public int getQuantidadeB() {
            return quantidadeB;
        }

        public double getPercentualB() {
            return percentualB;
        }

        /** Diferença em pontos percentuais (B − A), com uma casa decimal. */
        public double getDiferenca() {
            return Math.round((percentualB - percentualA) * 10.0) / 10.0;
        }
    }

    private final Analise analiseA;
    private final Analise analiseB;
    private final List<LinhaComparacao> linhas;

    public ComparacaoPesquisa(Analise a, Analise b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("As duas análises são obrigatórias.");
        }
        validarCompatibilidade(a, b);
        this.analiseA = a;
        this.analiseB = b;
        this.linhas = montarLinhas(a, b);
    }

    public Analise getAnaliseA() {
        return analiseA;
    }

    public Analise getAnaliseB() {
        return analiseB;
    }

    public List<LinhaComparacao> getLinhas() {
        return Collections.unmodifiableList(linhas);
    }

    private static void validarCompatibilidade(Analise a, Analise b) {
        if (a.getCodigoPesquisa().equalsIgnoreCase(b.getCodigoPesquisa())) {
            throw new ComparacaoIncompativelException("não é possível comparar uma pesquisa com ela mesma.");
        }
        if (a.getCargo() != b.getCargo()) {
            throw new ComparacaoIncompativelException(
                    "os cargos são diferentes ('" + a.getCargo() + "' e '" + b.getCargo() + "').");
        }
        if (a.getTipo() != b.getTipo()) {
            throw new ComparacaoIncompativelException(
                    "os tipos de pesquisa são diferentes ('" + a.getTipo() + "' e '" + b.getTipo() + "').");
        }
        if (!a.getRegioes().equals(b.getRegioes())) {
            throw new ComparacaoIncompativelException("selecione pesquisas com as mesmas regiões.");
        }
        if (a.getResultados().size() != b.getResultados().size()) {
            throw new ComparacaoIncompativelException("os questionários têm quantidades diferentes de perguntas.");
        }
    }

    private static List<LinhaComparacao> montarLinhas(Analise a, Analise b) {
        List<LinhaComparacao> saida = new ArrayList<>();
        for (Resultado resultadoA : a.getResultados()) {
            Resultado resultadoB = b.buscarResultado(resultadoA.getTextoPergunta())
                    .orElseThrow(() -> new ComparacaoIncompativelException(
                            "os questionários devem possuir as mesmas perguntas."));
            if (resultadoA.getTipoPergunta() != resultadoB.getTipoPergunta()) {
                throw new ComparacaoIncompativelException("o tipo de uma pergunta é diferente.");
            }
            Map<String, ItemResultado> itensA = indexar(resultadoA.getGeral());
            Map<String, ItemResultado> itensB = indexar(resultadoB.getGeral());
            if (!itensA.keySet().equals(itensB.keySet())) {
                throw new ComparacaoIncompativelException("as opções de resposta devem ser iguais.");
            }
            for (String rotulo : new LinkedHashSet<>(itensA.keySet())) {
                saida.add(new LinhaComparacao(resultadoA.getTextoPergunta(), rotulo,
                        itensA.get(rotulo), itensB.get(rotulo)));
            }
        }
        return saida;
    }

    private static Map<String, ItemResultado> indexar(Distribuicao distribuicao) {
        Map<String, ItemResultado> mapa = new LinkedHashMap<>();
        for (ItemResultado item : distribuicao.getItens()) {
            mapa.put(item.getRotulo(), item);
        }
        return mapa;
    }
}
