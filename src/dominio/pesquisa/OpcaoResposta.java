package dominio.pesquisa;

import infraestrutura.excecoes.PesquisaRegraNegocioException;
import java.util.UUID;

public class OpcaoResposta {
    private final String id;
    private String texto;

    public OpcaoResposta(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new PesquisaRegraNegocioException("O texto da opção de resposta não pode ser vazio.");
        }
        this.id = UUID.randomUUID().toString();
        this.texto = texto;
    }

    public String getId() { return id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}