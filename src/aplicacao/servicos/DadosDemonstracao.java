package aplicacao.servicos;

import aplicacao.SistemaContexto;
import dominio.admin.Parametro;
import dominio.admin.Perfil;
import dominio.admin.TipoParametro;
import dominio.admin.Usuario;
import dominio.base.PerfilDemografico;
import dominio.candidato.Candidato;
import dominio.candidato.Partido;
import dominio.candidato.SituacaoCandidatura;
import dominio.candidato.SituacaoPartido;
import dominio.coleta.Entrevista;
import dominio.coleta.Entrevistador;
import dominio.coleta.Participante;
import dominio.coleta.Resposta;
import dominio.coleta.RespostaEspecial;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.OpcaoResposta;
import dominio.pesquisa.Pergunta;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.PlanoAmostral;
import dominio.pesquisa.TipoPergunta;
import dominio.pesquisa.TipoPesquisa;
import infraestrutura.utils.Validacao;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import persistencia.EstadoSistema;

/** Massa de dados opcional e explicitamente fictícia, criada em uma única transação. */
public final class DadosDemonstracao {

    private static final List<String> NOMES_DOS_CANDIDATOS = List.of("Alfa", "Beta", "Gama");

    private DadosDemonstracao() {
    }

    /** Cria usuários, partido, candidatos, quatro pesquisas e entrevistas fictícias (só o administrador). */
    public static void carregar(SistemaContexto contexto) {
        contexto.getRepositorio().alterar(estado -> {
            contexto.exigirAcesso(estado, Perfil.ADMINISTRADOR);
            Validacao.exigir(!estado.isDemonstracaoCarregada() && estado.getPesquisas().isEmpty()
                    && estado.getCandidatos().isEmpty() && estado.getPartidos().isEmpty(),
                    "A demonstração só pode ser carregada uma vez, antes dos cadastros de pesquisas, "
                    + "candidatos e partidos.");
            criarUsuarios(estado);
            Partido partido = new Partido("Partido fictício Horizonte", "PFH", 11, SituacaoPartido.ATIVO);
            estado.getPartidos().add(partido);
            criarCandidatos(estado, partido);
            criarPesquisasComEntrevistas(estado);
            estado.setDemonstracaoCarregada(true);
        });
        contexto.getAnalise().processarPesquisa("P002");
    }

    private static void criarUsuarios(EstadoSistema estado) {
        adicionarUsuario(estado, "Pesquisador fictício", "pesquisador@simulado.local", "Pesquisa123", Perfil.PESQUISADOR);
        adicionarUsuario(estado, "Analista fictício", "analista@simulado.local", "Analise123", Perfil.ANALISTA);
    }

    private static void adicionarUsuario(EstadoSistema estado, String nome, String email, String senha, Perfil perfil) {
        if (estado.buscarUsuarioPorEmail(email).isPresent()) {
            return;
        }
        int id = estado.getUsuarios().stream().mapToInt(Usuario::getId).max().orElse(0) + 1;
        estado.getUsuarios().add(new Usuario(id, nome, email, senha, Set.of(perfil)));
    }

    private static void criarCandidatos(EstadoSistema estado, Partido partido) {
        for (int i = 0; i < NOMES_DOS_CANDIDATOS.size(); i++) {
            Candidato candidato = new Candidato(i + 1, "Candidato " + NOMES_DOS_CANDIDATOS.get(i), (i + 1) * 11,
                    Cargo.GOVERNADOR, SituacaoCandidatura.VALIDADA);
            candidato.setPartido(partido);
            estado.getCandidatos().add(candidato);
        }
    }

    /**
     * P001 e P002 ficam encerradas, P003 em coleta e P004 planejada.
     * Cada pesquisa tem uma pergunta de única escolha com os nomes dos candidatos.
     */
    private static void criarPesquisasComEntrevistas(EstadoSistema estado) {
        Entrevistador entrevistador = new Entrevistador(proximoIdEntrevistador(estado), "Entrevistador fictício");
        estado.getEntrevistadores().add(entrevistador);
        int idParticipante = estado.getParticipantes().stream().mapToInt(Participante::getId).max().orElse(0) + 1;
        List<String> regioes = valoresDe(estado, TipoParametro.REGIAO);

        for (int numero = 1; numero <= 4; numero++) {
            Pesquisa pesquisa = criarPesquisa(estado, numero, regioes);
            Pergunta pergunta = criarPergunta(estado, pesquisa);
            estado.getPesquisas().add(pesquisa);
            if (numero == 4) {
                continue;
            }
            pesquisa.iniciarColeta();
            int quantidade = numero == 1 ? 6 : numero == 2 ? 8 : 2;
            for (int k = 0; k < quantidade; k++) {
                Participante participante = criarParticipante(estado, idParticipante++, numero, k, regioes);
                registrarEntrevista(estado, pesquisa, pergunta, participante, entrevistador, k, numero);
            }
            if (numero <= 2) {
                pesquisa.encerrarColeta();
            }
        }
    }

