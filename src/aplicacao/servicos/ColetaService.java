package aplicacao.servicos;

import aplicacao.SistemaContexto;
import dominio.admin.Perfil;
import dominio.base.PerfilDemografico;
import dominio.coleta.Entrevista;
import dominio.coleta.Entrevistador;
import dominio.coleta.Participante;
import dominio.coleta.Resposta;
import dominio.coleta.RespostaEspecial;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import infraestrutura.excecoes.RegraNegocioException;
import infraestrutura.utils.Validacao;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import persistencia.EstadoSistema;

/** Módulo Coleta e Amostra: entrevistadores, participantes, entrevistas e respostas. */
public class ColetaService extends ServicoBase {

    public ColetaService(SistemaContexto contexto) {
        super(contexto);
    }

    // ------------------------------------------------------------------ consultas

    public List<Entrevistador> listarEntrevistadores() {
        return List.copyOf(ler(Perfil.PESQUISADOR).getEntrevistadores());
    }

    public List<Participante> listarParticipantes() {
        return List.copyOf(ler(Perfil.PESQUISADOR).getParticipantes());
    }

    /** Pesquisadores e analistas podem consultar as entrevistas coletadas. */
    public List<Entrevista> getBancoEntrevistas() {
        return List.copyOf(ler(Perfil.PESQUISADOR, Perfil.ANALISTA).getEntrevistas());
    }

    public List<Entrevista> getEntrevistasPorPesquisa(String codigo) {
        return getBancoEntrevistas().stream()
                .filter(e -> e.getPesquisa().getCodigo().equalsIgnoreCase(codigo))
                .toList();
    }

    /** Filtra as entrevistas por pesquisa (opcional) e por características da amostra. */
    public List<Entrevista> consultarEntrevistas(String codigoPesquisa, PerfilDemografico filtro) {
        return getBancoEntrevistas().stream()
                .filter(e -> codigoPesquisa == null || e.getPesquisa().getCodigo().equals(codigoPesquisa))
                .filter(e -> filtro.corresponde(e.getParticipante().getPerfilDemografico()))
                .toList();
    }

    /** Participantes que ainda não têm entrevista e moram em uma região da pesquisa. */
    public List<Participante> participantesDisponiveis(String codigoPesquisa) {
        EstadoSistema estado = ler(Perfil.PESQUISADOR);
        Pesquisa pesquisa = estado.exigirPesquisa(codigoPesquisa);
        Set<Integer> comEntrevista = estado.getEntrevistas().stream()
                .map(e -> e.getParticipante().getId())
                .collect(Collectors.toSet());
        return estado.getParticipantes().stream()
                .filter(p -> !comEntrevista.contains(p.getId()))
                .filter(p -> pesquisa.getRegioes().contains(p.getPerfilDemografico().getRegiao()))
                .toList();
    }

    /** Próximo número de entrevista livre dentro da pesquisa. */
    public int proximoNumero(String codigo) {
        return proximoId(getEntrevistasPorPesquisa(codigo).stream().mapToInt(Entrevista::getIdUnico));
    }

    public int proximoIdEntrevistador() {
        return proximoId(listarEntrevistadores().stream().mapToInt(Entrevistador::getId));
    }

    public int proximoIdParticipante() {
        return proximoId(listarParticipantes().stream().mapToInt(Participante::getId));
    }

    // ------------------------------------------------------------------ entrevistadores

    public void salvarEntrevistador(int id, String nome) {
        alterar(Perfil.PESQUISADOR, estado -> {
            Validacao.exigir(estado.getEntrevistas().stream().noneMatch(e -> e.getEntrevistador().getId() == id),
                    "Entrevistador já vinculado a entrevista não pode ser alterado.");
            estado.getEntrevistadores().removeIf(e -> e.getId() == id);
            estado.getEntrevistadores().add(new Entrevistador(id, nome));
        });
    }

    public void excluirEntrevistador(int id) {
        alterar(Perfil.PESQUISADOR, estado -> {
            Validacao.exigir(estado.getEntrevistas().stream().noneMatch(e -> e.getEntrevistador().getId() == id),
                    "Entrevistador possui entrevistas.");
            Validacao.exigir(estado.getEntrevistadores().removeIf(e -> e.getId() == id),
                    "Entrevistador não encontrado.");
        });
    }

    // ------------------------------------------------------------------ participantes

