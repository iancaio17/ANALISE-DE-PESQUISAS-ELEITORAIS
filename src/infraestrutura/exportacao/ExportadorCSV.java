package infraestrutura.exportacao;

import dominio.analise.Grafico;
import dominio.analise.Relatorio;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exporta em CSV com separador ';' (padrão do Excel em português) e UTF-8 com BOM.
 * O CSV só guarda texto; por isso cada gráfico vira uma tabela de dados com barras em texto.
 */
public class ExportadorCSV extends ExportadorArquivo {

    private static final String BOM = "﻿";
    private static final char SEPARADOR = ';';
    private static final String QUEBRA_DE_LINHA = "\r\n";

    @Override
    public String getExtensao() {
        return "csv";
    }

    @Override
    protected byte[] montarConteudo(Relatorio relatorio) {
        StringBuilder texto = new StringBuilder(BOM);
        escreverLinhas(texto, relatorio.gerarLinhas());
        for (Grafico grafico : relatorio.gerarGraficos()) {
            texto.append(QUEBRA_DE_LINHA);
            escreverLinhas(texto, grafico.gerarLinhas());
        }
        return texto.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void escreverLinhas(StringBuilder texto, List<String[]> linhas) {
        for (String[] linha : linhas) {
            for (int i = 0; i < linha.length; i++) {
                if (i > 0) {
                    texto.append(SEPARADOR);
                }
                texto.append(escapar(linha[i]));
            }
            texto.append(QUEBRA_DE_LINHA);
        }
    }

    private String escapar(String valor) {
        boolean precisaDeAspas = valor.contains(";") || valor.contains("\"") || valor.contains("\n");
        if (!precisaDeAspas) {
            return valor;
        }
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}
