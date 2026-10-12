package dominio.pesquisa;

import java.util.ArrayList;
import java.util.List;

public class Pergunta {
    private String texto;
    private String tipo;
    private List<OpcaoResposta> opcoes;

    public Pergunta(String texto, String tipo) {
        this.texto = texto;
        this.tipo = tipo;
        this.opcoes = new ArrayList<>();
    }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public List<OpcaoResposta> getOpcoes() { return opcoes; }

    // Método para adicionar opções à pergunta
    public void adicionarOpcao(OpcaoResposta opcao) {
        if (opcao != null) {
            this.opcoes.add(opcao);
        }
    }
}