package infraestrutura.exportacao;

import dominio.analise.Relatorio;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Gera um PDF simples (texto monoespaçado e gráficos de barras, várias páginas A4) sem bibliotecas
 * externas: primeiro as páginas de texto com as tabelas, depois as páginas com os gráficos.
 * Como o restante do sistema só conhece {@link ExportadorRelatorio}, esta classe pode ser
 * trocada por uma versão com iText ou PDFBox sem alterar mais nada.
 */
public class ExportadorPDF extends ExportadorArquivo {

    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private static final int CARACTERES_POR_LINHA = 82;
    private static final int LINHAS_POR_PAGINA = 56;

    @Override
    public String getExtensao() {
        return "pdf";
    }

    @Override
    protected byte[] montarConteudo(Relatorio relatorio) throws IOException {
        List<String> conteudos = new ArrayList<>();
        for (List<String> linhas : dividirEmPaginas(quebrarLinhas(relatorio.paraTexto()))) {
            conteudos.add(comandosDeTexto(linhas));
        }
        conteudos.addAll(new DesenhistaDeGraficos().desenhar(relatorio.gerarGraficos()));
        return montarPdf(conteudos);
    }

    private List<String> quebrarLinhas(String texto) {
        List<String> saida = new ArrayList<>();
        for (String linha : texto.split("\n", -1)) {
            String resto = linha;
            while (resto.length() > CARACTERES_POR_LINHA) {
                int corte = resto.lastIndexOf(' ', CARACTERES_POR_LINHA);
                if (corte < CARACTERES_POR_LINHA / 2) {
                    corte = CARACTERES_POR_LINHA;
                }
                saida.add(resto.substring(0, corte));
                resto = resto.substring(corte).stripLeading();
            }
            saida.add(resto);
        }
        return saida;
    }

    private List<List<String>> dividirEmPaginas(List<String> linhas) {
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < linhas.size(); i += LINHAS_POR_PAGINA) {
            paginas.add(linhas.subList(i, Math.min(i + LINHAS_POR_PAGINA, linhas.size())));
        }
        if (paginas.isEmpty()) {
            paginas.add(new ArrayList<>());
        }
        return paginas;
    }

    /** Objetos do PDF: 1 = catálogo, 2 = árvore de páginas, 3 = fonte; depois, página e conteúdo. */
    private byte[] montarPdf(List<String> conteudos) throws IOException {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        List<Integer> posicoes = new ArrayList<>();
        escrever(saida, "%PDF-1.4\n");

        StringBuilder filhos = new StringBuilder();
        for (int i = 0; i < conteudos.size(); i++) {
            filhos.append(4 + 2 * i).append(" 0 R ");
        }
        escreverObjeto(saida, posicoes, "<< /Type /Catalog /Pages 2 0 R >>");
        escreverObjeto(saida, posicoes,
                "<< /Type /Pages /Kids [ " + filhos + "] /Count " + conteudos.size() + " >>");
        escreverObjeto(saida, posicoes,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>");

        for (int i = 0; i < conteudos.size(); i++) {
            int idConteudo = 5 + 2 * i;
            escreverObjeto(saida, posicoes, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents "
                    + idConteudo + " 0 R /Resources << /Font << /F1 3 0 R >> >> >>");
            escreverConteudoDaPagina(saida, posicoes, conteudos.get(i));
        }
        escreverTabelaDeReferencias(saida, posicoes);
        return saida.toByteArray();
    }

    /** Comandos que escrevem as linhas de texto de uma página. */
    private String comandosDeTexto(List<String> linhas) {
        StringBuilder comandos = new StringBuilder("BT\n/F1 10 Tf\n13 TL\n50 792 Td\n");
        for (String linha : linhas) {
            comandos.append('(').append(escapar(linha)).append(") Tj T*\n");
        }
        comandos.append("ET");
        return comandos.toString();
    }

    private void escreverConteudoDaPagina(ByteArrayOutputStream saida, List<Integer> posicoes,
            String comandos) throws IOException {
        byte[] bytes = comandos.getBytes(WIN_ANSI);

        posicoes.add(saida.size());
        escrever(saida, posicoes.size() + " 0 obj\n<< /Length " + bytes.length + " >>\nstream\n");
        saida.write(bytes);
        escrever(saida, "\nendstream\nendobj\n");
    }

    private void escreverTabelaDeReferencias(ByteArrayOutputStream saida, List<Integer> posicoes)
            throws IOException {
        int inicio = saida.size();
        int total = posicoes.size() + 1;
        escrever(saida, "xref\n0 " + total + "\n0000000000 65535 f \n");
        for (int posicao : posicoes) {
            escrever(saida, String.format("%010d 00000 n \n", posicao));
        }
        escrever(saida, "trailer\n<< /Size " + total + " /Root 1 0 R >>\nstartxref\n" + inicio + "\n%%EOF\n");
    }

    private void escreverObjeto(ByteArrayOutputStream saida, List<Integer> posicoes, String corpo)
            throws IOException {
        posicoes.add(saida.size());
        escrever(saida, posicoes.size() + " 0 obj\n" + corpo + "\nendobj\n");
    }

    private void escrever(ByteArrayOutputStream saida, String texto) throws IOException {
        saida.write(texto.getBytes(WIN_ANSI));
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
