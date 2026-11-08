package dominio.admin;

import java.io.Serializable;

public class Parametro implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private TipoParametro tipo;
    private String valor;

    public Parametro(int id, TipoParametro tipo, String valor) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID deve ser maior que 0.");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("O tipo não pode ser nulo.");
        }

        this.id = id;
        this.tipo = tipo;
        setValor(valor);
    }

    public int getId() {
        return id;
    }

    public TipoParametro getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor não pode ser nulo ou vazio.");
        }

        this.valor = valor.trim();
    }
}