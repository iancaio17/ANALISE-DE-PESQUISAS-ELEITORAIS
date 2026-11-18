package testes;

public class TesteAdministracao {

    public static void main(String[] args) throws Exception {

        System.out.println("=== TESTES DE PERSISTÊNCIA ===");
        TestePersistenciaAdministracao.main(args);

        System.out.println("\n=== TESTES DE EXCEÇÕES ===");
        TesteExcecoesAdministracao.main(args);
    }
}