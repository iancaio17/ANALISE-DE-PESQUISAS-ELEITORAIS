package dominio.coleta;

import dominio.base.Pessoa;

/** Entrevistador fictício responsável por aplicar as entrevistas. */
public class Entrevistador extends Pessoa {
    private static final long serialVersionUID = 1L;

    private static final long serialVersionUID = 1L;

    public Entrevistador(int id, String nome) {
        super(id, nome);
    }

    @Override
    public String descrever() {
        return "E" + getId() + " · " + getNome();
    }
}
