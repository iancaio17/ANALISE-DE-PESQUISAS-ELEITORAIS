package dominio.pesquisa;

/** Formatos de pergunta de um questionário. */
public enum TipoPergunta {
    UNICA_ESCOLHA("Única escolha"),
    MULTIPLA_ESCOLHA("Múltipla escolha"),
    DISCURSIVA("Discursiva");

    private final String texto;

    TipoPergunta(String texto) {
        this.texto = texto;
    }

    /** Perguntas de escolha precisam de opções de resposta cadastradas. */
    public boolean possuiOpcoes() {
        return this != DISCURSIVA;
    }

    @Override
    public String toString() {
        return texto;
    }
}
