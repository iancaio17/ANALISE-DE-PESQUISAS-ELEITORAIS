package integracao;

import aplicacao.SistemaContexto;
import dominio.admin.Perfil;
import dominio.base.PerfilDemografico;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.TipoPergunta;
import dominio.pesquisa.TipoPesquisa;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/** Prepara um sistema novo (arquivo temporário, administrador logado) e métodos de apoio para os testes. */
abstract class TesteBase {

    @TempDir
    Path pasta;
    SistemaContexto ctx;

    @BeforeEach
    void abrirSistema() {
        ctx = new SistemaContexto(pasta.resolve("sistema.dat"));
        ctx.entrar("admin@simulado.local", "Admin12345");
    }

    @AfterEach
    void fecharSistema() {
        ctx.close();
    }

    void criarUsuario(int id, String email, Set<Perfil> perfis) {
        ctx.getAdministracao().salvarUsuario(id, "Usuário fictício " + id, email, "Senha12345", perfis, true);
    }

    void criarPesquisa(String codigo, int meta) {
        criarPesquisa(codigo, meta, TipoPesquisa.OUTRA);
    }

    void criarPesquisa(String codigo, int meta, TipoPesquisa tipo) {
        ctx.getPesquisas().salvarPesquisa(codigo, true, "Pesquisa fictícia", Cargo.GOVERNADOR,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "Região fictícia", meta, tipo,
                "Cotas simuladas", List.of("Norte", "Sul"));
    }

    Pergunta criarPergunta(String codigo) {
        Pergunta pergunta = new Pergunta("Opção preferida?", TipoPergunta.UNICA_ESCOLHA);
        pergunta.adicionarOpcao(new OpcaoResposta("Alfa"));
        pergunta.adicionarOpcao(new OpcaoResposta("Beta"));
        ctx.getPesquisas().salvarPergunta(codigo, pergunta);
        return pergunta;
    }

    PerfilDemografico perfil() {
        return new PerfilDemografico("16-34", "Norte", "Médio", "Até 2 SM", "Feminino");
    }

    /** Pesquisa com uma pergunta, já em coleta, e um entrevistador de código 1. */
    void prepararColeta(String codigo, int meta) {
        criarPesquisa(codigo, meta);
        criarPergunta(codigo);
        ctx.getPesquisas().iniciarColeta(codigo);
        ctx.getColeta().salvarEntrevistador(1, "Entrevistador fictício");
    }

    /** Cadastra um participante novo e registra uma entrevista com a resposta informada. */
    void entrevistar(String codigo, int id, String resposta) {
        ctx.getColeta().salvarParticipante(id, "Pessoa fictícia " + id, perfil());
        Pergunta pergunta = ctx.getPesquisas().buscarPesquisaPorCodigo(codigo).getPerguntas().get(0);
        ctx.getColeta().registrarEntrevista(codigo, id, id, 1, Map.of(pergunta.getId(), List.of(resposta)));
    }
}
