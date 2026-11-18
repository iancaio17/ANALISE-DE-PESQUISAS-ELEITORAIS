package aplicacao.servicos;

import aplicacao.SistemaContexto;
import dominio.admin.Perfil;
import dominio.admin.TipoParametro;
import dominio.base.PerfilDemografico;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.PlanoAmostral;
import dominio.pesquisa.SituacaoPesquisa;
import dominio.pesquisa.TipoPesquisa;
import infraestrutura.excecoes.RegraNegocioException;
import infraestrutura.utils.Validacao;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import persistencia.EstadoSistema;

/** Módulo Pesquisas: cadastro, questionário, candidatos, plano amostral e ciclo de coleta. */
public class PesquisaService extends ServicoBase {

    public PesquisaService(SistemaContexto contexto) {
        super(contexto);
    }

    // ------------------------------------------------------------------ consultas

    public List<Pesquisa> listarTodas() {
        return List.copyOf(ler().getPesquisas());
    }

    public Pesquisa buscarPesquisaPorCodigo(String codigo) {
        return ler().exigirPesquisa(codigo);
    }

    /** Consulta com critérios combinados, incluindo "apenas pesquisas com grupo abaixo da meta". */
    public List<Pesquisa> consultar(FiltroPesquisa filtro) {
        EstadoSistema estado = ler();
        return estado.getPesquisas().stream()
                .filter(filtro)
                .filter(p -> !filtro.isApenasAbaixoDaMeta() || temMetaPendente(estado, p))
                .toList();
    }

    /** Entrevistas concluídas de cada grupo do plano amostral (para comparar com a meta). */
    public Map<PerfilDemografico, Long> realizadasPorMeta(String codigo) {
        EstadoSistema estado = ler();
        return contarPorMeta(estado, estado.exigirPesquisa(codigo));
    }

    /** Regra de negócio: o grupo não atinge a meta se tiver menos entrevistas válidas que o mínimo. */
    public boolean temMetaPendente(String codigo) {
        EstadoSistema estado = ler();
        return temMetaPendente(estado, estado.exigirPesquisa(codigo));
    }

    private static boolean temMetaPendente(EstadoSistema estado, Pesquisa pesquisa) {
        Map<PerfilDemografico, Long> realizadas = contarPorMeta(estado, pesquisa);
        return pesquisa.getPlanoAmostral().getMetasPorPerfil().entrySet().stream()
                .anyMatch(meta -> realizadas.getOrDefault(meta.getKey(), 0L) < meta.getValue());
    }

    private static Map<PerfilDemografico, Long> contarPorMeta(EstadoSistema estado, Pesquisa pesquisa) {
        Map<PerfilDemografico, Long> contagem = new LinkedHashMap<>();
        for (PerfilDemografico grupo : pesquisa.getPlanoAmostral().getMetasPorPerfil().keySet()) {
            long quantidade = estado.entrevistasDaPesquisa(pesquisa.getCodigo()).stream()
                    .filter(e -> e.isConcluida() && grupo.corresponde(e.getParticipante().getPerfilDemografico()))
                    .count();
            contagem.put(grupo, quantidade);
        }
        return contagem;
    }

    // ------------------------------------------------------------------ cadastro

    /**
     * Cria (nova = true) ou atualiza uma pesquisa planejada.
     * Todas as regiões precisam estar cadastradas nos parâmetros do sistema.
     */
    public void salvarPesquisa(String codigo, boolean nova, String nome, Cargo cargo, LocalDate inicio,
            LocalDate fim, String abrangencia, int quantidadePrevista, TipoPesquisa tipo, String metodologia,
            List<String> regioes) {
        alterar(Perfil.PESQUISADOR, estado -> {
            validarRegioes(estado, regioes);
            if (nova) {
                criarPesquisa(estado, codigo, nome, cargo, inicio, fim, abrangencia, quantidadePrevista, tipo,
                        metodologia, regioes);
            } else {
                atualizarPesquisa(estado.exigirPesquisa(codigo), nome, cargo, inicio, fim, abrangencia,
                        quantidadePrevista, tipo, metodologia, regioes);
            }
        });
    }

    private void criarPesquisa(EstadoSistema estado, String codigo, String nome, Cargo cargo, LocalDate inicio,
            LocalDate fim, String abrangencia, int quantidadePrevista, TipoPesquisa tipo, String metodologia,
            List<String> regioes) {
        boolean codigoExistente = estado.getPesquisas().stream()
                .anyMatch(p -> p.getCodigo().equalsIgnoreCase(codigo.trim()));
        Validacao.exigir(!codigoExistente, "Código de pesquisa já cadastrado.");
        Pesquisa pesquisa = new Pesquisa.Builder()
                .codigo(codigo)
                .nome(nome)
                .cargo(cargo)
                .periodo(inicio, fim)
                .abrangencia(abrangencia)
                .metaEntrevistados(quantidadePrevista)
                .tipo(tipo)
                .planoAmostral(new PlanoAmostral(metodologia))
                .regioes(regioes)
                .build();
        estado.getPesquisas().add(pesquisa);
    }

