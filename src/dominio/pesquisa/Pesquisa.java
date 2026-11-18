package dominio.pesquisa;

import dominio.candidato.Candidato;
import infraestrutura.utils.Validacao;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Pesquisa eleitoral simulada. É a raiz do agregado: perguntas, candidatos e plano amostral só
 * podem ser alterados enquanto a pesquisa está planejada.
 */
public class Pesquisa implements Serializable {

    private static final long serialVersionUID = 3L;

    private final String codigo;
    private String nome;
    private Cargo cargo;
    private String abrangencia;
    private LocalDate inicioColeta;
    private LocalDate fimColeta;
    private int quantidadePrevista;
    private int quantidadeRealizada;
    private TipoPesquisa tipo;
    private SituacaoPesquisa situacao = SituacaoPesquisa.PLANEJADA;
    private PlanoAmostral planoAmostral;
    private final Set<String> regioes = new LinkedHashSet<>();
    private final List<Pergunta> perguntas = new ArrayList<>();
    private final List<Candidato> candidatos = new ArrayList<>();

    private Pesquisa(Builder builder) {
        this.codigo = Validacao.texto(builder.codigo, "Código").toUpperCase(Locale.ROOT);
        configurar(builder.nome, builder.cargo, builder.inicio, builder.fim, builder.abrangencia,
                builder.meta, builder.tipo, builder.plano, builder.regioes);
    }

    // ------------------------------------------------------------------ configuração (planejamento)

    /** Lança exceção se a pesquisa já saiu da situação "planejada". */
    public void exigirPlanejada() {
        Validacao.exigir(situacao == SituacaoPesquisa.PLANEJADA,
                "A estrutura só pode ser alterada enquanto a pesquisa está planejada.");
    }

    public void configurar(String nome, Cargo cargo, LocalDate inicio, LocalDate fim, String abrangencia,
            int meta, TipoPesquisa tipo, PlanoAmostral plano, Collection<String> regioes) {
        exigirPlanejada();
        Validacao.exigir(cargo != null && tipo != null && plano != null,
                "Cargo, tipo e plano amostral são obrigatórios.");
        Validacao.exigir(inicio != null && fim != null && !fim.isBefore(inicio),
                "O período de coleta é inválido.");
        Validacao.exigir(regioes != null && !regioes.isEmpty(), "Selecione pelo menos uma região.");
        Validacao.exigir(candidatos.stream().allMatch(c -> c.getCargo() == cargo),
                "Desassocie os candidatos antes de alterar o cargo.");

        this.nome = Validacao.texto(nome, "Nome");
        this.cargo = cargo;
        this.inicioColeta = inicio;
        this.fimColeta = fim;
        this.abrangencia = Validacao.texto(abrangencia, "Abrangência");
        this.quantidadePrevista = Validacao.positivo(meta, "Entrevistas previstas");
        this.tipo = tipo;
        this.planoAmostral = plano;
        this.regioes.clear();
        this.regioes.addAll(regioes);
    }

    public void salvarPergunta(Pergunta pergunta) {
        exigirPlanejada();
        Validacao.exigir(pergunta != null, "Pergunta obrigatória.");
        boolean textoRepetido = perguntas.stream()
                .anyMatch(p -> !p.getId().equals(pergunta.getId())
                        && p.getTexto().equalsIgnoreCase(pergunta.getTexto()));
        Validacao.exigir(!textoRepetido, "Já existe uma pergunta com esse texto.");
        perguntas.removeIf(p -> p.getId().equals(pergunta.getId()));
        perguntas.add(pergunta);
    }

    public void removerPergunta(String idPergunta) {
        exigirPlanejada();
        Validacao.exigir(perguntas.removeIf(p -> p.getId().equals(idPergunta)), "Pergunta não encontrada.");
    }

    public void associarCandidato(Candidato candidato) {
        exigirPlanejada();
        Validacao.exigir(candidato != null && candidato.podeParticiparDePesquisa(),
                "Apenas candidaturas Candidato ou Validada de partido ativo podem participar.");
        Validacao.exigir(candidato.getCargo() == cargo, "Cargo incompatível com a pesquisa.");
        Validacao.exigir(!possuiCandidato(candidato.getId()), "Candidato já associado.");
        candidatos.add(candidato);
    }

    public void removerCandidato(int idCandidato) {
        exigirPlanejada();
        Validacao.exigir(candidatos.removeIf(c -> c.getId() == idCandidato), "Candidato não associado.");
    }

    public boolean possuiCandidato(int idCandidato) {
        return candidatos.stream().anyMatch(c -> c.getId() == idCandidato);
    }

    // ------------------------------------------------------------------ ciclo de vida

