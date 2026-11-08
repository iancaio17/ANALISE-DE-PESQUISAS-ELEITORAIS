package infraestrutura.utils;

import dominio.analise.Relatorio;
import infraestrutura.excecoes.ExportacaoException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Exporta em CSV com separador ';' (padrão do Excel em português) e UTF-8 com BOM. */
public class ExportadorCSV implements ExportadorRelatorio {

    @Override
    public String getExtensao() { return "csv"; }

    @Override
    public void exportar(Relatorio relatorio, Path destino) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        List<String[]> linhas = relatorio.gerarLinhas();
        for (String[] linha : linhas) {
            for (int i = 0; i < linha.length; i++) {
                sb.append(escapar(linha[i]));
                if (i < linha.length - 1) sb.append(';');
            }
            sb.append("\r\n");
        }
        try {
            if (destino.toAbsolutePath().getParent() != null) {
                Files.createDirectories(destino.toAbsolutePath().getParent());
            }
            Files.write(destino, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ExportacaoException("Não foi possível gravar o arquivo CSV em '" + destino + "'.", e);
        }
    }

    private String escapar(String valor) {
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
