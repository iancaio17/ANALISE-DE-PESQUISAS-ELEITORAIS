package dominio.analise;

import dominio.coleta.RespostaEspecial;
import java.util.List;
import java.util.Locale;

/**
 * Relatório abstrato (Template Method): as subclasses montam as linhas; esta classe sabe
 * transformá-las em texto para a tela. Os exportadores (CSV e PDF) também usam estas linhas.
 *
 * <p>Convenção das linhas: 0 posições = linha em branco; 1 = título ou seção;
 * 2 = par "chave / valor"; 3 ou mais = linha de tabela.
 */
public abstract class Relatorio {

    protected static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final int MINIMO_COLUNAS_DE_TABELA = 3;

    public abstract String getTitulo();

    public abstract List<String[]> gerarLinhas();

    /** Gráficos do relatório, exportados junto com as tabelas. Por padrão, o relatório não tem gráficos. */
    public List<Grafico> gerarGraficos() {
        return List.of();
    }

    /** Versão em texto, com colunas alinhadas, para exibir na tela e gerar o PDF. */
    public String paraTexto() {
        List<String[]> linhas = gerarLinhas();
        int[] larguras = calcularLarguras(linhas);
        StringBuilder texto = new StringBuilder();
        for (String[] linha : linhas) {
            for (int coluna = 0; coluna < linha.length; coluna++) {
                boolean ultima = coluna == linha.length - 1;
                texto.append(ultima ? linha[coluna] : completar(linha[coluna], larguras[coluna]) + "  ");
            }
            texto.append('\n');
        }
        return texto.toString();
    }

    /** Só as linhas de tabela definem a largura; pares "chave / valor" não a inflam. */
    private int[] calcularLarguras(List<String[]> linhas) {
        int colunas = 0;
        for (String[] linha : linhas) {
            colunas = Math.max(colunas, linha.length);
        }
        int[] larguras = new int[colunas];
        for (String[] linha : linhas) {
            if (linha.length < MINIMO_COLUNAS_DE_TABELA) {
                continue;
            }
            for (int i = 0; i < linha.length; i++) {
                larguras[i] = Math.max(larguras[i], linha[i].length());
            }
        }
        return larguras;
    }

    private static String completar(String texto, int largura) {
        return texto + " ".repeat(Math.max(0, largura - texto.length()));
    }

    /** Formata um percentual com uma casa decimal, no padrão brasileiro. */
    protected static String percentual(double valor) {
        return String.format(PT_BR, "%.1f", valor);
    }

    /** Acrescenta a tabela de uma distribuição (itens, base válida, respostas especiais e total). */
    protected static void adicionarDistribuicao(List<String[]> linhas, Distribuicao distribuicao) {
        linhas.add(new String[] {"Opção", "Respostas", "Percentual (%)"});
        for (ItemResultado item : distribuicao.getItens()) {
            linhas.add(new String[] {item.getRotulo(), String.valueOf(item.getQuantidadeAbsoluta()),
                    percentual(item.getPercentual())});
        }
        linhas.add(new String[] {"Respostas válidas (base do cálculo)",
                String.valueOf(distribuicao.getTotalValidas()), ""});
        for (RespostaEspecial tipo : RespostaEspecial.values()) {
            linhas.add(new String[] {tipo.getTexto(), String.valueOf(distribuicao.getEspecial(tipo)), ""});
        }
        linhas.add(new String[] {"Total de entrevistas", String.valueOf(distribuicao.getTotalEntrevistas()), ""});
    }
}
