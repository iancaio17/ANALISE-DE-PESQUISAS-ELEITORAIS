package dominio.admin;

import infraestrutura.utils.Validacao;
import java.io.Serializable;

/** Categoria geral usada nas pesquisas (ex.: região "Norte" ou faixa etária "16-34"). */
public class Parametro implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final TipoParametro tipo;
    private final String valor;

    public Parametro(int id, TipoParametro tipo, String valor) {
        Validacao.exigir(tipo != null, "O tipo do parâmetro é obrigatório.");
        this.id = Validacao.positivo(id, "Identificador do parâmetro");
        this.tipo = tipo;
        this.valor = Validacao.texto(valor, "Valor do parâmetro");
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
}
