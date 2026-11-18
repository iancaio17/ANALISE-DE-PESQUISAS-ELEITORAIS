package dominio.candidato;

import infraestrutura.utils.Validacao;
import java.io.Serializable;
import java.util.Locale;
import java.util.UUID;

/** Partido político fictício. Sigla e número são únicos (verificado pelo serviço). */
public class Partido implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String id = UUID.randomUUID().toString();
    private String nome;
    private String sigla;
    private int numero;
    private SituacaoPartido situacao;

    public Partido(String nome, String sigla, int numero, SituacaoPartido situacao) {
        setNome(nome);
        setSigla(sigla);
        setNumero(numero);
        setSituacao(situacao);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public int getNumero() {
        return numero;
    }

    public SituacaoPartido getSituacao() {
        return situacao;
    }

    public boolean isAtivo() {
        return situacao == SituacaoPartido.ATIVO;
    }

    public void setNome(String nome) {
        this.nome = Validacao.texto(nome, "Nome do partido");
    }

    public void setSigla(String sigla) {
        this.sigla = Validacao.texto(sigla, "Sigla").toUpperCase(Locale.ROOT);
    }

    public void setNumero(int numero) {
        this.numero = Validacao.positivo(numero, "Número do partido");
    }

    public void setSituacao(SituacaoPartido situacao) {
        Validacao.exigir(situacao != null, "Situação do partido inválida.");
        this.situacao = situacao;
    }

    @Override
    public String toString() {
        return sigla + " · " + nome;
    }
}
