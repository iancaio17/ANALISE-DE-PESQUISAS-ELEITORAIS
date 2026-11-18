package integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dominio.candidato.Partido;
import dominio.candidato.SituacaoCandidatura;
import dominio.candidato.SituacaoPartido;
import dominio.pesquisa.Cargo;
import infraestrutura.excecoes.RegraNegocioException;
import org.junit.jupiter.api.Test;

/** Regras de partidos e candidatos. */
class CandidatoTest extends TesteBase {

    @Test
    void partidosTemNumeroESiglaUnicosENaoSaoExcluidosComCandidato() {
        ctx.getCandidatos().salvarPartido(null, "Fictício", "PF", 11, SituacaoPartido.ATIVO);
        Partido partido = ctx.getCandidatos().getBancoPartidos().get(0);
        assertThrows(RegraNegocioException.class,
                () -> ctx.getCandidatos().salvarPartido(null, "Outro", " pf ", 22, SituacaoPartido.ATIVO));
        assertThrows(RegraNegocioException.class,
                () -> ctx.getCandidatos().salvarPartido(null, "Outro", "OUT", 11, SituacaoPartido.ATIVO));

        ctx.getCandidatos().salvarCandidato(1, "Alfa", 11, Cargo.GOVERNADOR, SituacaoCandidatura.CANDIDATO,
                partido.getId());
        assertThrows(RegraNegocioException.class, () -> ctx.getCandidatos().excluirPartido(partido.getId()));

        ctx.getCandidatos().excluirCandidato(1);
        assertEquals(1, ctx.getCandidatos().getBancoPartidos().size());
        ctx.getCandidatos().excluirPartido(partido.getId());
    }

    @Test
    void numeroDeCandidatoEUnicoPorCargo() {
        ctx.getCandidatos().salvarCandidato(1, "Alfa", 11, Cargo.GOVERNADOR, SituacaoCandidatura.VALIDADA, null);
        assertThrows(RegraNegocioException.class, () -> ctx.getCandidatos()
                .salvarCandidato(2, "Beta", 11, Cargo.GOVERNADOR, SituacaoCandidatura.VALIDADA, null));
        ctx.getCandidatos().salvarCandidato(2, "Beta", 11, Cargo.SENADOR, SituacaoCandidatura.VALIDADA, null);
    }

    @Test
    void partidoInativoNaoRecebeCandidaturaAtiva() {
        ctx.getCandidatos().salvarPartido(null, "Fictício", "PF", 11, SituacaoPartido.INATIVO);
        Partido partido = ctx.getCandidatos().getBancoPartidos().get(0);
        assertThrows(RegraNegocioException.class, () -> ctx.getCandidatos().salvarCandidato(1, "Alfa", 11,
                Cargo.GOVERNADOR, SituacaoCandidatura.VALIDADA, partido.getId()));
    }

    @Test
    void indicadoresContamCandidaturasAtivasEPartidos() {
        ctx.getCandidatos().salvarPartido(null, "Fictício", "PF", 11, SituacaoPartido.ATIVO);
        Partido partido = ctx.getCandidatos().getBancoPartidos().get(0);
        ctx.getCandidatos().salvarCandidato(1, "Alfa", 11, Cargo.GOVERNADOR, SituacaoCandidatura.VALIDADA,
                partido.getId());
        ctx.getCandidatos().salvarCandidato(2, "Beta", 12, Cargo.GOVERNADOR, SituacaoCandidatura.INATIVA, null);
        var indicadores = ctx.getCandidatos().gerarIndicadores();
        assertEquals(2, indicadores.getTotalCandidatos());
        assertEquals(1, indicadores.getCandidaturasAtivas());
        assertEquals(1.0, indicadores.getMediaCandidatosPorPartido());
    }
}
