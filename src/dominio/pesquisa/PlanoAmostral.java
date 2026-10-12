package dominio.pesquisa;

import dominio.base.PerfilDemografico;
import java.util.HashMap;
import java.util.Map;

public class PlanoAmostral {
    private Map<PerfilDemografico, Integer> metasPorPerfil;

    public PlanoAmostral() {
        this.metasPorPerfil = new HashMap<>();
    }

    public Map<PerfilDemografico, Integer> getMetasPorPerfil() { return metasPorPerfil; }

    public void adicionarMeta(PerfilDemografico perfil, int quantidadeMinima) {
        if (perfil != null && quantidadeMinima > 0) {
            this.metasPorPerfil.put(perfil, quantidadeMinima);
        }
    }
}