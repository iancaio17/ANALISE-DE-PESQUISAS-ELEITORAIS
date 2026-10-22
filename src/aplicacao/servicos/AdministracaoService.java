package aplicacao.servicos;

import dominio.admin.Usuario;
import java.util.ArrayList;
import java.util.List;

public class AdministracaoService {

    private List<Usuario> bancoUsuarios;

    public AdministracaoService() {
        this.bancoUsuarios = new ArrayList<>();
    }

    public void cadastrarUsuario(Usuario novoUsuario) {
        if (novoUsuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }

        for (Usuario u : bancoUsuarios) {
            if (u.getEmail().equalsIgnoreCase(novoUsuario.getEmail())) {
                throw new IllegalArgumentException("Erro: Já existe um usuário cadastrado com este e-mail.");
            }
        }

        bancoUsuarios.add(novoUsuario);
        System.out.println("Sucesso: Usuário '" + novoUsuario.getNome() + "' cadastrado como " + novoUsuario.getPerfil() + ".");
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

    public void alterarStatusUsuario(String email, boolean novoStatus) {
        for (Usuario u : bancoUsuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                u.setAtivo(novoStatus);
                System.out.println("O status do usuário '" + u.getNome() + "' foi alterado para " + (novoStatus ? "Ativo" : "Inativo") + ".");
                return;
            }
        }
        throw new IllegalArgumentException("Erro: Usuário não encontrado para alteração de status.");
    }

    public List<Usuario> getBancoUsuarios() {
        return bancoUsuarios;
    }
}
