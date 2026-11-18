package integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aplicacao.SistemaContexto;
import aplicacao.servicos.DadosDemonstracao;
import dominio.admin.Perfil;
import dominio.admin.Usuario;
import infraestrutura.excecoes.PersistenciaException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Gravação em arquivo, reabertura, exportação e proteção contra falhas. */
class PersistenciaTest extends TesteBase {

    @Test
    void reabrirMantemTodosOsModulos() {
        DadosDemonstracao.carregar(ctx);
        ctx.close();
        ctx = new SistemaContexto(pasta.resolve("sistema.dat"));
        ctx.entrar("admin@simulado.local", "Admin12345");

        assertEquals(4, ctx.getPesquisas().listarTodas().size());
        assertEquals(16, ctx.getColeta().getBancoEntrevistas().size());
        assertEquals(3, ctx.getCandidatos().getBancoCandidatos().size());
        assertEquals(8, ctx.getAnalise().buscarAnalise("P002").getTotalEntrevistas());
        ctx.getAnalise().processarPesquisa("P001");
        assertEquals(6, ctx.getAnalise().buscarAnalise("P001").getTotalEntrevistas());
    }

    @Test
    void exportaCsvEPdfReais() throws Exception {
        DadosDemonstracao.carregar(ctx);
        var relatorio = ctx.getAnalise().gerarRelatorioConsolidado("P002");
        Path csv = pasta.resolve("resultado.csv");
        Path pdf = pasta.resolve("resultado.pdf");

        ctx.getAnalise().exportarRelatorio(relatorio, "csv", csv);
        ctx.getAnalise().exportarRelatorio(relatorio, "pdf", pdf);

        assertTrue(Files.readString(csv).contains("Candidato Alfa"));
        assertTrue(new String(Files.readAllBytes(pdf), StandardCharsets.ISO_8859_1).startsWith("%PDF-1.4"));
        assertTrue(Files.size(pdf) > 1000);
    }

    @Test
    void exportacaoIncluiOsGraficos() throws Exception {
        DadosDemonstracao.carregar(ctx);
        ctx.getAnalise().processarPesquisa("P001");
        var consolidado = ctx.getAnalise().gerarRelatorioConsolidado("P002");
        var comparativo = ctx.getAnalise().gerarRelatorioComparativo("P001", "P002");
        Path csv = pasta.resolve("comparacao.csv");
        Path pdf = pasta.resolve("comparacao.pdf");
        Path pdfSemGraficos = pasta.resolve("usuarios.pdf");

        ctx.getAnalise().exportarRelatorio(comparativo, "csv", csv);
        ctx.getAnalise().exportarRelatorio(comparativo, "pdf", pdf);
        ctx.getAdministracao().exportarRelatorio(ctx.getAdministracao().gerarRelatorioUsuarios(), "pdf",
                pdfSemGraficos);

        String textoCsv = Files.readString(csv);
        assertTrue(textoCsv.contains("GRÁFICO: Em quem votaria para governador?"));
        assertTrue(textoCsv.contains("█"));
        assertEquals(1, consolidado.gerarGraficos().size());
        assertEquals(2, comparativo.gerarGraficos().get(0).getSeries().size());
        String conteudoPdf = new String(Files.readAllBytes(pdf), StandardCharsets.ISO_8859_1);
        assertTrue(conteudoPdf.contains("re f"), "o PDF deve desenhar barras");
        assertTrue(Files.size(pdf) > Files.size(pdfSemGraficos));
    }

    @Test
    void exportaRelatorioDeUsuarios() throws Exception {
        Path csv = pasta.resolve("usuarios.csv");
        ctx.getAdministracao().exportarRelatorio(ctx.getAdministracao().gerarRelatorioUsuarios(), "csv", csv);
        assertTrue(Files.readString(csv).contains("admin@simulado.local"));
    }

    @Test
    void falhaNaGravacaoNaoPublicaOEstado() throws Exception {
        Path arquivo = pasta.resolve("sistema.dat");
        Files.delete(arquivo);
        Files.createDirectory(arquivo);

        assertThrows(PersistenciaException.class, () -> criarUsuario(2, "outro@teste.local", Set.of(Perfil.ANALISTA)));
        assertEquals(1, ctx.getAdministracao().listarUsuarios().size());
    }

    @Test
    void copiasExternasNaoAlteramOQueFoiGravado() {
        Usuario usuario = ctx.getUsuarioLogado();
        usuario.setNome("Mudança externa");
        assertNotEquals("Mudança externa", ctx.getUsuarioLogado().getNome());

        ctx.getRepositorio().ler().getUsuarios().clear();
        assertEquals(1, ctx.getAdministracao().listarUsuarios().size());
    }

    @Test
    void bloqueiaSegundaInstanciaENaoSobrescreveArquivoCorrompido() throws Exception {
        assertThrows(PersistenciaException.class, () -> new SistemaContexto(pasta.resolve("sistema.dat")));
        ctx.close();
        Files.writeString(pasta.resolve("sistema.dat"), "conteúdo corrompido");

        assertThrows(PersistenciaException.class, () -> new SistemaContexto(pasta.resolve("sistema.dat")));
        assertEquals("conteúdo corrompido", Files.readString(pasta.resolve("sistema.dat")));
    }
}
