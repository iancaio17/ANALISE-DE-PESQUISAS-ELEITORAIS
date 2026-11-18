package dominio.pesquisa;

import infraestrutura.utils.Validacao;
import java.io.Serializable;
import java.util.UUID;

/** Uma opção de resposta de uma pergunta de escolha. */
public final class OpcaoResposta implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String id = UUID.randomUUID().toString();
    private final String texto;

    public OpcaoResposta(String texto) {
        this.texto = Validacao.texto(texto, "Opção");
    }

    public String getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
