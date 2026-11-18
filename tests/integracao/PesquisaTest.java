package integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dominio.pesquisa.Cargo;
import dominio.pesquisa.SituacaoPesquisa;
import dominio.pesquisa.TipoPesquisa;
import dominio.base.PerfilDemografico;
import dominio.candidato.SituacaoCandidatura;
import infraestrutura.excecoes.RegraNegocioException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Regras do cadastro e do ciclo de vida das pesquisas. */
class PesquisaTest extends TesteBase {

    @Test
    void exigeDatasRegioesECodigoUnico() {
        assertThrows(RegraNegocioException.class, () -> ctx.getPesquisas().salvarPesquisa("P", true, "Fictícia",
                Cargo.GOVERNADOR, LocalDate.now(), LocalDate.now().minusDays(1), "Local", 10, TipoPesquisa.OUTRA,
                "Cotas", List.of("Norte")));
        assertThrows(RegraNegocioException.class, () -> ctx.getPesquisas().salvarPesquisa("P", true, "Fictícia",
                Cargo.GOVERNADOR, LocalDate.now(), LocalDate.now(), "Local", 10, TipoPesquisa.OUTRA, "Cotas",
                List.of("Inexistente")));
        criarPesquisa("P", 1);
        assertThrows(RegraNegocioException.class, () -> criarPesquisa("p", 2));
    }

    @Test
    void estruturaNaoMudaAposInicioDaColeta() {
        prepararColeta("P", 2);
        assertThrows(RegraNegocioException.class, () -> criarPergunta("P"));
        assertThrows(RegraNegocioException.class, () -> ctx.getPesquisas().excluirPesquisa("P"));
        ctx.getPesquisas().alterarMeta("P", 3);
        assertEquals(3, ctx.getPesquisas().buscarPesquisaPorCodigo("P").getQuantidadePrevista());
    }

    @Test
    void intencaoDeVotoExigeCandidatos() {
        criarPesquisa("P", 2, TipoPesquisa.INTENCAO_VOTO);
        criarPergunta("P");
        assertThrows(RegraNegocioException.class, () -> ctx.getPesquisas().iniciarColeta("P"));
        ctx.getCandidatos().salvarCandidato(1, "Alfa", 11, Cargo.GOVERNADOR, SituacaoCandidatura.VALIDADA, null);
        ctx.getPesquisas().associarCandidato("P", 1);
        ctx.getPesquisas().iniciarColeta("P");
        assertEquals(SituacaoPesquisa.EM_COLETA, ctx.getPesquisas().buscarPesquisaPorCodigo("P").getSituacao());
    }

    @Test
    void candidatoExigeMesmoCargoSituacaoPermitidaESemDuplicidade() {
        criarPesquisa("P", 2);
        ctx.getCandidatos().salvarCandidato(1, "Alfa", 11, Cargo.PRESIDENTE, SituacaoCandidatura.VALIDADA, null);
        assertThrows(RegraNegocioException.class, () -> ctx.getPesquisas().associarCandidato("P", 1));

        ctx.getCandidatos().salvarCandidato(2, "Beta", 11, Cargo.GOVERNADOR, SituacaoCandidatura.INATIVA, null);
        assertThrows(RegraNegocioException.class, () -> ctx.getPesquisas().associarCandidato("P", 2));

        ctx.getCandidatos().salvarCandidato(2, "Beta", 11, Cargo.GOVERNADOR, SituacaoCandidatura.VALIDADA, null);
        ctx.getPesquisas().associarCandidato("P", 2);
        assertThrows(RegraNegocioException.class, () -> ctx.getPesquisas().associarCandidato("P", 2));
        assertThrows(RegraNegocioException.class, () -> ctx.getCandidatos().excluirCandidato(2));
    }

    @Test
    void metasPorGrupoConsideramSomenteEntrevistasConcluidas() {
        criarPesquisa("P", 2);
        criarPergunta("P");
        PerfilDemografico grupo = new PerfilDemografico("", "Norte", "", "", "");
        ctx.getPesquisas().salvarMeta("P", grupo, 2);
        ctx.getPesquisas().iniciarColeta("P");
        ctx.getColeta().salvarEntrevistador(1, "Fictício");
        entrevistar("P", 1, "Alfa");
        assertEquals(1L, ctx.getPesquisas().realizadasPorMeta("P").get(grupo));
    }
}
