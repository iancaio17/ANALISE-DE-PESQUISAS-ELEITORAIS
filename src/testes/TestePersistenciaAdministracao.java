package testes;

import aplicacao.servicos.AdministracaoService;
import dominio.admin.Parametro;
import dominio.admin.Perfil;
import dominio.admin.TipoParametro;
import dominio.admin.Usuario;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class TestePersistenciaAdministracao {

    public static void main(String[] args) throws Exception {

        AdministracaoService servico = new AdministracaoService();

        Usuario administrador = new Usuario(
                990000,
                "Administrador Teste",
                "admin.teste@exemplo.com",
                "Senha12345",
                Set.of(Perfil.ADMINISTRADOR)
        );

        int idUsuario = 990001;
        int idParametro = 990001;

        boolean usuarioJaExistia =
                servico.buscarPorId(idUsuario) != null;

        boolean parametroJaExistia =
                servico.listarParametros().stream()
                        .anyMatch(p -> p.getId() == idParametro);

        if (!usuarioJaExistia) {

            Usuario novoUsuario = new Usuario(
                    idUsuario,
                    "Usuario Persistencia",
                    "persistencia.teste@exemplo.com",
                    "Senha12345",
                    Set.of(Perfil.PESQUISADOR)
            );

            servico.cadastrarUsuario(administrador, novoUsuario);

            System.out.println("Usuário gravado com sucesso.");
        }

        if (!parametroJaExistia) {

            Parametro parametro = new Parametro(
                    idParametro,
                    TipoParametro.values()[0],
                    "Teste de persistencia 990001"
            );

            servico.cadastrarParametro(administrador, parametro);

            System.out.println("Parâmetro gravado com sucesso.");
        }

        Usuario usuarioRecuperado = servico.buscarPorId(idUsuario);

        Parametro parametroRecuperado =
                servico.listarParametros().stream()
                        .filter(p -> p.getId() == idParametro)
                        .findFirst()
                        .orElse(null);

        if (usuarioRecuperado == null || parametroRecuperado == null) {
            throw new IllegalStateException(
                    "Falha ao recuperar os dados."
            );
        }

        System.out.println("\n=== DADOS ENCONTRADOS ===");

        System.out.println(
                "Usuário: " + usuarioRecuperado.getNome()
        );

        System.out.println(
                "Parâmetro: " + parametroRecuperado.getValor()
        );

        Path arquivoUsuarios = Path.of(
                "src", "persistencia", "arquivos", "usuarios.dat"
        );

        Path arquivoParametros = Path.of(
                "src", "persistencia", "arquivos", "parametros.dat"
        );

        boolean arquivosValidos =
                Files.isRegularFile(arquivoUsuarios) &&
                        Files.isRegularFile(arquivoParametros) &&
                        Files.size(arquivoUsuarios) > 0 &&
                        Files.size(arquivoParametros) > 0;

        if (!arquivosValidos) {
            throw new IllegalStateException(
                    "Os arquivos de persistência não foram criados corretamente."
            );
        }

        System.out.println("\nArquivos .dat encontrados com sucesso.");

        if (usuarioJaExistia && parametroJaExistia) {
            System.out.println(
                    "SUCESSO: os dados foram recuperados de uma execução anterior!"
            );
        } else {
            System.out.println(
                    "Dados gravados. Execute o programa novamente para testar a recuperação."
            );
        }
    }
}