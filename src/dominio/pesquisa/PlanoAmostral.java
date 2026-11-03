package dominio.pesquisa;

import java.util.HashMap;
import java.util.Map;

public class PlanoAmostral {
    private String metodologia;
    private final Map<String, Integer> metasPorGrupo;

    public PlanoAmostral(String metodologia) {
        this.metodologia = metodologia;
        this.metasPorGrupo = new HashMap<>();
    }

    public void definirMetaGrupo(String grupo, int quantidadeMinima) {
        this.metasPorGrupo.put(grupo, quantidadeMinima);
    }

    public boolean atingiuMeta(String grupo, int entrevistasValidasRealizadas) {
        int meta = metasPorGrupo.getOrDefault(grupo, 0);
        return entrevistasValidasRealizadas >= meta;
    }

    public String getMetodologia() { return metodologia; }
    public Map<String, Integer> getMetasPorGrupo() { return Map.copyOf(metasPorGrupo); }
}