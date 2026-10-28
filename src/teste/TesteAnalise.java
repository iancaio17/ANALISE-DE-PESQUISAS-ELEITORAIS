package teste;

import aplicacao.servicos.AnaliseService;
import dominio.analise.Analise;
import dominio.analise.RelatorioComparativo;
import dominio.analise.RelatorioConsolidado;
import dominio.analise.RespostasEspeciais;
import dominio.candidato.Candidato;
import dominio.coleta.Entrevista;
import dominio.coleta.Entrevistador;
import dominio.coleta.Participante;
import dominio.coleta.Resposta;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.PlanoAmostral;
import infraestrutura.excecoes.AnaliseException;
import persistencia.arquivos.AnaliseRepositorioArquivo;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;

/**
 * Teste manual do módulo de Análise Estatística, com dados 100% FICTÍCIOS.
 * Execute a classe main e confira o console e a pasta "saida/".
 */
public class TesteAnalise {

    private static final String[] REGIOES = {"Norte", "Sul", "Centro"};
    private static final String[] IDADES = {"16-34", "35-59", "60+"};

    public static void main(String[] args) {
        Candidato alfa = new Candidato(1, "Candidato Alfa", 11, "Governador", "Candidato");
        Candidato beta = new Candidato(2, "Candidato Beta", 22, "Governador", "Candidato");
        Candidato gama = new Candidato(3, "Candidato Gama", 33, "Governador", "Candidato");
        Candidato[] candidatos = {alfa, beta, gama};

        ColetaService coleta = new ColetaService();
        Pesquisa p1 = criarPesquisa("P001", "Pesquisa Fictícia 1", candidatos, coleta, 24, 0);
        Pesquisa p2 = criarPesquisa("P002", "Pesquisa Fictícia 2", candidatos, coleta, 18, 1);
        Pesquisa aberta = criarPesquisaAberta("P003", candidatos);

        AnaliseService service = new AnaliseService(new AnaliseRepositorioArquivo(Paths.get("saida", "analises.dat")), coleta);

        System.out.println("\n### 1) Regra: pesquisa não encerrada não pode ser processada");
        try {
            service.processarPesquisa(aberta);
        } catch (AnaliseException e) {
            System.out.println("OK (erro esperado): " + e.getMessage());
        }

        System.out.println("\n### 2) Processar P001 e mostrar relatório consolidado");
        Analise a1 = service.processarPesquisa(p1);
        System.out.println("Situação da pesquisa após processar: " + p1.getSituacao());
        RelatorioConsolidado rel = service.gerarRelatorioConsolidado("P001");
        System.out.println(rel.paraTexto());

        System.out.println("### 3) Exportar CSV e PDF");
        service.exportarRelatorio(rel, "csv", Paths.get("saida", "relatorio_P001.csv"));
        service.exportarRelatorio(rel, "pdf", Paths.get("saida", "relatorio_P001.pdf"));
        System.out.println("Arquivos gerados na pasta saida/");
        try {
            service.exportarRelatorio(rel, "xls", Paths.get("saida", "x.xls"));
        } catch (AnaliseException e) {
            System.out.println("OK (erro esperado): " + e.getMessage());
        }

        System.out.println("\n### 4) Processar P002 e comparar com P001");
        service.processarPesquisa(p2);
        RelatorioComparativo comp = service.gerarRelatorioComparativo("P001", "P002");
        System.out.println(comp.paraTexto());
        service.exportarRelatorio(comp, "pdf", Paths.get("saida", "comparacao_P001_P002.pdf"));

        System.out.println("### 5) Análise com filtro (somente região Norte), sem salvar");
        Analise filtrada = service.analisarComFiltro(p1,
                e -> "Norte".equals(e.getParticipante().getPerfilDemografico().getRegiao()));
        System.out.println("Entrevistas na região Norte: " + filtrada.getTotalEntrevistas()
                + " (total da pesquisa: " + a1.getTotalEntrevistas() + ")");

        System.out.println("\n### 6) Persistência: reabrir o arquivo e listar análises salvas");
        AnaliseService outro = new AnaliseService(new AnaliseRepositorioArquivo(Paths.get("saida", "analises.dat")), coleta);
        System.out.println("Análises recuperadas do arquivo: " + outro.listarAnalises().size());

        System.out.println("\n### 7) Reprocessar P001 (permitido) e checar que continua uma só análise");
        service.processarPesquisa(p1);
        System.out.println("Análises salvas: " + service.listarAnalises().size());
    }

    private static PerfilDemografico perfil(int i, int deslocamento) {
        return new PerfilDemografico(IDADES[(i + deslocamento) % 3], REGIOES[i % 3],
                i % 2 == 0 ? "Médio" : "Superior", i % 4 == 0 ? "Até 2 SM" : "2 a 5 SM");
    }

    private static Pesquisa novaPesquisa(String codigo, String nome, Candidato[] candidatos, int previstas) {
        PlanoAmostral plano = new PlanoAmostral();
        for (int i = 0; i < 3; i++) plano.adicionarMeta(perfil(i, 0), 2);
        Pesquisa p = new Pesquisa(codigo, nome, "Governador", "Intenção de voto", previstas, plano);
        for (Candidato c : candidatos) p.associarCandidato(c);
        Pergunta pergunta = new Pergunta("Se a eleição fosse hoje, em quem votaria?", "Única escolha");
        for (Candidato c : candidatos) pergunta.adicionarOpcao(new OpcaoResposta(c.getNome()));
        pergunta.adicionarOpcao(new OpcaoResposta(RespostasEspeciais.NAO_SABE));
        p.adicionarPergunta(pergunta);
        return p;
    }

    private static Pesquisa criarPesquisa(String codigo, String nome, Candidato[] candidatos,
                                          ColetaService coleta, int qtd, int deslocamento) {
        Pesquisa p = novaPesquisa(codigo, nome, candidatos, 30);
        Pergunta pergunta = p.getPerguntas().get(0);
        Entrevistador entrevistador = new Entrevistador(1, "Entrevistador Fictício");
        p.iniciarColeta();

        PrintStream original = System.out;
        System.setOut(new PrintStream(new OutputStream() { public void write(int b) { } }));
        try {
            for (int i = 0; i < qtd; i++) {
                Participante part = new Participante(i + 1, "Participante " + (i + 1), perfil(i, deslocamento));
                Entrevista e = new Entrevista(i + 1, p, part, entrevistador);
                String escolha;
                if (i % 7 == 0) escolha = RespostasEspeciais.NAO_SABE;
                else if (i % 11 == 0) escolha = RespostasEspeciais.NAO_RESPONDEU;
                else escolha = candidatos[(i + deslocamento) % 3].getNome();
                e.registrarResposta(new Resposta(pergunta, escolha));
                coleta.registrarEntrevista(e);
            }
        } finally {
            System.setOut(original);
        }
        p.encerrarColeta();
        return p;
    }

    private static Pesquisa criarPesquisaAberta(String codigo, Candidato[] candidatos) {
        Pesquisa p = novaPesquisa(codigo, "Pesquisa Fictícia em Coleta", candidatos, 10);
        p.iniciarColeta();
        return p;
    }
}
