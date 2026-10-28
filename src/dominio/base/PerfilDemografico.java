package dominio.base;

import java.util.Objects;

public class PerfilDemografico {

    private String faixaEtaria;
    private String regiao;
    private String escolaridade;
    private String faixaRenda;

    public PerfilDemografico(String faixaEtaria, String regiao, String escolaridade, String faixaRenda){
        this.faixaEtaria = faixaEtaria;
        this.regiao = regiao;
        this.escolaridade = escolaridade;
        this.faixaRenda = faixaRenda;
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

    public void setFaixaEtaria(String faixaEtaria) {
        this.faixaEtaria = faixaEtaria;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public void setEscolaridade(String escolaridade) {
        this.escolaridade = escolaridade;
    }

    public void setFaixaRenda(String faixaRenda) {
        this.faixaRenda = faixaRenda;
    }

    public String exibirPerfil() {
        return "Região: " + regiao + " | Idade: " + faixaEtaria +
                " | Escolaridade: " + escolaridade + " | Renda: " + faixaRenda;
    }

    // NOVO: sem isto, PlanoAmostral (HashMap<PerfilDemografico,...>) nunca reconhece dois perfis iguais
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerfilDemografico)) return false;
        PerfilDemografico p = (PerfilDemografico) o;
        return Objects.equals(faixaEtaria, p.faixaEtaria) && Objects.equals(regiao, p.regiao)
                && Objects.equals(escolaridade, p.escolaridade) && Objects.equals(faixaRenda, p.faixaRenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faixaEtaria, regiao, escolaridade, faixaRenda);
    }
}
