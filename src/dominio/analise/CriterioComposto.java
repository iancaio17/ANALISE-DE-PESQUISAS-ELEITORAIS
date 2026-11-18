package dominio.analise;

import dominio.coleta.Entrevista;

/** Combina dois critérios para gerar a distribuição conjunta (ex.: Região × Faixa etária). */
public class CriterioComposto implements CriterioAgrupamento {

    private final CriterioAgrupamento primeiro;
    private final CriterioAgrupamento segundo;

    public CriterioComposto(CriterioAgrupamento primeiro, CriterioAgrupamento segundo) {
        if (primeiro == null || segundo == null) {
            throw new IllegalArgumentException("Os dois critérios são obrigatórios.");
        }
        this.primeiro = primeiro;
        this.segundo = segundo;
    }

    @Override
    public String getNome() {
        return primeiro.getNome() + " × " + segundo.getNome();
    }

    @Override
    public String grupoDe(Entrevista entrevista) {
        return primeiro.grupoDe(entrevista) + " / " + segundo.grupoDe(entrevista);
    }
}
