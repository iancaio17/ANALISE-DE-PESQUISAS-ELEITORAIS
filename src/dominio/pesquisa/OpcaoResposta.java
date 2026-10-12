package dominio.pesquisa;

public class OpcaoResposta {
    private String textoOpcao;

    public OpcaoResposta(String textoOpcao) {
        if (textoOpcao == null || textoOpcao.trim().isEmpty()) {
            throw new IllegalArgumentException("O texto da opção não pode ser vazio.");
        }
        this.textoOpcao = textoOpcao;
    }

    public String getTextoOpcao() { return textoOpcao; }
    public void setTextoOpcao(String textoOpcao) { this.textoOpcao = textoOpcao; }
}