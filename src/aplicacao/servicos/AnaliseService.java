package aplicacao.servicos;

import dominio.analise.Analise;
import dominio.analise.Relatorio;
import dominio.pesquisa.Pesquisa;
import java.util.ArrayList;
import java.util.List;

public class AnaliseService {

    private List<Analise> bancoAnalises;

    public AnaliseService() {
        this.bancoAnalises = new ArrayList<>();
    }

    public void processarPesquisa(Pesquisa pesquisa) {
        if (pesquisa == null) throw new IllegalArgumentException("Pesquisa inválida para processamento.");

        if (!pesquisa.getSituacao().equalsIgnoreCase("Encerrada")) {
            throw new IllegalStateException("Erro: Uma pesquisa somente poderá ser processada após o encerramento da coleta.");
        }

        Analise novaAnalise = new Analise(pesquisa);


        bancoAnalises.add(novaAnalise);
        System.out.println("Sucesso: Processamento estatístico concluído para a pesquisa '" + pesquisa.getCodigo() + "'.");
    }

    public void exportarRelatorio(Pesquisa pesquisa, String formato) {
        Analise analiseAlvo = null;
        for (Analise a : bancoAnalises) {
            if (a.getPesquisa().getCodigo().equals(pesquisa.getCodigo())) {
                analiseAlvo = a;
                break;
            }
        }

        if (analiseAlvo == null) {
            throw new IllegalArgumentException("Erro: Não há processamento estatístico disponível para esta pesquisa.");
        }

        Relatorio relatorio = new Relatorio(analiseAlvo, formato);
        relatorio.exportar();
    }

    public List<Analise> getBancoAnalises() { return bancoAnalises; }
}