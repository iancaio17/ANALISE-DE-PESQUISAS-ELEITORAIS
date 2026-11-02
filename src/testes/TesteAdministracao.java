package testes;

import dominio.admin.Usuario;
import dominio.admin.Perfil;
import java.util.Set;
import aplicacao.servicos.AdministracaoService;

import javax.swing.plaf.synth.SynthOptionPaneUI;

public class TesteAdministracao {
    public static void main(String[] args) {

        Usuario administrador = new Usuario(1, "samuel", "samuel@gmail.com", "Senha123", Set.of(Perfil.ADMINISTRADOR));
        administrador.exibirDetalhes();

        AdministracaoService servico = new AdministracaoService();

        Usuario pesquisador = new Usuario(2, "joão", "joao@gmail.com", "Senha1234", Set.of(Perfil.PESQUISADOR));
        Usuario analista = new Usuario(3, "maria", "maria@gmail.com", "Senha12345", Set.of(Perfil.ANALISTA));
        Usuario analista2 = new Usuario(4, "pedro", "joaoo@gmail.com", "Senha123", Set.of(Perfil.ANALISTA));


        servico.cadastrarUsuario(administrador, pesquisador);

        try {
            servico.cadastrarUsuario(administrador, analista2);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        try {
            servico.cadastrarUsuario(pesquisador, analista);
        } catch (IllegalArgumentException e) {
            System.out.printf(e.getMessage());
        }

        for(Usuario u : servico.listarUsuarios()){
            u.exibirDetalhes();
        }

        System.out.println("usuarios encontrados por nome:");

        for (Usuario u : servico.buscarUsuariosPorNome("JO")){
            u.exibirDetalhes();
        }

        System.out.println("Encontrados na busca:");

        for (Usuario u : servico.buscarUsuarios("j", null, true)){
            u.exibirDetalhes();
        }

        System.out.println("teste mudança email");

        try {
            servico.alterarEmailUsuario(administrador, 999, "joaoo@gmail.com");
        }
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
        pesquisador.exibirDetalhes();

        System.out.println("nome alterado:");

        servico.alterarNomeUsuario(administrador, 2, "João Pedro");
        pesquisador.exibirDetalhes();

        System.out.println("teste alterar perfil");

        servico.alterarPerfisUsuario(administrador, 2, Set.of(Perfil.PESQUISADOR, Perfil.ANALISTA));
        pesquisador.exibirDetalhes();

        System.out.println("teste alterar status:");

        servico.alterarStatusUsuario(administrador, pesquisador.getEmail(), false);
        pesquisador.exibirDetalhes();

        System.out.println("teste autenticar inativo:");

        try {
            servico.autenticar(pesquisador.getEmail(),"Senha1234");
        } catch (IllegalStateException e){
            System.out.println(e.getMessage());
        }

        servico.alterarStatusUsuario(administrador, pesquisador.getEmail(), true);

        pesquisador.exibirDetalhes();

        try {
            servico.autenticar(pesquisador.getEmail(),"Senha1234");
        } catch (IllegalStateException e){
            System.out.println(e.getMessage());
        }

        System.out.println("teste excluir usuarios");
        servico.excluirUsuario(administrador, 4);
        System.out.println(servico.buscarPorId(4) == null);
        servico.excluirUsuario(administrador, 4);

    }
}
