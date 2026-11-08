package dominio.analise;

import dominio.pesquisa.Pesquisa;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Analise {
    private Pesquisa pesquisa;
    private Date dataProcessamento;

    private List<Resultados> resultados;

    public Analise(Pesquisa pesquisa) {
        if (pesquisa == null) {
            throw new IllegalArgumentException("A análise requer uma pesquisa válida.");
        }

        if (!pesquisa.getSituacao().equals("Encerrada") && !pesquisa.getSituacao().equals("Processada")) {
            throw new IllegalStateException("Erro: A pesquisa só pode ser processada após o encerramento da coleta.");
        }

        this.pesquisa = pesquisa;
        this.dataProcessamento = new Date();
        this.resultados = new ArrayList<>();

        this.pesquisa.setSituacao("Processada");
    }

    public Pesquisa getPesquisa() { return pesquisa; }
    public Date getDataProcessamento() { return dataProcessamento; }
    public List<Resultados> getResultados() { return resultados; }

    public void adicionarResultado(Resultados resultado) {
        if (resultado != null) {
            this.resultados.add(resultado);
        }
    }
}