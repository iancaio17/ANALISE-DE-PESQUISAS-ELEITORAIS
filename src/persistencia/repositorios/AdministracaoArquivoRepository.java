package persistencia.repositorios;

import dominio.admin.Usuario;
import dominio.admin.Parametro;
import infraestrutura.excecoes.PersistenciaException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class AdministracaoArquivoRepository {

    private static AdministracaoArquivoRepository instance;

    private static final Path DIRETORIO =
            Paths.get("src", "persistencia", "arquivos");

    private static final Path ARQUIVO_USUARIOS =
            DIRETORIO.resolve("usuarios.dat");

    private static final Path ARQUIVO_PARAMETROS =
            DIRETORIO.resolve("parametros.dat");

    private final List<Usuario> usuarios;
    private final List<Parametro> parametros;

    private AdministracaoArquivoRepository() {
        garantirDiretorio();

        this.usuarios = carregarArquivo(ARQUIVO_USUARIOS);
        this.parametros = carregarArquivo(ARQUIVO_PARAMETROS);
    }

    public static synchronized AdministracaoArquivoRepository getInstance() {
        if (instance == null) {
            instance = new AdministracaoArquivoRepository();
        }

        return instance;
    }

    private void garantirDiretorio() {
        try {
            Files.createDirectories(DIRETORIO);
        } catch (IOException e) {
            throw new PersistenciaException(
                    "Erro ao criar o diretório de persistência.", e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> carregarArquivo(Path caminho) {
        if (!Files.exists(caminho)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream entrada =
                     new ObjectInputStream(Files.newInputStream(caminho))) {

            Object dados = entrada.readObject();

            if (!(dados instanceof List<?>)) {
                throw new IOException("Formato de arquivo inválido.");
            }

            return new ArrayList<>((List<T>) dados);

        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException(
                    "Erro ao carregar os dados de " + caminho, e
            );
        }
    }

    private void salvarArquivo(Path caminho, List<?> dados) {
        Path temporario = null;

        try {
            temporario = Files.createTempFile(
                    DIRETORIO, "admin-", ".tmp"
            );

            try (ObjectOutputStream saida =
                         new ObjectOutputStream(
                                 Files.newOutputStream(temporario))) {

                saida.writeObject(dados);
            }

            try {
                Files.move(
                        temporario,
                        caminho,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporario,
                        caminho,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {
            throw new PersistenciaException(
                    "Erro ao salvar os dados em " + caminho, e
            );

        } finally {
            if (temporario != null) {
                try {
                    Files.deleteIfExists(temporario);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public synchronized List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    public synchronized List<Parametro> listarParametros() {
        return new ArrayList<>(parametros);
    }

    public synchronized void salvarUsuarios(List<Usuario> novosUsuarios) {
        if (novosUsuarios == null) {
            throw new IllegalArgumentException(
                    "A lista de usuários não pode ser nula."
            );
        }

        List<Usuario> copia = new ArrayList<>(novosUsuarios);

        salvarArquivo(ARQUIVO_USUARIOS, copia);

        usuarios.clear();
        usuarios.addAll(copia);
    }

    public synchronized void salvarParametros(List<Parametro> novosParametros) {
        if (novosParametros == null) {
            throw new IllegalArgumentException(
                    "A lista de parâmetros não pode ser nula."
            );
        }

        List<Parametro> copia = new ArrayList<>(novosParametros);

        salvarArquivo(ARQUIVO_PARAMETROS, copia);

        parametros.clear();
        parametros.addAll(copia);
    }
}