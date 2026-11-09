package dominio.pesquisa;

import infraestrutura.excecoes.PesquisaRegraNegocioException;
import dominio.candidato.Candidato; // Importação baseada na sua estrutura de pastas
import java.time.LocalDate;
import java.util.*;

public class Pesquisa {
    private final String codigo;
    private String nome;
    private Cargo cargo;
    private LocalDate inicioColeta;
    private LocalDate fimColeta;
    private String abrangencia;
    private int quantidadePrevistaEntrevistados;
    private TipoPesquisa tipo;
    private SituacaoPesquisa situacao;
    private PlanoAmostral planoAmostral;

    private final List<Pergunta> perguntas = new ArrayList<>();
    private final Set<Candidato> candidatos = new HashSet<>();
    private int quantidadeRealizada = 0;

    private Pesquisa(Builder builder) {
        this.codigo = builder.codigo;
        this.nome = builder.nome;
        this.cargo = builder.cargo;
        this.inicioColeta = builder.inicioColeta;
        this.fimColeta = builder.fimColeta;
        this.abrangencia = builder.abrangencia;
        this.quantidadePrevistaEntrevistados = builder.quantidadePrevistaEntrevistados;
        this.tipo = builder.tipo;
        this.planoAmostral = builder.planoAmostral;
        this.situacao = SituacaoPesquisa.PLANEJADA;
    }

    public void associarCandidato(Candidato candidato) {
        if (!candidato.getCargo().equals(this.cargo)) {
            throw new PesquisaRegraNegocioException("O cargo do candidato é incompatível com a pesquisa.");
        }
        if (!candidato.isSituacaoPermitidaParaPesquisa()) {
            throw new PesquisaRegraNegocioException("A candidatura não está em situação permitida para pesquisas.");
        }
        if (this.situacao == SituacaoPesquisa.PROCESSADA) {
            throw new PesquisaRegraNegocioException("Não é possível adicionar candidatos a uma pesquisa já processada.");
        }
        this.candidatos.add(candidato);
    }

    public void registrarEntrevista() {
        if (this.situacao != SituacaoPesquisa.EM_COLETA) {
            throw new PesquisaRegraNegocioException("As entrevistas só podem ser registadas em pesquisas 'Em Coleta'.");
        }
        this.quantidadeRealizada++;
    }

    public void iniciarColeta() {
        if (this.tipo == TipoPesquisa.INTENCAO_VOTO && this.candidatos.isEmpty()) {
            throw new PesquisaRegraNegocioException("As pesquisas de intenção de voto requerem pelo menos um candidato associado.");
        }
        this.situacao = this.situacao.iniciar();
    }

    public void encerrarColeta() {
        this.situacao = this.situacao.encerrar();
    }

    public void processarDados() {
        this.situacao = this.situacao.processar();
    }

    public double getPercentualConclusao() {
        if (quantidadePrevistaEntrevistados == 0) return 0.0;
        return ((double) quantidadeRealizada / quantidadePrevistaEntrevistados) * 100;
    }

    public String getCodigo() { return codigo; }
    public SituacaoPesquisa getSituacao() { return situacao; }

    public static class Builder {
        private String codigo = UUID.randomUUID().toString();
        private String nome;
        private Cargo cargo;
        private LocalDate inicioColeta;
        private LocalDate fimColeta;
        private String abrangencia;
        private int quantidadePrevistaEntrevistados;
        private TipoPesquisa tipo;
        private PlanoAmostral planoAmostral;

        public Builder nome(String nome) { this.nome = nome; return this; }
        public Builder cargo(Cargo cargo) { this.cargo = cargo; return this; }
        public Builder periodo(LocalDate inicio, LocalDate fim) {
            this.inicioColeta = inicio;
            this.fimColeta = fim;
            return this;
        }
        public Builder abrangencia(String abrangencia) { this.abrangencia = abrangencia; return this; }
        public Builder metaEntrevistados(int qtd) { this.quantidadePrevistaEntrevistados = qtd; return this; }
        public Builder tipo(TipoPesquisa tipo) { this.tipo = tipo; return this; }
        public Builder planoAmostral(PlanoAmostral plano) { this.planoAmostral = plano; return this; }

        public Pesquisa build() {
            if (nome == null || tipo == null || cargo == null) {
                throw new PesquisaRegraNegocioException("Campos obrigatórios ausentes na criação da Pesquisa.");
            }
            return new Pesquisa(this);
        }
    }
}

enum Cargo { PREFEITO, VEREADOR, GOVERNADOR, PRESIDENTE }
enum TipoPesquisa { INTENCAO_VOTO, REJEICAO, APROVACAO_GOVERNO }

enum SituacaoPesquisa {
    PLANEJADA {
        @Override public SituacaoPesquisa iniciar() { return EM_COLETA; }
        @Override public SituacaoPesquisa encerrar() { throw new PesquisaRegraNegocioException("Uma pesquisa planeada não pode ser encerrada sem iniciar."); }
        @Override public SituacaoPesquisa processar() { throw new PesquisaRegraNegocioException("Apenas pesquisas encerradas podem ser processadas."); }
    },
    EM_COLETA {
        @Override public SituacaoPesquisa iniciar() { throw new PesquisaRegraNegocioException("A pesquisa já está em coleta."); }
        @Override public SituacaoPesquisa encerrar() { return ENCERRADA; }
        @Override public SituacaoPesquisa processar() { throw new PesquisaRegraNegocioException("Não é possível processar uma pesquisa ainda em coleta."); }
    },
    ENCERRADA {
        @Override public SituacaoPesquisa iniciar() { throw new PesquisaRegraNegocioException("Uma pesquisa encerrada não pode ser reaberta."); }
        @Override public SituacaoPesquisa encerrar() { throw new PesquisaRegraNegocioException("A pesquisa já está encerrada."); }
        @Override public SituacaoPesquisa processar() { return PROCESSADA; }
    },
    PROCESSADA {
        @Override public SituacaoPesquisa iniciar() { throw new PesquisaRegraNegocioException("Uma pesquisa processada não pode ser reaberta."); }
        @Override public SituacaoPesquisa encerrar() { throw new PesquisaRegraNegocioException("Uma pesquisa processada já está encerrada."); }
        @Override public SituacaoPesquisa processar() { return PROCESSADA; }
    };

    public abstract SituacaoPesquisa iniciar();
    public abstract SituacaoPesquisa encerrar();
    public abstract SituacaoPesquisa processar();
}