    /** Mantém as metas já definidas, desde que as regiões delas continuem na pesquisa. */
    private void atualizarPesquisa(Pesquisa pesquisa, String nome, Cargo cargo, LocalDate inicio, LocalDate fim,
            String abrangencia, int quantidadePrevista, TipoPesquisa tipo, String metodologia,
            List<String> regioes) {
        PlanoAmostral novoPlano = new PlanoAmostral(metodologia);
        pesquisa.getPlanoAmostral().getMetasPorPerfil().forEach((grupo, meta) -> {
            Validacao.exigir(grupo.getRegiao().isBlank() || regioes.contains(grupo.getRegiao()),
                    "Há meta para uma região removida.");
            novoPlano.definirMetaPerfil(grupo, meta);
        });
        pesquisa.configurar(nome, cargo, inicio, fim, abrangencia, quantidadePrevista, tipo, novoPlano, regioes);
    }

    private static void validarRegioes(EstadoSistema estado, List<String> regioes) {
        Validacao.exigir(regioes != null && !regioes.isEmpty(), "Selecione as regiões.");
        for (String regiao : regioes) {
            boolean cadastrada = estado.getParametros().stream()
                    .anyMatch(p -> p.getTipo() == TipoParametro.REGIAO && p.getValor().equals(regiao));
            Validacao.exigir(cadastrada, "Região não cadastrada: " + regiao);
        }
    }

    public void excluirPesquisa(String codigo) {
        alterar(Perfil.PESQUISADOR, estado -> {
            Pesquisa pesquisa = estado.exigirPesquisa(codigo);
            pesquisa.exigirPlanejada();
            Validacao.exigir(estado.entrevistasDaPesquisa(pesquisa.getCodigo()).isEmpty(), "Pesquisa possui entrevistas.");
            estado.getPesquisas().remove(pesquisa);
            estado.getAnalises().remove(pesquisa.getCodigo());
        });
    }

    // ------------------------------------------------------------------ perguntas

    public void salvarPergunta(String codigo, Pergunta pergunta) {
        alterar(Perfil.PESQUISADOR, estado -> estado.exigirPesquisa(codigo).salvarPergunta(pergunta));
    }

    public void excluirPergunta(String codigo, String idPergunta) {
        alterar(Perfil.PESQUISADOR, estado -> estado.exigirPesquisa(codigo).removerPergunta(idPergunta));
    }

    // ------------------------------------------------------------------ candidatos

    public void associarCandidato(String codigo, int idCandidato) {
        alterar(Perfil.PESQUISADOR, estado -> {
            var candidato = estado.buscarCandidato(idCandidato)
                    .orElseThrow(() -> new RegraNegocioException("Candidato não encontrado."));
            estado.exigirPesquisa(codigo).associarCandidato(candidato);
        });
    }

    public void removerCandidato(String codigo, int idCandidato) {
        alterar(Perfil.PESQUISADOR, estado -> estado.exigirPesquisa(codigo).removerCandidato(idCandidato));
    }

    // ------------------------------------------------------------------ ciclo da coleta

    public void iniciarColeta(String codigo) {
        alterar(Perfil.PESQUISADOR, estado -> {
            Pesquisa pesquisa = estado.exigirPesquisa(codigo);
            Validacao.exigir(!LocalDate.now().isAfter(pesquisa.getFimColeta()),
                    "O período de coleta já terminou. Edite as datas.");
            pesquisa.iniciarColeta();
        });
    }

    public void encerrarColeta(String codigo) {
        alterar(Perfil.PESQUISADOR, estado -> estado.exigirPesquisa(codigo).encerrarColeta());
    }

    /** Altera a quantidade total prevista de entrevistas (planejamento ou coleta). */
    public void alterarMeta(String codigo, int quantidadePrevista) {
        alterar(Perfil.PESQUISADOR,
                estado -> estado.exigirPesquisa(codigo).alterarQuantidadePrevista(quantidadePrevista));
    }

    // ------------------------------------------------------------------ metas por grupo

    public void salvarMeta(String codigo, PerfilDemografico grupo, int meta) {
        alterar(Perfil.PESQUISADOR, estado -> {
            Pesquisa pesquisa = estado.exigirPesquisa(codigo);
            pesquisa.exigirPlanejada();
            ValidadorPerfil.validar(estado, grupo, true);
            Validacao.exigir(grupo.getRegiao().isBlank() || pesquisa.getRegioes().contains(grupo.getRegiao()),
                    "A região da meta deve estar na pesquisa.");
            Validacao.exigir(meta <= pesquisa.getQuantidadePrevista(),
                    "Meta do grupo não pode ultrapassar a previsão total.");
            pesquisa.getPlanoAmostral().definirMetaPerfil(grupo, meta);
        });
    }

    public void removerMeta(String codigo, PerfilDemografico grupo) {
        alterar(Perfil.PESQUISADOR, estado -> {
            Pesquisa pesquisa = estado.exigirPesquisa(codigo);
            pesquisa.exigirPlanejada();
            pesquisa.getPlanoAmostral().removerMeta(grupo);
        });
    }

    /** Pesquisas em coleta, que podem receber entrevistas. */
    public List<Pesquisa> listarEmColeta() {
        return listarTodas().stream().filter(p -> p.getSituacao() == SituacaoPesquisa.EM_COLETA).toList();
    }
}
