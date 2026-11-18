package dominio.base;

import infraestrutura.utils.Validacao;
import java.io.Serializable;

/**
 * Classe base de todas as pessoas do sistema (usuários, entrevistadores e participantes).
 * Concentra o identificador e o nome, evitando código repetido nas subclasses.
 */
public abstract class Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private String nome;

    protected Pessoa(int id, String nome) {
        this.id = Validacao.positivo(id, "Identificador");
        setNome(nome);
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.texto(nome, "Nome");
    }

    /** Texto que descreve a pessoa nas listas e relatórios. Cada subclasse decide o formato. */
    public abstract String descrever();

    @Override
    public String toString() {
        return descrever();
    }
}
