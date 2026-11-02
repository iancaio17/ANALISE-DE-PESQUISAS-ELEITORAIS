package dominio.base;

import infraestrutura.excecoes.RegraNegocioException;
import java.io.Serializable;

public abstract class Pessoa implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;

    public Pessoa(int id, String nome) {
        this.id = id;
        setNome(nome);
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome não pode ser nulo ou vazio.");
        }
        this.nome = nome;
    }

    public abstract void exibirDetalhes();
}

