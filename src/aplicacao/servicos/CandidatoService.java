package aplicacao.servicos;

import aplicacao.SistemaContexto;
import dominio.admin.Perfil;
import dominio.candidato.Candidato;
import dominio.candidato.IndicadoresCadastrais;
import dominio.candidato.Partido;
import dominio.candidato.SituacaoCandidatura;
import dominio.candidato.SituacaoPartido;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.Pesquisa;
import infraestrutura.excecoes.RegraNegocioException;
import infraestrutura.utils.Validacao;
import java.util.List;
import persistencia.EstadoSistema;

/** Módulo Candidatos e Partidos. Todos consultam; somente administradores cadastram. */
public class CandidatoService extends ServicoBase {

    public CandidatoService(SistemaContexto contexto) {
        super(contexto);
    }

    // ------------------------------------------------------------------ consultas

    public List<Candidato> getBancoCandidatos() {
        return List.copyOf(ler().getCandidatos());
    }

    public List<Partido> getBancoPartidos() {
        return List.copyOf(ler().getPartidos());
    }

    public int proximoId() {
        return proximoId(getBancoCandidatos().stream().mapToInt(Candidato::getId));
    }

    /** Consulta combinando nome, cargo, partido, situação e pesquisa. Critério nulo não filtra. */
    public List<Candidato> consultarCandidatos(String nome, Cargo cargo, Partido partido,
            SituacaoCandidatura situacao, Pesquisa pesquisa) {
        return getBancoCandidatos().stream()
                .filter(c -> Validacao.chave(c.getNome()).contains(Validacao.chave(nome)))
                .filter(c -> cargo == null || c.getCargo() == cargo)
                .filter(c -> partido == null || c.pertenceAoPartido(partido.getId()))
                .filter(c -> situacao == null || c.getSituacao() == situacao)
                .filter(c -> pesquisa == null || pesquisa.possuiCandidato(c.getId()))
                .toList();
    }

    public IndicadoresCadastrais gerarIndicadores() {
        EstadoSistema estado = ler();
        return IndicadoresCadastrais.calcular(estado.getCandidatos(), estado.getPartidos());
    }

    // ------------------------------------------------------------------ partidos

    /** Cria o partido (id nulo) ou atualiza o existente. Sigla e número não podem se repetir. */
    public void salvarPartido(String id, String nome, String sigla, int numero, SituacaoPartido situacao) {
        alterar(Perfil.ADMINISTRADOR, estado -> {
            String siglaInformada = Validacao.texto(sigla, "Sigla");
            boolean repetido = estado.getPartidos().stream()
                    .anyMatch(p -> !p.getId().equals(id)
                            && (p.getNumero() == numero || p.getSigla().equalsIgnoreCase(siglaInformada)));
            Validacao.exigir(!repetido, "Já existe partido com este número ou sigla.");

            Partido existente = estado.buscarPartido(id).orElse(null);
            if (existente == null) {
                estado.getPartidos().add(new Partido(nome, sigla, numero, situacao));
                return;
            }
            if (existente.getSituacao() != situacao) {
                Validacao.exigir(!partidoTemCandidatoEmPesquisa(estado, existente),
                        "Desassocie das pesquisas os candidatos desse partido antes de alterar a situação.");
            }
            existente.setNome(nome);
            existente.setSigla(sigla);
            existente.setNumero(numero);
            existente.setSituacao(situacao);
        });
    }

    private static boolean partidoTemCandidatoEmPesquisa(EstadoSistema estado, Partido partido) {
        return estado.getPesquisas().stream().anyMatch(pesquisa -> pesquisa.getCandidatos().stream()
                .anyMatch(c -> c.pertenceAoPartido(partido.getId())));
    }

    /** Um partido com candidatos não pode ser excluído até que eles sejam desvinculados. */
    public void excluirPartido(String id) {
        alterar(Perfil.ADMINISTRADOR, estado -> {
            Validacao.exigir(estado.getCandidatos().stream().noneMatch(c -> c.pertenceAoPartido(id)),
                    "Desvincule os candidatos antes de excluir o partido.");
            Validacao.exigir(estado.getPartidos().removeIf(p -> p.getId().equals(id)), "Partido não encontrado.");
        });
    }

    // ------------------------------------------------------------------ candidatos

    /** Cria ou atualiza o candidato. O número é único por cargo e o partido é opcional. */
    public void salvarCandidato(int id, String nome, int numero, Cargo cargo, SituacaoCandidatura situacao,
            String idPartido) {
        alterar(Perfil.ADMINISTRADOR, estado -> {
            Validacao.exigir(!estado.candidatoEstaEmPesquisa(id),
                    "Desassocie o candidato das pesquisas antes de editar seu cadastro.");
            Validacao.exigir(estado.getCandidatos().stream()
                    .noneMatch(c -> c.getId() != id && c.getNumeroIdentificador() == numero && c.getCargo() == cargo),
                    "Número já utilizado por candidato do mesmo cargo.");
            Validacao.exigir(estado.getCandidatos().stream()
                    .noneMatch(c -> c.getId() != id && c.getNome().equalsIgnoreCase(nome.trim())),
                    "Use um nome fictício distinto para cada candidato.");

            Candidato candidato = new Candidato(id, nome, numero, cargo, situacao);
            if (idPartido != null) {
                candidato.setPartido(buscarPartidoPermitido(estado, idPartido, situacao));
            }
            estado.getCandidatos().removeIf(c -> c.getId() == id);
            estado.getCandidatos().add(candidato);
        });
    }

    private Partido buscarPartidoPermitido(EstadoSistema estado, String idPartido, SituacaoCandidatura situacao) {
        Partido partido = estado.buscarPartido(idPartido)
                .orElseThrow(() -> new RegraNegocioException("Partido não encontrado."));
        Validacao.exigir(partido.isAtivo() || situacao == SituacaoCandidatura.INATIVA,
                "Partido inativo não pode receber candidatura ativa.");
        return partido;
    }

    /** Excluir um candidato não exclui o seu partido. */
    public void excluirCandidato(int id) {
        alterar(Perfil.ADMINISTRADOR, estado -> {
            Validacao.exigir(!estado.candidatoEstaEmPesquisa(id),
                    "Candidato associado a pesquisa não pode ser excluído.");
            Validacao.exigir(estado.getCandidatos().removeIf(c -> c.getId() == id), "Candidato não encontrado.");
        });
    }
}
