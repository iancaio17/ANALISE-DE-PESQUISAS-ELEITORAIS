package dominio.analise;

import dominio.coleta.Entrevista;
import dominio.coleta.Resposta;
import dominio.coleta.RespostaEspecial;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Motor de cálculo. Para cada pergunta gera a distribuição geral e as distribuições por critério.
 * Se "paralelo" for verdadeiro, as contagens usam streams paralelos (requisito opcional de concorrência).
 *
 * <p>Regras de negócio aplicadas:
 * <ul>
 *   <li>só entrevistas concluídas entram no cálculo;</li>
 *   <li>entrevista sem resposta para a pergunta conta como "Não respondeu";</li>
 *   <li>os percentuais usam só respostas válidas (sem "não sabe", "não respondeu" e "nenhuma").</li>
 * </ul>
 */
public class ProcessadorEstatistico {

    private final boolean paralelo;

    public ProcessadorEstatistico(boolean paralelo) {
        this.paralelo = paralelo;
    }

    public Resultado processarPergunta(String codigoPesquisa, Pergunta pergunta, List<Entrevista> entrevistas,
            List<CriterioAgrupamento> criterios) {
        if (pergunta == null) {
            throw new IllegalArgumentException("Pergunta inválida.");
        }
        List<Entrevista> concluidas = abrir(entrevistas).filter(Entrevista::isConcluida).collect(Collectors.toList());

        Distribuicao geral = montarDistribuicao("Geral", "Todos", pergunta, concluidas);
        List<Distribuicao> porGrupo = new ArrayList<>();
        for (CriterioAgrupamento criterio : criterios) {
            Map<String, List<Entrevista>> grupos = abrir(concluidas)
                    .collect(Collectors.groupingBy(criterio::grupoDe, TreeMap::new, Collectors.toList()));
            grupos.forEach((nomeDoGrupo, doGrupo) ->
                    porGrupo.add(montarDistribuicao(criterio.getNome(), nomeDoGrupo, pergunta, doGrupo)));
        }
        return new Resultado(codigoPesquisa, pergunta.getTexto(), pergunta.getTipo(), geral, porGrupo);
    }

    private Distribuicao montarDistribuicao(String criterio, String grupo, Pergunta pergunta,
            List<Entrevista> entrevistas) {
        Map<String, Long> contagem = abrir(entrevistas)
                .flatMap(e -> respostasDe(e, pergunta).stream())
                .map(opcao -> rotuloCanonico(opcao, pergunta))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<String> rotulos = rotulosDoResultado(pergunta, contagem);
        int[] quantidades = new int[rotulos.size()];
        int totalValidas = 0;
        for (int i = 0; i < quantidades.length; i++) {
            quantidades[i] = contagem.getOrDefault(rotulos.get(i), 0L).intValue();
            totalValidas += quantidades[i];
        }
        double[] percentuais = CalculadoraPercentual.calcular(quantidades, totalValidas);

        List<ItemResultado> itens = new ArrayList<>();
        for (int i = 0; i < quantidades.length; i++) {
            itens.add(new ItemResultado(rotulos.get(i), quantidades[i], percentuais[i]));
        }
        return new Distribuicao(criterio, grupo, itens, totalValidas, contarEspeciais(contagem), entrevistas.size());
    }

    /** Primeiro as opções declaradas na pergunta; depois eventuais extras, em ordem alfabética. */
    private List<String> rotulosDoResultado(Pergunta pergunta, Map<String, Long> contagem) {
        List<String> rotulos = new ArrayList<>();
        for (OpcaoResposta opcao : pergunta.getOpcoes()) {
            String texto = opcao.getTexto().trim();
            if (!RespostaEspecial.ehEspecial(texto) && !rotulos.contains(texto)) {
                rotulos.add(texto);
            }
        }
        List<String> extras = contagem.keySet().stream()
                .filter(texto -> !RespostaEspecial.ehEspecial(texto) && !rotulos.contains(texto))
                .sorted()
                .collect(Collectors.toList());
        rotulos.addAll(extras);
        return rotulos;
    }

    private Map<RespostaEspecial, Integer> contarEspeciais(Map<String, Long> contagem) {
        Map<RespostaEspecial, Integer> especiais = new EnumMap<>(RespostaEspecial.class);
        for (RespostaEspecial tipo : RespostaEspecial.values()) {
            especiais.put(tipo, contagem.getOrDefault(tipo.getTexto(), 0L).intValue());
        }
        return especiais;
    }

    /** Opções escolhidas na pergunta; se a entrevista não a respondeu, vale "Não respondeu". */
    private List<String> respostasDe(Entrevista entrevista, Pergunta pergunta) {
        return entrevista.buscarResposta(pergunta)
                .map(Resposta::getOpcoesEscolhidas)
                .orElse(List.of(RespostaEspecial.NAO_RESPONDEU.getTexto()));
    }

    private String rotuloCanonico(String opcao, Pergunta pergunta) {
        if (RespostaEspecial.ehEspecial(opcao)) {
            return RespostaEspecial.padronizar(opcao);
        }
        for (OpcaoResposta declarada : pergunta.getOpcoes()) {
            if (declarada.getTexto().trim().equalsIgnoreCase(opcao.trim())) {
                return declarada.getTexto().trim();
            }
        }
        return opcao.trim();
    }

    private <T> Stream<T> abrir(List<T> lista) {
        Stream<T> fluxo = lista.stream();
        return paralelo ? fluxo.parallel() : fluxo;
    }
}
