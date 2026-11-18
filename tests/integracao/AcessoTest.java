package integracao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dominio.admin.Perfil;
import dominio.admin.Usuario;
import dominio.candidato.SituacaoPartido;
import infraestrutura.excecoes.AcessoNegadoException;
import infraestrutura.excecoes.RegraNegocioException;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Login, perfis e regras de usuários. */
class AcessoTest extends TesteBase {

    @Test
    void autenticaNormalizandoEmailEGuardaSenhaComoHash() {
        Usuario usuario = ctx.entrar(" ADMIN@SIMULADO.LOCAL ", "Admin12345");
        assertFalse(usuario.getSenha().contains("Admin12345"));
        assertTrue(usuario.getSenha().startsWith("pbkdf2$"));
    }

    @Test
    void exigeLogin() {
        ctx.sair();
        assertThrows(AcessoNegadoException.class, () -> ctx.getPesquisas().listarTodas());
    }

    @Test
    void senhaInvalidaEUsuarioInativoNaoEntram() {
        assertThrows(AcessoNegadoException.class, () -> ctx.entrar("admin@simulado.local", "Errada123"));
        criarUsuario(2, "inativo@teste.local", Set.of(Perfil.PESQUISADOR));
        ctx.getAdministracao().salvarUsuario(2, "Inativo", "inativo@teste.local", "", Set.of(Perfil.PESQUISADOR), false);
        assertThrows(AcessoNegadoException.class, () -> ctx.entrar("inativo@teste.local", "Senha12345"));
    }

    @Test
    void pesquisadorNaoAdministraNemProcessa() {
        criarUsuario(2, "pesq@teste.local", Set.of(Perfil.PESQUISADOR));
        ctx.entrar("pesq@teste.local", "Senha12345");
        assertThrows(AcessoNegadoException.class, () -> ctx.getAdministracao().listarUsuarios());
        assertThrows(AcessoNegadoException.class,
                () -> ctx.getCandidatos().salvarPartido(null, "Partido fictício", "PF", 11, SituacaoPartido.ATIVO));
        assertThrows(AcessoNegadoException.class, () -> ctx.getAnalise().processarPesquisa("X"));
        criarPesquisa("P", 1);
    }

    @Test
    void analistaConsultaMasNaoCadastraNemColeta() {
        criarUsuario(2, "analise@teste.local", Set.of(Perfil.ANALISTA));
        ctx.entrar("analise@teste.local", "Senha12345");
        assertTrue(ctx.getPesquisas().listarTodas().isEmpty());
        assertThrows(AcessoNegadoException.class, () -> criarPesquisa("P", 1));
        assertThrows(AcessoNegadoException.class, () -> ctx.getColeta().listarParticipantes());
        assertThrows(AcessoNegadoException.class, () -> ctx.getColeta().salvarEntrevistador(1, "Fictício"));
    }

    @Test
    void multiplosPerfisCombinamPermissoes() {
        criarUsuario(2, "multi@teste.local", Set.of(Perfil.PESQUISADOR, Perfil.ANALISTA));
        ctx.entrar("multi@teste.local", "Senha12345");
        assertTrue(ctx.permite(Perfil.PESQUISADOR));
        assertTrue(ctx.permite(Perfil.ANALISTA));
        assertFalse(ctx.permite(Perfil.ADMINISTRADOR));
    }

    @Test
    void validaEmailUnicoESenhaForte() {
        assertThrows(RegraNegocioException.class, () -> criarUsuario(2, "ADMIN@SIMULADO.LOCAL", Set.of(Perfil.ANALISTA)));
        assertThrows(RegraNegocioException.class, () -> criarUsuario(2, "sem-email", Set.of(Perfil.ANALISTA)));
        assertThrows(RegraNegocioException.class,
                () -> new Usuario(2, "Fictício", "f@teste.local", "abcdefgh", Set.of(Perfil.ANALISTA)));
        assertThrows(RegraNegocioException.class,
                () -> new Usuario(2, "Fictício", "f@teste.local", "Forte1234", Set.of()));
    }

    @Test
    void preservaUltimoAdministradorEContaAtual() {
        assertThrows(RegraNegocioException.class, () -> ctx.getAdministracao().excluirUsuario(1));
        assertThrows(RegraNegocioException.class, () -> ctx.getAdministracao()
                .salvarUsuario(1, "Admin", "admin@simulado.local", "", Set.of(Perfil.ANALISTA), true));
        assertTrue(ctx.getUsuarioLogado().getPerfis().contains(Perfil.ADMINISTRADOR));
    }

    @Test
    void usuarioDesativadoPerdeAcessoNaSessao() {
        criarUsuario(2, "p@teste.local", Set.of(Perfil.PESQUISADOR));
        ctx.entrar("p@teste.local", "Senha12345");
        ctx.getRepositorio().alterar(estado -> estado.getUsuarios().stream()
                .filter(u -> u.getId() == 2).findFirst().orElseThrow().setAtivo(false));
        assertThrows(AcessoNegadoException.class, () -> ctx.getPesquisas().listarTodas());
    }
}
