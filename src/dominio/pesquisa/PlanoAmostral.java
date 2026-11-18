package dominio.pesquisa;

import dominio.base.PerfilDemografico;
import infraestrutura.utils.Validacao;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Metodologia da pesquisa e metas mínimas de entrevistas por grupo da amostra. */
public class PlanoAmostral implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String metodologia;
    private final Map<PerfilDemografico, Integer> metas = new LinkedHashMap<>();

    public PlanoAmostral(String metodologia) {
        this.metodologia = Validacao.texto(metodologia, "Metodologia");
    }

    public void definirMetaPerfil(PerfilDemografico perfil, int quantidade) {
        Validacao.exigir(perfil != null, "Perfil obrigatório.");
        metas.put(perfil, Validacao.positivo(quantidade, "Meta"));
    }

    public void removerMeta(PerfilDemografico perfil) {
        metas.remove(perfil);
    }

    public String getMetodologia() {
        return metodologia;
    }

    public Map<PerfilDemografico, Integer> getMetasPorPerfil() {
        return Collections.unmodifiableMap(metas);
    }
}
