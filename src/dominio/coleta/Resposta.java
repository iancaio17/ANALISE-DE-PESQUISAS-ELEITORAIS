package dominio.coleta;

import dominio.pesquisa.Pergunta;
import infraestrutura.excecoes.RegraNegocioException;
import java.io.Serializable;

public class Resposta implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String NAO_SABE = "Não sabe";
    public static final String NAO_RESPONDEU = "Não respondeu";
    public static final String NENHUMA_DAS_OPCOES = "Nenhuma das opções";

    private Pergunta pergunta;
    private String opcaoEscolhida;

    public Resposta(Pergunta pergunta, String opcaoEscolhida) {
        if (pergunta == null) {
            throw new RegraNegocioException("A resposta deve estar vinculada a uma pergunta.");
        }
        if (opcaoEscolhida == null || opcaoEscolhida.trim().isEmpty()) {
            throw new RegraNegocioException("A opção escolhida não pode ser vazia.");
        }
        this.pergunta = pergunta;
        this.opcaoEscolhida = opcaoEscolhida.trim();
    }

    public Pergunta getPergunta() { return pergunta; }
    public String getOpcaoEscolhida() { return opcaoEscolhida; }

    public boolean isRespostaEspecial() {
        return opcaoEscolhida.equalsIgnoreCase(NAO_SABE) ||
                opcaoEscolhida.equalsIgnoreCase(NAO_RESPONDEU) ||
                opcaoEscolhida.equalsIgnoreCase(NENHUMA_DAS_OPCOES);
    }
}