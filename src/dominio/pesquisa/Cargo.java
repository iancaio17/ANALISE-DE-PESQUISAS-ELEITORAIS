package dominio.pesquisa;

import infraestrutura.excecoes.RegraNegocioException;

/** Cargos eletivos usados em pesquisas e candidaturas (todos fictícios neste projeto). */
public enum Cargo {
    PRESIDENTE("Presidente"),
    GOVERNADOR("Governador"),
    SENADOR("Senador"),
    DEPUTADO_FEDERAL("Deputado federal"),
    DEPUTADO_ESTADUAL("Deputado estadual"),
    DEPUTADO_DISTRITAL("Deputado distrital"),
    PREFEITO("Prefeito"),
    VEREADOR("Vereador");

    private final String texto;

    Cargo(String texto) {
        this.texto = texto;
    }

    /** Converte o nome do enum ou o texto exibido no cargo correspondente. */
    public static Cargo de(String valor) {
        for (Cargo cargo : values()) {
            if (cargo.name().equalsIgnoreCase(valor) || cargo.texto.equalsIgnoreCase(valor)) {
                return cargo;
            }
        }
        throw new RegraNegocioException("Cargo inválido.");
    }

    @Override
    public String toString() {
        return texto;
    }
}
