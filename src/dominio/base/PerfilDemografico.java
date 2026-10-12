package dominio.base;

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
}