    /** Planejada → em coleta. Valida questionário e candidatos antes de liberar as entrevistas. */
    public void iniciarColeta() {
        exigirPlanejada();
        Validacao.exigir(!perguntas.isEmpty(), "Cadastre pelo menos uma pergunta.");
        for (Pergunta pergunta : perguntas) {
            Validacao.exigir(!pergunta.getTipo().possuiOpcoes() || !pergunta.getOpcoes().isEmpty(),
                    "Cadastre as opções de todas as perguntas de escolha.");
        }
        if (tipo.exigeCandidatos()) {
            validarCandidatosParaColeta();
        }
        situacao = SituacaoPesquisa.EM_COLETA;
    }

    private void validarCandidatosParaColeta() {
        Validacao.exigir(!candidatos.isEmpty(), "Associe pelo menos um candidato antes de iniciar a coleta.");
        Validacao.exigir(candidatos.stream().allMatch(Candidato::podeParticiparDePesquisa),
                "Há candidaturas sem situação permitida.");
        Validacao.exigir(perguntas.stream().anyMatch(this::listaTodosOsCandidatos),
                "Inclua uma pergunta de única escolha com os nomes de todos os candidatos associados.");
    }

    private boolean listaTodosOsCandidatos(Pergunta pergunta) {
        return pergunta.getTipo() == TipoPergunta.UNICA_ESCOLHA
                && candidatos.stream().allMatch(c -> pergunta.possuiOpcao(c.getNome()));
    }

    public void encerrarColeta() {
        Validacao.exigir(situacao == SituacaoPesquisa.EM_COLETA,
                "Somente uma pesquisa em coleta pode ser encerrada.");
        situacao = SituacaoPesquisa.ENCERRADA;
    }

    public void processarDados() {
        Validacao.exigir(situacao.permiteProcessamento(), "Encerre a coleta antes de processar.");
        situacao = SituacaoPesquisa.PROCESSADA;
    }

    /** Conta mais uma entrevista realizada, respeitando a situação e a quantidade prevista. */
    public void registrarEntrevista() {
        Validacao.exigir(situacao == SituacaoPesquisa.EM_COLETA, "A pesquisa não está em coleta.");
        Validacao.exigir(quantidadeRealizada < quantidadePrevista, "A meta total de entrevistas já foi atingida.");
        quantidadeRealizada++;
    }

    /** Altera a quantidade prevista (permitido no planejamento e durante a coleta). */
    public void alterarQuantidadePrevista(int novaQuantidade) {
        boolean permitido = situacao == SituacaoPesquisa.PLANEJADA || situacao == SituacaoPesquisa.EM_COLETA;
        Validacao.exigir(permitido, "Pesquisa encerrada não admite alteração de meta.");
        Validacao.exigir(novaQuantidade >= quantidadeRealizada,
                "A previsão não pode ser inferior à quantidade realizada.");
        quantidadePrevista = Validacao.positivo(novaQuantidade, "Quantidade prevista");
    }

    // ------------------------------------------------------------------ consultas

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public TipoPesquisa getTipo() {
        return tipo;
    }

    public SituacaoPesquisa getSituacao() {
        return situacao;
    }

    public LocalDate getInicioColeta() {
        return inicioColeta;
    }

    public LocalDate getFimColeta() {
        return fimColeta;
    }

    public String getAbrangencia() {
        return abrangencia;
    }

    public int getQuantidadePrevista() {
        return quantidadePrevista;
    }

    public int getQuantidadeRealizada() {
        return quantidadeRealizada;
    }

    public double getPercentualConclusao() {
        return 100.0 * quantidadeRealizada / quantidadePrevista;
    }

    public PlanoAmostral getPlanoAmostral() {
        return planoAmostral;
    }

    public Set<String> getRegioes() {
        return Collections.unmodifiableSet(regioes);
    }

    public List<Pergunta> getPerguntas() {
        return List.copyOf(perguntas);
    }

    public List<Candidato> getCandidatos() {
        return List.copyOf(candidatos);
    }

    @Override
    public String toString() {
        return codigo + " · " + nome;
    }

    // ------------------------------------------------------------------ Builder

    /** Padrão Builder: monta uma pesquisa válida passo a passo. */
    public static class Builder {

        private String codigo = UUID.randomUUID().toString();
        private String nome;
        private String abrangencia;
        private Cargo cargo;
        private LocalDate inicio;
        private LocalDate fim;
        private int meta;
        private TipoPesquisa tipo;
        private PlanoAmostral plano;
        private Collection<String> regioes = List.of();

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder cargo(Cargo cargo) {
            this.cargo = cargo;
            return this;
        }

        public Builder periodo(LocalDate inicio, LocalDate fim) {
            this.inicio = inicio;
            this.fim = fim;
            return this;
        }

        public Builder abrangencia(String abrangencia) {
            this.abrangencia = abrangencia;
            return this;
        }

        public Builder metaEntrevistados(int meta) {
            this.meta = meta;
            return this;
        }

        public Builder tipo(TipoPesquisa tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder planoAmostral(PlanoAmostral plano) {
            this.plano = plano;
            return this;
        }

        public Builder regioes(Collection<String> regioes) {
            this.regioes = regioes;
            return this;
        }

        public Pesquisa build() {
            return new Pesquisa(this);
        }
    }
}