    private static Pesquisa criarPesquisa(EstadoSistema estado, int numero, List<String> regioes) {
        Pesquisa pesquisa = new Pesquisa.Builder()
                .codigo("P00" + numero)
                .nome("Pesquisa fictícia " + numero)
                .cargo(Cargo.GOVERNADOR)
                .tipo(TipoPesquisa.INTENCAO_VOTO)
                .periodo(LocalDate.now().minusDays(7), LocalDate.now().plusDays(30))
                .abrangencia("Regiões simuladas")
                .metaEntrevistados(10)
                .regioes(regioes)
                .planoAmostral(new PlanoAmostral("Amostragem didática por cotas"))
                .build();
        for (Candidato candidato : estado.getCandidatos()) {
            pesquisa.associarCandidato(candidato);
        }
        pesquisa.getPlanoAmostral().definirMetaPerfil(new PerfilDemografico("", regioes.get(0), "", "", ""), 4);
        return pesquisa;
    }

    private static Pergunta criarPergunta(EstadoSistema estado, Pesquisa pesquisa) {
        Pergunta pergunta = new Pergunta("Em quem votaria para governador?", TipoPergunta.UNICA_ESCOLHA);
        for (Candidato candidato : estado.getCandidatos()) {
            pergunta.adicionarOpcao(new OpcaoResposta(candidato.getNome()));
        }
        pesquisa.salvarPergunta(pergunta);
        return pergunta;
    }

    private static Participante criarParticipante(EstadoSistema estado, int id, int pesquisa, int indice,
            List<String> regioes) {
        String faixaEtaria = valoresDe(estado, TipoParametro.FAIXA_ETARIA).get(0);
        String escolaridade = valoresDe(estado, TipoParametro.ESCOLARIDADE).get(0);
        String renda = valoresDe(estado, TipoParametro.FAIXA_RENDA).get(0);
        String genero = indice % 2 == 0 ? "Feminino" : "Masculino";
        PerfilDemografico perfil = new PerfilDemografico(faixaEtaria, regioes.get(indice % regioes.size()),
                escolaridade, renda, genero);
        Participante participante = new Participante(id, "Participante fictício " + pesquisa + "-" + (indice + 1), perfil);
        estado.getParticipantes().add(participante);
        return participante;
    }

    /** As respostas alternam entre os candidatos; as três últimas posições usam respostas especiais. */
    private static void registrarEntrevista(EstadoSistema estado, Pesquisa pesquisa, Pergunta pergunta,
            Participante participante, Entrevistador entrevistador, int indice, int numeroDaPesquisa) {
        String opcao;
        if (indice == 4) {
            opcao = RespostaEspecial.NAO_SABE.getTexto();
        } else if (indice == 5) {
            opcao = RespostaEspecial.NAO_RESPONDEU.getTexto();
        } else if (indice == 6) {
            opcao = RespostaEspecial.NENHUMA_DAS_OPCOES.getTexto();
        } else {
            opcao = estado.getCandidatos().get((indice + numeroDaPesquisa) % 3).getNome();
        }
        Entrevista entrevista = new Entrevista(indice + 1, pesquisa, participante, entrevistador);
        entrevista.registrarResposta(new Resposta(pergunta, opcao));
        entrevista.concluirEntrevista();
        pesquisa.registrarEntrevista();
        estado.getEntrevistas().add(entrevista);
    }

    private static int proximoIdEntrevistador(EstadoSistema estado) {
        return estado.getEntrevistadores().stream().mapToInt(Entrevistador::getId).max().orElse(0) + 1;
    }

    private static List<String> valoresDe(EstadoSistema estado, TipoParametro tipo) {
        return estado.getParametros().stream()
                .filter(p -> p.getTipo() == tipo)
                .map(Parametro::getValor)
                .toList();
    }
}
