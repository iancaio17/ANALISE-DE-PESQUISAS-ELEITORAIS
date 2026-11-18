package infraestrutura.utils;

import dominio.analise.Relatorio;
import infraestrutura.excecoes.ExportacaoException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Gera um PDF simples (texto monoespaçado, várias páginas A4) SEM bibliotecas externas.
 * Se o grupo preferir PDFs mais bonitos, pode trocar esta classe por uma versão com iText/PDFBox
 * sem mudar mais nada, pois o resto do sistema só conhece a interface ExportadorRelatorio.
 */
public class ExportadorPDF implements ExportadorRelatorio {

    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private static final int LARGURA_MAX = 82;   // caracteres por linha (Courier 10pt em A4 com margens de 50pt)
    private static final int LINHAS_POR_PAGINA = 56;

    @Override
    public String getExtensao() { return "pdf"; }

    @Override
    public void exportar(Relatorio relatorio, Path destino) {
        try {
            List<String> linhas = quebrarLinhas(relatorio.paraTexto());
            byte[] pdf = montarPdf(linhas);
            if (destino.toAbsolutePath().getParent() != null) {
                Files.createDirectories(destino.toAbsolutePath().getParent());
            }
            Files.write(destino, pdf);
        } catch (IOException e) {
            throw new ExportacaoException("Não foi possível gravar o arquivo PDF em '" + destino + "'.", e);
        }
    }

    private List<String> quebrarLinhas(String texto) {
        List<String> saida = new ArrayList<>();
        for (String linha : texto.split("\n", -1)) {
            String resto = linha;
            if (resto.isEmpty()) { saida.add(""); continue; }
            while (resto.length() > LARGURA_MAX) {
                saida.add(resto.substring(0, LARGURA_MAX));
                resto = resto.substring(LARGURA_MAX);
            }
            saida.add(resto);
        }
        return saida;
    }

    private byte[] montarPdf(List<String> linhas) throws IOException {
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < linhas.size(); i += LINHAS_POR_PAGINA) {
            paginas.add(linhas.subList(i, Math.min(i + LINHAS_POR_PAGINA, linhas.size())));
        }
        if (paginas.isEmpty()) paginas.add(new ArrayList<String>());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        escrever(out, "%PDF-1.4\n");

        // 1 = catálogo, 2 = árvore de páginas, 3 = fonte; páginas: 4+2i (página) e 5+2i (conteúdo)
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < paginas.size(); i++) kids.append(4 + 2 * i).append(" 0 R ");

        objeto(out, offsets, "<< /Type /Catalog /Pages 2 0 R >>");
        objeto(out, offsets, "<< /Type /Pages /Kids [ " + kids + "] /Count " + paginas.size() + " >>");
        objeto(out, offsets, "<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>");

        for (int i = 0; i < paginas.size(); i++) {
            int idConteudo = 5 + 2 * i;
            objeto(out, offsets, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents " + idConteudo
                    + " 0 R /Resources << /Font << /F1 3 0 R >> >> >>");

            StringBuilder cs = new StringBuilder("BT\n/F1 10 Tf\n13 TL\n50 792 Td\n");
            for (String l : paginas.get(i)) {
                cs.append('(').append(escapar(l)).append(") Tj T*\n");
            }
            cs.append("ET");
            byte[] bytes = cs.toString().getBytes(WIN_ANSI);

            offsets.add(out.size());
            escrever(out, (offsets.size()) + " 0 obj\n<< /Length " + bytes.length + " >>\nstream\n");
            out.write(bytes);
            escrever(out, "\nendstream\nendobj\n");
        }

        int inicioXref = out.size();
        int total = offsets.size() + 1;
        escrever(out, "xref\n0 " + total + "\n0000000000 65535 f \n");
        for (int off : offsets) escrever(out, String.format("%010d 00000 n \n", off));
        escrever(out, "trailer\n<< /Size " + total + " /Root 1 0 R >>\nstartxref\n" + inicioXref + "\n%%EOF\n");
        return out.toByteArray();
    }

    private void objeto(ByteArrayOutputStream out, List<Integer> offsets, String corpo) throws IOException {
        offsets.add(out.size());
        escrever(out, offsets.size() + " 0 obj\n" + corpo + "\nendobj\n");
    }

    private void escrever(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(WIN_ANSI));
    }

    private String escapar(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