    public void salvarParticipante(int id, String nome, PerfilDemografico perfil) {
        alterar(Perfil.PESQUISADOR, estado -> {
            ValidadorPerfil.validar(estado, perfil, false);
            Validacao.exigir(estado.getEntrevistas().stream().noneMatch(e -> e.getParticipante().getId() == id),
                    "Participante com entrevista concluída não pode ser alterado.");
            estado.getParticipantes().removeIf(p -> p.getId() == id);
            estado.getParticipantes().add(new Participante(id, nome, perfil));
        });
    }

    public void excluirParticipante(int id) {
        alterar(Perfil.PESQUISADOR, estado -> {
            Validacao.exigir(estado.getEntrevistas().stream().noneMatch(e -> e.getParticipante().getId() == id),
                    "Participante possui entrevista concluída.");
            Validacao.exigir(estado.getParticipantes().removeIf(p -> p.getId() == id),
                    "Participante não encontrado.");
        });
    }

    // ------------------------------------------------------------------ entrevistas

    /**
     * Registra uma entrevista já concluída. Tudo é validado antes de gravar: se algo falhar,
     * nada é salvo.
     *
     * @param respostas opções escolhidas, por identificador de pergunta
     */
    public void registrarEntrevista(String codigo, int numero, int idParticipante, int idEntrevistador,
            Map<String, List<String>> respostas) {
        alterar(Perfil.PESQUISADOR, estado -> {
            Pesquisa pesquisa = estado.exigirPesquisa(codigo);
            validarPesquisaAceitaEntrevista(estado, pesquisa, numero);

            Participante participante = estado.buscarParticipante(idParticipante)
                    .orElseThrow(() -> new RegraNegocioException("Participante não cadastrado."));
            Entrevistador entrevistador = estado.buscarEntrevistador(idEntrevistador)
                    .orElseThrow(() -> new RegraNegocioException("Entrevistador não cadastrado."));
            validarParticipante(estado, pesquisa, participante);

            Entrevista entrevista = new Entrevista(numero, pesquisa, participante, entrevistador);
            registrarRespostas(entrevista, pesquisa, respostas);
            entrevista.concluirEntrevista();
            pesquisa.registrarEntrevista();
            estado.getEntrevistas().add(entrevista);
        });
    }

    private void validarPesquisaAceitaEntrevista(EstadoSistema estado, Pesquisa pesquisa, int numero) {
        Validacao.exigir(pesquisa.getSituacao() == SituacaoPesquisa.EM_COLETA, "Esta pesquisa não está em coleta.");
        LocalDate hoje = LocalDate.now();
        boolean dentroDoPeriodo = !hoje.isBefore(pesquisa.getInicioColeta()) && !hoje.isAfter(pesquisa.getFimColeta());
        Validacao.exigir(dentroDoPeriodo, "A entrevista precisa ocorrer dentro do período de coleta.");
        boolean numeroRepetido = estado.entrevistasDaPesquisa(pesquisa.getCodigo()).stream()
                .anyMatch(e -> e.getIdUnico() == numero);
        Validacao.exigir(!numeroRepetido, "Número de entrevista já registrado nesta pesquisa.");
    }

    private void validarParticipante(EstadoSistema estado, Pesquisa pesquisa, Participante participante) {
        boolean jaEntrevistado = estado.getEntrevistas().stream()
                .anyMatch(e -> e.getParticipante().getId() == participante.getId());
        Validacao.exigir(!jaEntrevistado, "Este participante já possui uma entrevista. Cadastre outro participante fictício.");
        ValidadorPerfil.validar(estado, participante.getPerfilDemografico(), false);
        Validacao.exigir(pesquisa.getRegioes().contains(participante.getPerfilDemografico().getRegiao()),
                "O participante não pertence às regiões da pesquisa.");
    }

    /** Cria uma resposta para cada pergunta; pergunta opcional sem resposta vira "Não respondeu". */
    private void registrarRespostas(Entrevista entrevista, Pesquisa pesquisa, Map<String, List<String>> respostas) {
        Validacao.exigir(respostas != null && respostas.keySet().stream()
                .allMatch(id -> pesquisa.getPerguntas().stream().anyMatch(q -> q.getId().equals(id))),
                "Há resposta de pergunta que não pertence à pesquisa.");
        for (Pergunta pergunta : pesquisa.getPerguntas()) {
            List<String> opcoes = respostas.get(pergunta.getId());
            if (opcoes == null || opcoes.isEmpty()) {
                Validacao.exigir(!pergunta.isObrigatoria(), "Responda a pergunta obrigatória: " + pergunta.getTexto());
                opcoes = List.of(RespostaEspecial.NAO_RESPONDEU.getTexto());
            }
            entrevista.registrarResposta(new Resposta(pergunta, opcoes));
        }
    }
}
