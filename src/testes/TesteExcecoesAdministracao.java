package testes;

import aplicacao.servicos.AdministracaoService;
import dominio.admin.Perfil;
import dominio.admin.Usuario;
import infraestrutura.excecoes.AcessoNegadoException;
import infraestrutura.excecoes.RegraNegocioException;

import java.util.Set;

public class TesteExcecoesAdministracao {

    private static void verificarExcecao(
            String teste,
            Class<? extends RuntimeException> excecaoEsperada,
            Runnable operacao
    ) {
        try {
            operacao.run();

            throw new AssertionError(
                    "FALHOU: " + teste + " não lançou a exceção esperada."
            );

        } catch (RuntimeException e) {

            if (!excecaoEsperada.isInstance(e)) {
                throw new AssertionError(
                        "FALHOU: " + teste +
                                " lançou uma exceção diferente.",
                        e
                );
            }

            System.out.println("OK - " + teste);
            System.out.println("Mensagem: " + e.getMessage());
            System.out.println();
        }
    }

    public static void main(String[] args) {

        AdministracaoService servico = new AdministracaoService();

        Usuario administrador = new Usuario(
                100,
                "Administrador",
                "administrador@teste.com",
                "Senha12345",
                Set.of(Perfil.ADMINISTRADOR)
        );

        Usuario pesquisador = new Usuario(
                101,
                "Pesquisador",
                "pesquisador@teste.com",
                "Senha12345",
                Set.of(Perfil.PESQUISADOR)
        );

        verificarExcecao(
                "Pesquisador tentando realizar operação administrativa",
                AcessoNegadoException.class,
                () -> servico.cadastrarUsuario(pesquisador, null)
        );

        verificarExcecao(
                "Solicitante não informado",
                AcessoNegadoException.class,
                () -> servico.cadastrarUsuario(null, null)
        );

        verificarExcecao(
                "Cadastro de parâmetro nulo",
                RegraNegocioException.class,
                () -> servico.cadastrarParametro(administrador, null)
        );

        verificarExcecao(
                "Exclusão de usuário inexistente",
                RegraNegocioException.class,
                () -> servico.excluirUsuario(administrador, -1)
        );

        administrador.setAtivo(false);

        verificarExcecao(
                "Administrador inativo tentando realizar operação",
                AcessoNegadoException.class,
                () -> servico.cadastrarParametro(administrador, null)
        );

        System.out.println("TODOS OS TESTES DE EXCEÇÕES PASSARAM!");
    }
}