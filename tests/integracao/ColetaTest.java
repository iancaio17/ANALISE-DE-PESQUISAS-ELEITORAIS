package integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dominio.base.PerfilDemografico;
import dominio.coleta.Entrevista;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.TipoPergunta;
import dominio.pesquisa.TipoPesquisa;
import dominio.coleta.RespostaEspecial;
import infraestrutura.excecoes.RegraNegocioException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Regras de entrevistas, participantes e entrevistadores. */
class ColetaTest extends TesteBase {

    @Test
    void participanteExigeCategoriasCadastradas() {
        assertThrows(RegraNegocioException.class, () -> ctx.getColeta().salvarParticipante(1, "Fictício",
                new PerfilDemografico("99", "Norte", "Médio", "Até 2 SM", "Feminino")));
        ctx.getColeta().salvarParticipante(1, "Fictício", perfil());
        int idParametro = ctx.getAdministracao().listarParametros().stream()
                .filter(p -> p.getValor().equals("Norte")).findFirst().orElseThrow().getId();
        assertThrows(RegraNegocioException.class, () -> ctx.getAdministracao().excluirParametro(idParametro));
    }

    @Test
    void entrevistaExigePerguntasObrigatoriasENaoGravaFalhas() {
        prepararColeta("P", 2);
        ctx.getColeta().salvarParticipante(1, "Fictício", perfil());
        assertThrows(RegraNegocioException.class, () -> ctx.getColeta().registrarEntrevista("P", 1, 1, 1, Map.of()));
        assertEquals(0, ctx.getColeta().getBancoEntrevistas().size());
        assertEquals(0, ctx.getPesquisas().buscarPesquisaPorCodigo("P").getQuantidadeRealizada());
    }

    @Test
    void entrevistaRejeitaOpcaoEPerguntaAlheias() {
        prepararColeta("P", 2);
        ctx.getColeta().salvarParticipante(1, "Fictício", perfil());
        Pergunta pergunta = ctx.getPesquisas().buscarPesquisaPorCodigo("P").getPerguntas().get(0);
        assertThrows(RegraNegocioException.class, () -> ctx.getColeta()
                .registrarEntrevista("P", 1, 1, 1, Map.of(pergunta.getId(), List.of("Opção alheia"))));
        assertThrows(RegraNegocioException.class, () -> ctx.getColeta()
                .registrarEntrevista("P", 1, 1, 1, Map.of("outra", List.of("Alfa"))));
    }

    @Test
    void entrevistaExigeParticipanteDeUmaRegiaoDaPesquisa() {
        ctx.getPesquisas().salvarPesquisa("P", true, "Pesquisa", Cargo.GOVERNADOR, LocalDate.now(), LocalDate.now(),
                "Fictícia", 2, TipoPesquisa.OUTRA, "Cotas", List.of("Sul"));
        criarPergunta("P");
        ctx.getPesquisas().iniciarColeta("P");
        ctx.getColeta().salvarEntrevistador(1, "Fictício");
        ctx.getColeta().salvarParticipante(1, "Fictício", perfil());
        Pergunta pergunta = ctx.getPesquisas().buscarPesquisaPorCodigo("P").getPerguntas().get(0);
        assertThrows(RegraNegocioException.class, () -> ctx.getColeta()
                .registrarEntrevista("P", 1, 1, 1, Map.of(pergunta.getId(), List.of("Alfa"))));
    }

    @Test
    void pesquisaForaDoPeriodoNaoRecebeEntrevista() {
        ctx.getPesquisas().salvarPesquisa("P", true, "Pesquisa", Cargo.GOVERNADOR, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2), "Fictícia", 2, TipoPesquisa.OUTRA, "Cotas", List.of("Norte"));
        criarPergunta("P");
        ctx.getPesquisas().iniciarColeta("P");
        ctx.getColeta().salvarEntrevistador(1, "Fictício");
        assertThrows(RegraNegocioException.class, () -> entrevistar("P", 1, "Alfa"));
    }

    @Test
    void numeroDeEntrevistaUnicoELimiteDeEntrevistasRespeitado() {
        prepararColeta("P", 1);
        entrevistar("P", 1, "Alfa");
        Pergunta pergunta = ctx.getPesquisas().buscarPesquisaPorCodigo("P").getPerguntas().get(0);
        assertThrows(RegraNegocioException.class, () -> ctx.getColeta()
                .registrarEntrevista("P", 1, 1, 1, Map.of(pergunta.getId(), List.of("Beta"))));
        assertThrows(RegraNegocioException.class, () -> entrevistar("P", 2, "Beta"));

        ctx.getPesquisas().alterarMeta("P", 2);
        entrevistar("P", 2, "Beta");
        assertEquals(100.0, ctx.getPesquisas().buscarPesquisaPorCodigo("P").getPercentualConclusao());
        assertThrows(RegraNegocioException.class, () -> ctx.getPesquisas().alterarMeta("P", 1));
    }

    @Test
    void entrevistaConcluidaEDadosVinculadosSaoPreservados() {
        prepararColeta("P", 2);
        entrevistar("P", 1, "Alfa");
        Entrevista entrevista = ctx.getColeta().getBancoEntrevistas().get(0);
        assertThrows(RegraNegocioException.class,
                () -> entrevista.registrarResposta(entrevista.getRespostas().get(0)));
        assertThrows(RegraNegocioException.class, () -> ctx.getColeta().salvarParticipante(1, "Mudança", perfil()));
        assertThrows(RegraNegocioException.class, () -> ctx.getColeta().excluirEntrevistador(1));
    }

    @Test
    void perguntaDiscursivaEOpcionalSemRespostaViraNaoRespondeu() {
        criarPesquisa("P", 1);
        Pergunta discursiva = new Pergunta("Texto fictício?", TipoPergunta.DISCURSIVA);
        Pergunta opcional = new Pergunta(UUID.randomUUID().toString(), "Opcional?", TipoPergunta.UNICA_ESCOLHA, false);
        opcional.adicionarOpcao(new OpcaoResposta("Sim"));
        ctx.getPesquisas().salvarPergunta("P", discursiva);
        ctx.getPesquisas().salvarPergunta("P", opcional);
        ctx.getPesquisas().iniciarColeta("P");
        ctx.getColeta().salvarEntrevistador(1, "Fictício");
        ctx.getColeta().salvarParticipante(1, "Fictício", perfil());

        ctx.getColeta().registrarEntrevista("P", 1, 1, 1, Map.of(discursiva.getId(), List.of("Comentário fictício")));

        String resposta = ctx.getColeta().getBancoEntrevistas().get(0).getRespostas().get(1).getOpcaoEscolhida();
        assertEquals(RespostaEspecial.NAO_RESPONDEU.getTexto(), resposta);
    }
}
