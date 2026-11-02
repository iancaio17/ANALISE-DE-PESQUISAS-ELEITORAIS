package aplicacao.servicos;

import dominio.admin.Parametro;
import dominio.admin.TipoParametro;
import dominio.admin.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dominio.admin.Perfil;

public class AdministracaoService {

    private List<Usuario> bancoUsuarios;
    private List<Parametro> bancoParametros;

    public AdministracaoService() {
        this.bancoUsuarios = new ArrayList<>();
        this.bancoParametros = new ArrayList<>();
    }

    private void validarAdministrador(Usuario solicitante){
        if (solicitante == null) {
            throw new IllegalArgumentException("Solicitante não informado.");
        }
        if (!solicitante.isAtivo()) {
            throw new IllegalArgumentException("Usuário inativo não pode realizar essa operação");
        }
        if (!solicitante.getPerfis().contains(Perfil.ADMINISTRADOR)) {
            throw new IllegalArgumentException("Apenas administradores podem realizar essa operação.");
        }
    }

    public void cadastrarUsuario(Usuario solicitante, Usuario novoUsuario) {
        validarAdministrador(solicitante);

        if (novoUsuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }

        for (Usuario u : bancoUsuarios) {
            if (u.getEmail().equalsIgnoreCase(novoUsuario.getEmail())) {
                throw new IllegalArgumentException("Erro: Já existe um usuário cadastrado com este e-mail.");
            }
        }
        bancoUsuarios.add(novoUsuario);
        System.out.println("Sucesso: Usuário '" + novoUsuario.getNome() + "' cadastrado como " + novoUsuario.getPerfis() + ".");
    }

