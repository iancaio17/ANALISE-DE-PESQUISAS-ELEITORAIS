package dominio.base;

import java.io.Serializable;
import java.util.Objects;

public class PerfilDemografico implements Serializable {
    private static final long serialVersionUID = 1L;

    private String faixaEtaria;
    private String regiao;
    private String escolaridade;
    private String faixaRenda;
    private String genero;

    public PerfilDemografico(String faixaEtaria, String regiao, String escolaridade, String faixaRenda) {
        this(faixaEtaria, regiao, escolaridade, faixaRenda, "Não informado");
    }

    public PerfilDemografico(String faixaEtaria, String regiao, String escolaridade, String faixaRenda, String genero) {
        this.faixaEtaria = faixaEtaria;
        this.regiao = regiao;
        this.escolaridade = escolaridade;
        this.faixaRenda = faixaRenda;
        this.genero = (genero == null || genero.trim().isEmpty()) ? "Não informado" : genero;
    }

    public String getFaixaEtaria() { return faixaEtaria; }
    public String getRegiao() { return regiao; }
    public String getEscolaridade() { return escolaridade; }
    public String getFaixaRenda() { return faixaRenda; }
    public String getGenero() { return genero; }

    public void setFaixaEtaria(String faixaEtaria) { this.faixaEtaria = faixaEtaria; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
    public void setEscolaridade(String escolaridade) { this.escolaridade = escolaridade; }
    public void setFaixaRenda(String faixaRenda) { this.faixaRenda = faixaRenda; }
    public void setGenero(String genero) { this.genero = genero; }

    public String exibirPerfil() {
        return "Região: " + regiao + " | Idade: " + faixaEtaria +
                " | Escolaridade: " + escolaridade + " | Renda: " + faixaRenda +
                " | Género: " + genero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerfilDemografico that = (PerfilDemografico) o;
        return Objects.equals(faixaEtaria, that.faixaEtaria) &&
                Objects.equals(regiao, that.regiao) &&
                Objects.equals(escolaridade, that.escolaridade) &&
                Objects.equals(faixaRenda, that.faixaRenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faixaEtaria, regiao, escolaridade, faixaRenda);
    }
}