package dominio.coleta;

import dominio.pesquisa.Pergunta;

public class Resposta {
    private Pergunta pergunta;
    private String opcaoEscolhida;

    public Resposta(Pergunta pergunta, String opcaoEscolhida) {
        if (pergunta == null) {
            throw new IllegalArgumentException("A resposta deve estar vinculada a uma pergunta.");
        }
        if (opcaoEscolhida == null || opcaoEscolhida.trim().isEmpty()) {
            throw new IllegalArgumentException("A opção escolhida não pode ser vazia.");
        }
        this.pergunta = pergunta;
        this.opcaoEscolhida = opcaoEscolhida;
    }

    public Pergunta getPergunta() { return pergunta; }
    public String getOpcaoEscolhida() { return opcaoEscolhida; }
}