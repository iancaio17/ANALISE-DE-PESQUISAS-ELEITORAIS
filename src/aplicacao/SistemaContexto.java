package aplicacao;

import aplicacao.servicos.AdministracaoService;
import aplicacao.servicos.AnaliseService;
import aplicacao.servicos.CandidatoService;
import aplicacao.servicos.ColetaService;
import aplicacao.servicos.PesquisaService;
import dominio.admin.Parametro;
import dominio.admin.Perfil;
import dominio.admin.TipoParametro;
import dominio.admin.Usuario;
import infraestrutura.excecoes.AcessoNegadoException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import persistencia.EstadoSistema;
import persistencia.SistemaRepository;

/**
 * Ponto de composição da aplicação: cria o repositório, os cinco serviços (um por módulo) e controla
 * quem é o usuário logado (sessão).
 */
public final class SistemaContexto implements AutoCloseable {

    private static final String EMAIL_ADMINISTRADOR_INICIAL = "admin@simulado.local";
    private static final String SENHA_ADMINISTRADOR_INICIAL = "Admin12345";

    private final SistemaRepository repositorio;
    private Integer idUsuarioLogado;

    private final AdministracaoService administracao;
    private final PesquisaService pesquisas;
    private final ColetaService coleta;
    private final CandidatoService candidatos;
    private final AnaliseService analise;

    /** Usa a pasta "dados" (ou a indicada em -Dsape.dados) para guardar o arquivo do sistema. */
    public SistemaContexto() {
        this(Path.of(System.getProperty("sape.dados", "dados"), "sistema.dat"));
    }

    public SistemaContexto(Path arquivo) {
        repositorio = new SistemaRepository(arquivo);
        if (repositorio.ler().getUsuarios().isEmpty()) {
            repositorio.alterar(SistemaContexto::criarDadosIniciais);
        }
        administracao = new AdministracaoService(this);
        pesquisas = new PesquisaService(this);
        coleta = new ColetaService(this);
        candidatos = new CandidatoService(this);
        analise = new AnaliseService(this);
    }

    /** Primeiro acesso: administrador padrão e categorias de exemplo para os parâmetros gerais. */
    private static void criarDadosIniciais(EstadoSistema estado) {
        estado.getUsuarios().add(new Usuario(1, "Administrador fictício", EMAIL_ADMINISTRADOR_INICIAL,
                SENHA_ADMINISTRADOR_INICIAL, Set.of(Perfil.ADMINISTRADOR)));
        adicionarParametros(estado, TipoParametro.FAIXA_ETARIA, "16-34", "35-59", "60+");
        adicionarParametros(estado, TipoParametro.ESCOLARIDADE, "Fundamental", "Médio", "Superior");
        adicionarParametros(estado, TipoParametro.REGIAO, "Norte", "Sul", "Centro");
        adicionarParametros(estado, TipoParametro.FAIXA_RENDA, "Até 2 SM", "2 a 5 SM", "Acima de 5 SM");
    }

    private static void adicionarParametros(EstadoSistema estado, TipoParametro tipo, String... valores) {
        for (String valor : valores) {
            int id = estado.getParametros().size() + 1;
            estado.getParametros().add(new Parametro(id, tipo, valor));
        }
    }

    public SistemaRepository getRepositorio() {
        return repositorio;
    }

    public AdministracaoService getAdministracao() {
        return administracao;
    }

    public PesquisaService getPesquisas() {
        return pesquisas;
    }

    public ColetaService getColeta() {
        return coleta;
    }

    public CandidatoService getCandidatos() {
        return candidatos;
    }

    public AnaliseService getAnalise() {
        return analise;
    }

    // ------------------------------------------------------------------ sessão

    /** Autentica o usuário e inicia a sessão. */
    public Usuario entrar(String email, String senha) {
        Usuario usuario = repositorio.ler().buscarUsuarioPorEmail(email)
                .orElseThrow(SistemaContexto::acessoInvalido);
        if (!usuario.autenticar(senha)) {
            throw acessoInvalido();
        }
        idUsuarioLogado = usuario.getId();
        return usuario;
    }

    public void sair() {
        idUsuarioLogado = null;
    }

    /**
     * Confere se há um usuário ativo logado e se ele possui algum dos perfis informados.
     * O administrador sempre passa; sem perfis informados, basta estar logado.
     */
    public Usuario exigirAcesso(EstadoSistema estado, Perfil... perfisPermitidos) {
        Usuario usuario = estado.getUsuarios().stream()
                .filter(u -> idUsuarioLogado != null && u.getId() == idUsuarioLogado && u.isAtivo())
                .findFirst()
                .orElseThrow(() -> new AcessoNegadoException("Entre com uma conta ativa para continuar."));
        boolean semRestricao = perfisPermitidos.length == 0 || usuario.isAdministrador();
        boolean possuiPerfil = Arrays.stream(perfisPermitidos).anyMatch(usuario::possuiPerfil);
        if (!semRestricao && !possuiPerfil) {
            throw new AcessoNegadoException("Seu perfil não permite esta operação.");
        }
        return usuario;
    }

    /** Usuário logado, conforme o cadastro atual. */
    public Usuario getUsuarioLogado() {
        return exigirAcesso(repositorio.ler());
    }

    /** Indica se o usuário logado pode usar as funcionalidades do perfil (administrador pode tudo). */
    public boolean permite(Perfil perfil) {
        Usuario usuario = getUsuarioLogado();
        return usuario.isAdministrador() || usuario.possuiPerfil(perfil);
    }

    private static AcessoNegadoException acessoInvalido() {
        return new AcessoNegadoException("E-mail ou senha inválidos, ou usuário inativo.");
    }

    @Override
    public void close() {
        repositorio.close();
    }
}
