package dominio.base;

import java.io.Serializable;
import java.util.Objects;

/**
 * Características de amostra de um participante (faixa etária, região, escolaridade, renda e gênero).
 * Também serve como filtro: um campo vazio significa "qualquer categoria".
 */
public final class PerfilDemografico implements Serializable {

    private static final long serialVersionUID = 2L;
    private static final String QUALQUER = "Todas";

    private final String faixaEtaria;
    private final String regiao;
    private final String escolaridade;
    private final String faixaRenda;
    private final String genero;

    public PerfilDemografico(String faixaEtaria, String regiao, String escolaridade, String faixaRenda,
            String genero) {
        this.faixaEtaria = limpar(faixaEtaria);
        this.regiao = limpar(regiao);
        this.escolaridade = limpar(escolaridade);
        this.faixaRenda = limpar(faixaRenda);
        this.genero = limpar(genero);
    }

    public String getFaixaEtaria() {
        return faixaEtaria;
    }

    public String getRegiao() {
        return regiao;
    }

    public String getEscolaridade() {
        return escolaridade;
    }

    public String getFaixaRenda() {
        return faixaRenda;
    }

    public String getGenero() {
        return genero;
    }

    /** Indica se o perfil informado atende a este filtro (campos vazios aceitam qualquer valor). */
    public boolean corresponde(PerfilDemografico outro) {
        return combina(faixaEtaria, outro.faixaEtaria)
                && combina(regiao, outro.regiao)
                && combina(escolaridade, outro.escolaridade)
                && combina(faixaRenda, outro.faixaRenda)
                && combina(genero, outro.genero);
    }

    /** Texto em uma linha com todas as características. */
    public String descrever() {
        return "Região: " + rotulo(regiao)
                + " | Idade: " + rotulo(faixaEtaria)
                + " | Escolaridade: " + rotulo(escolaridade)
                + " | Renda: " + rotulo(faixaRenda)
                + " | Gênero: " + rotulo(genero);
    }

    private static String limpar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private static boolean combina(String filtro, String valor) {
        return filtro.isBlank() || filtro.equalsIgnoreCase(valor);
    }

    private static String rotulo(String texto) {
        return texto.isBlank() ? QUALQUER : texto;
    }

    @Override
    public String toString() {
        return descrever();
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof PerfilDemografico)) {
            return false;
        }
        PerfilDemografico p = (PerfilDemografico) outro;
        return faixaEtaria.equals(p.faixaEtaria) && regiao.equals(p.regiao)
                && escolaridade.equals(p.escolaridade) && faixaRenda.equals(p.faixaRenda)
                && genero.equals(p.genero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faixaEtaria, regiao, escolaridade, faixaRenda, genero);
    }
}
