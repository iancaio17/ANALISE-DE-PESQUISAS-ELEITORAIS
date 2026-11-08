package aplicacao.servicos;

import dominio.admin.Parametro;
import dominio.admin.Perfil;
import dominio.admin.TipoParametro;
import dominio.admin.Usuario;
import infraestrutura.excecoes.AcessoNegadoException;
import infraestrutura.excecoes.RegraNegocioException;
import persistencia.repositorios.AdministracaoArquivoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdministracaoService {

    private final AdministracaoArquivoRepository repositorio;

    public AdministracaoService() {
        this.repositorio = AdministracaoArquivoRepository.getInstance();
    }

    private void validarAdministrador(Usuario solicitante) {
        if (solicitante == null) {
            throw new AcessoNegadoException("Solicitante não informado.");
        }

        if (!solicitante.isAtivo()) {
            throw new AcessoNegadoException(
                    "Usuário inativo não pode realizar essa operação."
            );
        }

        if (!solicitante.getPerfis().contains(Perfil.ADMINISTRADOR)) {
            throw new AcessoNegadoException(
                    "Apenas administradores podem realizar essa operação."
            );
        }
    }

    private Usuario localizarUsuario(List<Usuario> usuarios, int id) {
        for (Usuario u : usuarios) {
            if (u.getId() == id) {
                return u;
            }
        }

        return null;
    }

    private Parametro localizarParametro(List<Parametro> parametros, int id) {
        for (Parametro p : parametros) {
            if (p.getId() == id) {
                return p;
            }
        }

        return null;
    }

    public void cadastrarUsuario(Usuario solicitante, Usuario novoUsuario) {
        validarAdministrador(solicitante);

        if (novoUsuario == null) {
            throw new RegraNegocioException("Usuário não pode ser nulo.");
        }

        List<Usuario> usuarios = repositorio.listarUsuarios();

        for (Usuario u : usuarios) {
            if (u.getId() == novoUsuario.getId()) {
                throw new RegraNegocioException(
                        "Já existe um usuário cadastrado com este ID."
                );
            }

            if (u.getEmail().equalsIgnoreCase(novoUsuario.getEmail())) {
                throw new RegraNegocioException(
                        "Já existe um usuário cadastrado com este e-mail."
                );
            }
        }

        usuarios.add(novoUsuario);
        repositorio.salvarUsuarios(usuarios);

        System.out.println(
                "Usuário '" + novoUsuario.getNome() + "' cadastrado com sucesso."
        );
    }

    public Usuario autenticar(String email, String senha) {
        if (email == null || senha == null) {
            throw new RegraNegocioException(
                    "E-mail e senha são obrigatórios."
            );
        }

        for (Usuario u : repositorio.listarUsuarios()) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {

                if (!u.isAtivo()) {
                    throw new AcessoNegadoException(
                            "Este usuário está inativo no sistema."
                    );
                }

                if (u.autenticar(senha)) {
                    System.out.println(
                            "Login aprovado! Bem-vindo(a), " + u.getNome() + "."
                    );

                    return u;
                }

                throw new RegraNegocioException("Senha incorreta.");
            }
        }

        throw new RegraNegocioException(
                "E-mail não encontrado no sistema."
        );
    }

    public void alterarStatusUsuario(
            Usuario solicitante,
            String email,
            boolean novoStatus
    ) {
        validarAdministrador(solicitante);

        List<Usuario> usuarios = repositorio.listarUsuarios();

        for (Usuario u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) {

                u.setAtivo(novoStatus);
                repositorio.salvarUsuarios(usuarios);

                System.out.println(
                        "Status do usuário '" + u.getNome() +
                                "' alterado para " +
                                (novoStatus ? "Ativo" : "Inativo") + "."
                );

                return;
            }
        }

        throw new RegraNegocioException(
                "Usuário não encontrado para alteração de status."
        );
    }

    public List<Usuario> listarUsuarios() {
        return List.copyOf(repositorio.listarUsuarios());
    }

    public List<Usuario> buscarUsuariosPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException(
                    "O nome pesquisado não pode ser nulo ou vazio."
            );
        }

        String termo = nome.trim().toLowerCase(Locale.ROOT);
        List<Usuario> encontrados = new ArrayList<>();

        for (Usuario u : repositorio.listarUsuarios()) {
            if (u.getNome().toLowerCase(Locale.ROOT).contains(termo)) {
                encontrados.add(u);
            }
        }

        return encontrados;
    }

    public List<Usuario> buscarUsuarios(
            String nome,
            Perfil perfil,
            Boolean ativo
    ) {
        List<Usuario> encontrados = new ArrayList<>();

        String termo = nome == null
                ? null
                : nome.trim().toLowerCase(Locale.ROOT);

        for (Usuario u : repositorio.listarUsuarios()) {
            boolean nomeConfere =
                    termo == null ||
                            u.getNome().toLowerCase(Locale.ROOT).contains(termo);

            boolean perfilConfere =
                    perfil == null ||
                            u.getPerfis().contains(perfil);

            boolean statusConfere =
                    ativo == null ||
                            u.isAtivo() == ativo;

            if (nomeConfere && perfilConfere && statusConfere) {
                encontrados.add(u);
            }
        }

        return encontrados;
    }

    public Usuario buscarPorId(int id) {
        return localizarUsuario(repositorio.listarUsuarios(), id);
    }

    public void alterarEmailUsuario(
            Usuario solicitante,
            int id,
            String novoEmail
    ) {
        validarAdministrador(solicitante);

        List<Usuario> usuarios = repositorio.listarUsuarios();
        Usuario encontrado = localizarUsuario(usuarios, id);

        if (encontrado == null) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }

        if (novoEmail == null || novoEmail.trim().isEmpty()) {
            throw new RegraNegocioException(
                    "O novo e-mail não pode ser nulo ou vazio."
            );
        }

        String emailNormalizado = novoEmail.trim();

        for (Usuario u : usuarios) {
            if (u != encontrado &&
                    u.getEmail().equalsIgnoreCase(emailNormalizado)) {

                throw new RegraNegocioException(
                        "O e-mail informado já está sendo utilizado."
                );
            }
        }

        encontrado.setEmail(emailNormalizado);
        repositorio.salvarUsuarios(usuarios);
    }

    public void alterarNomeUsuario(
            Usuario solicitante,
            int id,
            String novoNome
    ) {
        validarAdministrador(solicitante);

        List<Usuario> usuarios = repositorio.listarUsuarios();
        Usuario encontrado = localizarUsuario(usuarios, id);

        if (encontrado == null) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }

        encontrado.setNome(novoNome);
        repositorio.salvarUsuarios(usuarios);
    }

    public void alterarPerfisUsuario(
            Usuario solicitante,
            int id,
            Set<Perfil> novosPerfis
    ) {
        validarAdministrador(solicitante);

        List<Usuario> usuarios = repositorio.listarUsuarios();
        Usuario encontrado = localizarUsuario(usuarios, id);

        if (encontrado == null) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }

        encontrado.setPerfis(novosPerfis);
        repositorio.salvarUsuarios(usuarios);
    }

    public void excluirUsuario(Usuario solicitante, int id) {
        validarAdministrador(solicitante);

        List<Usuario> usuarios = repositorio.listarUsuarios();
        Usuario encontrado = localizarUsuario(usuarios, id);

        if (encontrado == null) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }

        usuarios.remove(encontrado);
        repositorio.salvarUsuarios(usuarios);
    }

    public void alterarSenha(
            Usuario solicitante,
            int id,
            String novaSenha
    ) {
        validarAdministrador(solicitante);

        List<Usuario> usuarios = repositorio.listarUsuarios();
        Usuario encontrado = localizarUsuario(usuarios, id);

        if (encontrado == null) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }

        encontrado.setSenha(novaSenha);
        repositorio.salvarUsuarios(usuarios);
    }

    public int contarUsuariosPorStatus(
            Usuario solicitante,
            boolean ativo
    ) {
        validarAdministrador(solicitante);

        int contador = 0;

        for (Usuario u : repositorio.listarUsuarios()) {
            if (u.isAtivo() == ativo) {
                contador++;
            }
        }

        return contador;
    }

    public int contarUsuariosPorPerfil(
            Usuario solicitante,
            Perfil perfil
    ) {
        validarAdministrador(solicitante);

        if (perfil == null) {
            throw new RegraNegocioException(
                    "O perfil não pode ser nulo."
            );
        }

        int contador = 0;

        for (Usuario u : repositorio.listarUsuarios()) {
            if (u.getPerfis().contains(perfil)) {
                contador++;
            }
        }

        return contador;
    }

    public void cadastrarParametro(
            Usuario solicitante,
            Parametro novoParametro
    ) {
        validarAdministrador(solicitante);

        if (novoParametro == null) {
            throw new RegraNegocioException(
                    "O parâmetro não pode ser nulo."
            );
        }

        List<Parametro> parametros = repositorio.listarParametros();

        for (Parametro p : parametros) {
            if (p.getId() == novoParametro.getId()) {
                throw new RegraNegocioException(
                        "Já existe um parâmetro com este ID."
                );
            }

            if (p.getTipo() == novoParametro.getTipo() &&
                    p.getValor().equalsIgnoreCase(novoParametro.getValor())) {

                throw new RegraNegocioException(
                        "Este parâmetro já está cadastrado."
                );
            }
        }

        parametros.add(novoParametro);
        repositorio.salvarParametros(parametros);
    }

    public List<Parametro> listarParametros() {
        return List.copyOf(repositorio.listarParametros());
    }

    public List<Parametro> buscarParametrosPorTipo(TipoParametro tipo) {
        if (tipo == null) {
            throw new RegraNegocioException(
                    "O tipo não pode ser nulo."
            );
        }

        List<Parametro> encontrados = new ArrayList<>();

        for (Parametro p : repositorio.listarParametros()) {
            if (p.getTipo() == tipo) {
                encontrados.add(p);
            }
        }

        return encontrados;
    }

    public void alterarValorParametro(
            Usuario solicitante,
            int id,
            String novoValor
    ) {
        validarAdministrador(solicitante);

        List<Parametro> parametros = repositorio.listarParametros();
        Parametro encontrado = localizarParametro(parametros, id);

        if (encontrado == null) {
            throw new RegraNegocioException(
                    "Parâmetro não encontrado."
            );
        }

        if (novoValor == null || novoValor.trim().isEmpty()) {
            throw new RegraNegocioException(
                    "O novo valor não pode ser nulo ou vazio."
            );
        }

        String valorNormalizado = novoValor.trim();

        for (Parametro p : parametros) {
            if (p != encontrado &&
                    p.getTipo() == encontrado.getTipo() &&
                    p.getValor().equalsIgnoreCase(valorNormalizado)) {

                throw new RegraNegocioException(
                        "Já existe um parâmetro desse tipo com o mesmo valor."
                );
            }
        }

        encontrado.setValor(valorNormalizado);
        repositorio.salvarParametros(parametros);
    }

    public void excluirParametro(Usuario solicitante, int id) {
        validarAdministrador(solicitante);

        List<Parametro> parametros = repositorio.listarParametros();
        Parametro encontrado = localizarParametro(parametros, id);

        if (encontrado == null) {
            throw new RegraNegocioException(
                    "Parâmetro não encontrado."
            );
        }

        parametros.remove(encontrado);
        repositorio.salvarParametros(parametros);
    }
}