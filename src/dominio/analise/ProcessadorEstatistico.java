package dominio.analise;

import dominio.coleta.Entrevista;
import dominio.coleta.Resposta;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Motor de cálculo. Para cada pergunta gera a distribuição geral e as distribuições por critério.
 * Se "paralelo" for true, as contagens usam streams paralelos (requisito opcional de concorrência).
 *
 * Decisões de regra de negócio:
 *  - Só entrevistas CONCLUÍDAS entram no cálculo.
 *  - Entrevista sem resposta para a pergunta é contada como "Não respondeu".
 *  - Percentuais têm como base apenas as respostas VÁLIDAS (exclui não sabe / não respondeu / nenhuma).
 */
public class ProcessadorEstatistico {

    private final boolean paralelo;

    public ProcessadorEstatistico(boolean paralelo) {
        this.paralelo = paralelo;
    }

    public Resultado processarPergunta(String codigoPesquisa, Pergunta pergunta,
                                       List<Entrevista> entrevistas, List<CriterioAgrupamento> criterios) {
        if (pergunta == null) throw new IllegalArgumentException("Pergunta inválida.");

        List<Entrevista> concluidas = abrir(entrevistas).filter(Entrevista::isConcluida).collect(Collectors.toList());

        Distribuicao geral = montar("Geral", "Todos", pergunta, concluidas);

        List<Distribuicao> porGrupo = new ArrayList<>();
        for (CriterioAgrupamento criterio : criterios) {
            Map<String, List<Entrevista>> grupos = abrir(concluidas)
                    .collect(Collectors.groupingBy(criterio::grupoDe, TreeMap::new, Collectors.toList()));
            for (Map.Entry<String, List<Entrevista>> g : grupos.entrySet()) {
                porGrupo.add(montar(criterio.getNome(), g.getKey(), pergunta, g.getValue()));
            }
        }
        return new Resultado(codigoPesquisa, pergunta.getTexto(), geral, porGrupo);
    }

    private Distribuicao montar(String criterio, String grupo, Pergunta pergunta, List<Entrevista> lista) {
        Map<String, Long> contagem = abrir(lista)
                .map(e -> rotuloCanonico(respostaDe(e, pergunta), pergunta))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Ordem dos rótulos: primeiro as opções declaradas na pergunta, depois eventuais extras (ordem alfabética).
        List<String> rotulos = new ArrayList<>();
        for (OpcaoResposta o : pergunta.getOpcoes()) {
            String t = o.getTextoOpcao().trim();
            if (!RespostasEspeciais.isEspecial(t) && !rotulos.contains(t)) rotulos.add(t);
        }
        List<String> extras = contagem.keySet().stream()
                .filter(k -> !RespostasEspeciais.isEspecial(k) && !rotulos.contains(k))
                .sorted()
                .collect(Collectors.toList());
        rotulos.addAll(extras);

        int[] qtds = new int[rotulos.size()];
        int validas = 0;
        for (int i = 0; i < qtds.length; i++) {
            qtds[i] = contagem.getOrDefault(rotulos.get(i), 0L).intValue();
            validas += qtds[i];
        }
        double[] pct = CalculadoraPercentual.calcular(qtds, validas);

        List<ItemResultado> itens = new ArrayList<>();
        for (int i = 0; i < qtds.length; i++) {
            itens.add(new ItemResultado(rotulos.get(i), qtds[i], pct[i]));
        }

        return new Distribuicao(criterio, grupo, itens, validas,
                contagem.getOrDefault(RespostasEspeciais.NAO_SABE, 0L).intValue(),
                contagem.getOrDefault(RespostasEspeciais.NAO_RESPONDEU, 0L).intValue(),
                contagem.getOrDefault(RespostasEspeciais.NENHUMA, 0L).intValue());
    }

    /** Opção escolhida pelo participante para a pergunta, ou null se a entrevista não tem resposta a ela. */
    private String respostaDe(Entrevista entrevista, Pergunta pergunta) {
        for (Resposta r : entrevista.getRespostas()) {
            Pergunta p = r.getPergunta();
            if (p == pergunta || (p.getTexto() != null && p.getTexto().equals(pergunta.getTexto()))) {
                return r.getOpcaoEscolhida();
            }
        }
        return null;
    }

    private String rotuloCanonico(String opcao, Pergunta pergunta) {
        if (opcao == null) return RespostasEspeciais.NAO_RESPONDEU;
        if (RespostasEspeciais.isEspecial(opcao)) return RespostasEspeciais.padronizar(opcao);
        for (OpcaoResposta o : pergunta.getOpcoes()) {
            if (o.getTextoOpcao().trim().equalsIgnoreCase(opcao.trim())) return o.getTextoOpcao().trim();
        }
        return opcao.trim();
    }

    private <T> Stream<T> abrir(List<T> lista) {
        Stream<T> s = lista.stream();
        return paralelo ? s.parallel() : s;
    }
}
