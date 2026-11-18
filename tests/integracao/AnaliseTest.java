package integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aplicacao.servicos.FiltroEntrevista;
import dominio.analise.Analise;
import dominio.analise.Distribuicao;
import dominio.analise.ItemResultado;
import dominio.coleta.Resposta;
import dominio.coleta.RespostaEspecial;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.SituacaoPesquisa;
import dominio.pesquisa.TipoPergunta;
import infraestrutura.excecoes.ComparacaoIncompativelException;
import infraestrutura.excecoes.PesquisaNaoEncerradaException;
import infraestrutura.excecoes.RegraNegocioException;
import infraestrutura.excecoes.SemRespostasValidasException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Processamento, estatística, filtros e comparação. */
class AnaliseTest extends TesteBase {

    @Test
    void naoProcessaAntesDeEncerrarENemRecebeColetaDepois() {
        prepararColeta("P", 2);
        entrevistar("P", 1, "Alfa");
        assertThrows(PesquisaNaoEncerradaException.class, () -> ctx.getAnalise().processarPesquisa("P"));

        ctx.getPesquisas().encerrarColeta("P");
        assertThrows(RegraNegocioException.class, () -> entrevistar("P", 2, "Beta"));
        ctx.getAnalise().processarPesquisa("P");
        assertEquals(SituacaoPesquisa.PROCESSADA, ctx.getPesquisas().buscarPesquisaPorCodigo("P").getSituacao());
    }

    @Test
    void semEntrevistasNaoMarcaComoProcessada() {
        prepararColeta("P", 2);
        ctx.getPesquisas().encerrarColeta("P");
        assertThrows(SemRespostasValidasException.class, () -> ctx.getAnalise().processarPesquisa("P"));
        assertEquals(SituacaoPesquisa.ENCERRADA, ctx.getPesquisas().buscarPesquisaPorCodigo("P").getSituacao());
    }

    @Test
    void respostasEspeciaisSaoSeparadasEPercentuaisSomam100() {
        prepararColeta("P", 6);
        entrevistar("P", 1, "Alfa");
        entrevistar("P", 2, "Alfa");
        entrevistar("P", 3, "Beta");
        entrevistar("P", 4, RespostaEspecial.NAO_SABE.getTexto());
        entrevistar("P", 5, RespostaEspecial.NAO_RESPONDEU.getTexto());
        entrevistar("P", 6, RespostaEspecial.NENHUMA_DAS_OPCOES.getTexto());
        ctx.getPesquisas().encerrarColeta("P");

        Analise analise = ctx.getAnalise().processarPesquisa("P");
        Distribuicao geral = analise.getResultados().get(0).getGeral();

        assertEquals(6, geral.getTotalEntrevistas());
        assertEquals(3, geral.getTotalValidas());
        assertEquals(1, geral.getEspecial(RespostaEspecial.NAO_SABE));
        assertEquals(1, geral.getEspecial(RespostaEspecial.NAO_RESPONDEU));
        assertEquals(1, geral.getEspecial(RespostaEspecial.NENHUMA_DAS_OPCOES));
        assertEquals(100, geral.getItens().stream().mapToDouble(ItemResultado::getPercentual).sum(), 0.0001);
        assertEquals(66.7, geral.getItens().get(0).getPercentual());
        assertFalse(analise.getResultados().get(0).getPorCriterio("Região × Faixa etária").isEmpty());
    }

    @Test
    void somenteRespostasEspeciaisNaoDividemPorZero() {
        prepararColeta("P", 1);
        entrevistar("P", 1, RespostaEspecial.NAO_SABE.getTexto());
        ctx.getPesquisas().encerrarColeta("P");

        Distribuicao geral = ctx.getAnalise().processarPesquisa("P").getResultados().get(0).getGeral();

        assertEquals(0, geral.getTotalValidas());
        assertTrue(geral.getItens().stream().allMatch(item -> item.getPercentual() == 0));
    }

    @Test
    void multiplaEscolhaContaSelecoesEEntrevistasSeparadamente() {
        criarPesquisa("P", 1);
        Pergunta pergunta = new Pergunta("Selecione opções", TipoPergunta.MULTIPLA_ESCOLHA);
        pergunta.adicionarOpcao(new OpcaoResposta("Alfa"));
        pergunta.adicionarOpcao(new OpcaoResposta("Beta"));
        ctx.getPesquisas().salvarPergunta("P", pergunta);
        ctx.getPesquisas().iniciarColeta("P");
        ctx.getColeta().salvarParticipante(1, "Fictício", perfil());
        ctx.getColeta().salvarEntrevistador(1, "Fictício");

        assertThrows(RegraNegocioException.class,
                () -> new Resposta(pergunta, List.of("Alfa", RespostaEspecial.NAO_SABE.getTexto())));
        ctx.getColeta().registrarEntrevista("P", 1, 1, 1, Map.of(pergunta.getId(), List.of("Alfa", "Beta")));
        ctx.getPesquisas().encerrarColeta("P");

        Distribuicao geral = ctx.getAnalise().processarPesquisa("P").getResultados().get(0).getGeral();
        assertEquals(1, geral.getTotalEntrevistas());
        assertEquals(2, geral.getTotalValidas());
        assertEquals(50, geral.getItens().get(0).getPercentual());
    }

    @Test
    void filtroNaoAlteraAAnaliseSalva() {
        prepararColeta("P", 2);
        entrevistar("P", 1, "Alfa");
        entrevistar("P", 2, "Beta");
        ctx.getPesquisas().encerrarColeta("P");
        ctx.getAnalise().processarPesquisa("P");

        Analise filtrada = ctx.getAnalise().analisarComFiltro("P", entrevista -> entrevista.getIdUnico() == 1);

        assertEquals(1, filtrada.getTotalEntrevistas());
        assertEquals(2, ctx.getAnalise().buscarAnalise("P").getTotalEntrevistas());
        assertThrows(SemRespostasValidasException.class, () -> ctx.getAnalise().analisarComFiltro("P", e -> false));
    }

    @Test
    void filtroPorOpcaoEscolhidaMantemSoQuemEscolheuACandidatura() {
        prepararColeta("P", 2);
        entrevistar("P", 1, "Alfa");
        entrevistar("P", 2, "Beta");
        ctx.getPesquisas().encerrarColeta("P");
        ctx.getAnalise().processarPesquisa("P");
        Pergunta pergunta = ctx.getPesquisas().buscarPesquisaPorCodigo("P").getPerguntas().get(0);

        Analise filtrada = ctx.getAnalise().analisar("P", new FiltroEntrevista().opcaoEscolhida(pergunta, "Beta"));

        assertEquals(1, filtrada.getTotalEntrevistas());
    }

    @Test
    void comparacaoExigePesquisasDiferentesECompativeis() {
        prepararColeta("A", 1);
        entrevistar("A", 1, "Alfa");
        ctx.getPesquisas().encerrarColeta("A");
        ctx.getAnalise().processarPesquisa("A");

        criarPesquisa("B", 1);
        criarPergunta("B");
        ctx.getPesquisas().iniciarColeta("B");
        entrevistar("B", 2, "Beta");
        ctx.getPesquisas().encerrarColeta("B");
        ctx.getAnalise().processarPesquisa("B");

        var comparacao = ctx.getAnalise().compararPesquisas("A", "B");
        assertEquals(-100, comparacao.getLinhas().get(0).getDiferenca());
        assertThrows(ComparacaoIncompativelException.class, () -> ctx.getAnalise().compararPesquisas("A", "A"));
    }
}