    public Usuario autenticar(String email, String senha) {
        for (Usuario u : bancoUsuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) {

                if (!u.isAtivo()) {
                    throw new IllegalStateException("Acesso negado: Este usuário está inativo no sistema.");
                }

                if (u.autenticar(senha)) {
                    System.out.println("Login aprovado! Bem-vindo(a), " + u.getNome() + ".");
                    return u;
                } else {
                    throw new IllegalArgumentException("Erro: Senha incorreta.");
                }
            }
        }
        throw new IllegalArgumentException("Erro: E-mail não encontrado no sistema.");
    }

    public void alterarStatusUsuario(Usuario solicitante, String email, boolean novoStatus) {
        validarAdministrador(solicitante);
        for (Usuario u : bancoUsuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                u.setAtivo(novoStatus);
                System.out.println("O status do usuário '" + u.getNome() + "' foi alterado para " + (novoStatus ? "Ativo" : "Inativo") + ".");
                return;
            }
        }
        throw new IllegalArgumentException("Erro: Usuário não encontrado para alteração de status.");
    }

    public List<Usuario> listarUsuarios(){ return List.copyOf(bancoUsuarios); }

    public List<Usuario> buscarUsuariosPorNome(String nome){
        if(nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome pesquisado não pode ser nulo ou vazio.");
        }

        List<Usuario> encontrados = new ArrayList<>();
        for(Usuario u : bancoUsuarios){
            if(u.getNome().toLowerCase().contains(nome.toLowerCase())){
                encontrados.add(u);
            }
        }
        return encontrados;
    }

    public List<Usuario> buscarUsuarios(String nome, Perfil perfil, Boolean ativo) {
        List<Usuario> encontrados = new ArrayList<>();
        for(Usuario u : bancoUsuarios){
            boolean nomeConfere = nome == null || u.getNome().toLowerCase().contains(nome.toLowerCase());
            boolean perfilConfere = perfil == null || u.getPerfis().contains(perfil);
            boolean statusConfere = ativo == null || u.isAtivo() == ativo;
            if (nomeConfere && perfilConfere && statusConfere){
                encontrados.add(u);
            }
        }
        return encontrados;
    }

    public Usuario buscarPorId(int id){
        for (Usuario u : bancoUsuarios){
            if (u.getId() == id){
                return u;
            }
        }
        return null;
    }

    public void alterarEmailUsuario(Usuario solicitante, int id, String novoEmail){
        validarAdministrador(solicitante);

        Usuario usuarioEncontrado = buscarPorId(id);

        if (usuarioEncontrado == null){
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        for (Usuario u : bancoUsuarios){
            if(u.getEmail().equalsIgnoreCase(novoEmail) && u != usuarioEncontrado){
                throw new IllegalArgumentException("O email informado já está sendo usado.");
            }
        }

        usuarioEncontrado.setEmail(novoEmail);
    }

    public void alterarNomeUsuario(Usuario solicitante, int id, String novoNome){
        validarAdministrador(solicitante);

        Usuario usuarioEncontrado = buscarPorId(id);

        if (usuarioEncontrado == null){
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        usuarioEncontrado.setNome(novoNome);
    }

    public void alterarPerfisUsuario(Usuario solicitante, int id, Set<Perfil> novosPerfis){
        validarAdministrador(solicitante);

        Usuario usuarioEncontrado = buscarPorId(id);

        if (usuarioEncontrado == null){
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        usuarioEncontrado.setPerfis(novosPerfis);
    }

    public void excluirUsuario(Usuario solicitante, int id){
        validarAdministrador(solicitante);

        Usuario usuarioEncontrado = buscarPorId(id);

        if (usuarioEncontrado == null){
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        bancoUsuarios.remove(usuarioEncontrado);
    }

    public void alterarSenha(Usuario solicitante, int id, String novaSenha){
        validarAdministrador(solicitante);

        Usuario usuarioEncontrado = buscarPorId(id);

        if (usuarioEncontrado == null){
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        usuarioEncontrado.setSenha(novaSenha);
    }

    public int contarUsuariosPorStatus(Usuario solicitante, boolean ativo){
        validarAdministrador(solicitante);
        int contador = 0;
        for (Usuario u : bancoUsuarios){
            if (u.isAtivo() == ativo){
                contador++;
            }
        }
        return contador;
    }

    public int contarUsuariosPorPerfil(Usuario solicitante, Perfil perfil){
        validarAdministrador(solicitante);
        if (perfil == null){
            throw new IllegalArgumentException("O perfil não pode ser nulo.");
        }
        int contador = 0;
        for (Usuario u : bancoUsuarios){
            if (u.getPerfis().contains(perfil)){
                contador++;
            }
        }
        return contador;
    }

    public void cadastrarParametro(Usuario solicitante, Parametro novoParametro){
        validarAdministrador(solicitante);
        if (novoParametro == null){
            throw new IllegalArgumentException("O parâmetro não pode ser nulo ou vazio.");
        }
        for (Parametro p : bancoParametros){

            if (p.getTipo() == novoParametro.getTipo() && p.getValor().equalsIgnoreCase(novoParametro.getValor())){
                throw new IllegalArgumentException("Este parâmetro já está cadastrado.");
            }
            if (p.getId() == novoParametro.getId()){
                throw new IllegalArgumentException("Já existe um parâmetro com este ID.");
            }
        }
        bancoParametros.add(novoParametro);
    }

    public List<Parametro> listarParametros() {
        return List.copyOf(bancoParametros);
    }

    public List<Parametro> buscarParametrosPorTipo(TipoParametro tipo) {
        if (tipo == null){
            throw new IllegalArgumentException("O tipo não pode ser nulo.");
        }
        List<Parametro> encontrados = new ArrayList<>();
        for (Parametro p : bancoParametros){
            if (p.getTipo() == tipo){
                encontrados.add(p);
            }
        }
        return encontrados;
    }

    private Parametro buscarParametroPorId(int id) {
        for (Parametro p : bancoParametros){
            if (p.getId() == id){
                return p;
            }
        }
        return null;
    }

    public void alterarValorParametro(Usuario solicitante, int id, String novoValor) {
        validarAdministrador(solicitante);

        Parametro encontrado = buscarParametroPorId(id);

        if (encontrado == null) {
            throw new IllegalArgumentException("Parâmetro não encontrado.");
        }
        if (novoValor == null || novoValor.trim().isEmpty()) {
            throw new IllegalArgumentException("O novo valor não pode ser nulo ou vazio.");
        }

        String valorNormalizado = novoValor.trim();

        for (Parametro p : bancoParametros) {
            if (p != encontrado &&
                    p.getTipo() == encontrado.getTipo() &&
                    p.getValor().equalsIgnoreCase(valorNormalizado)) {
                throw new IllegalArgumentException("Já existe um parâmetro desse tipo com o mesmo valor.");
            }
        }
        encontrado.setValor(valorNormalizado);
    }

    public void excluirParametro(Usuario solicitante, int id) {
        validarAdministrador(solicitante);

        Parametro encontrado = buscarParametroPorId(id);

        if (encontrado == null) {
            throw new IllegalArgumentException("Parâmetro não encontrado.");
        }

        bancoParametros.remove(encontrado);
    }

}

