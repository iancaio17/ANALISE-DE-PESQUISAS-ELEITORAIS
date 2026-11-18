package persistencia;

import infraestrutura.excecoes.PersistenciaException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

/**
 * Persistência em arquivo por serialização de objetos.
 * Cada alteração trabalha sobre uma cópia do estado; só depois de gravar com sucesso a cópia
 * passa a valer em memória. Uma falha, portanto, não deixa o sistema em estado inconsistente.
 */
public final class SistemaRepository implements AutoCloseable {

    private static final String FILTRO_DE_CLASSES = "maxdepth=100;maxrefs=2000000;maxbytes=134217728;"
            + "dominio.**;persistencia.EstadoSistema;java.base/*;!*";

    private final Path arquivo;
    private EstadoSistema estado;
    private FileChannel canalDeTrava;
    private FileLock trava;

    /** Abre (ou cria) o arquivo de dados e impede que outra instância use a mesma pasta. */
    public SistemaRepository(Path arquivo) {
        this.arquivo = arquivo.toAbsolutePath();
        try {
            Files.createDirectories(this.arquivo.getParent());
            travarPasta();
            estado = Files.exists(this.arquivo) ? lerArquivo() : new EstadoSistema();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            close();
            throw new PersistenciaException(
                    "Não foi possível abrir os dados: " + e.getMessage() + " Arquivo: " + this.arquivo, e);
        }
    }

    public Path getArquivo() {
        return arquivo;
    }

    /** Devolve uma cópia independente do estado atual. */
    public synchronized EstadoSistema ler() {
        return copiar(estado);
    }

    /** Aplica a operação em uma cópia e só publica a mudança depois de gravar o arquivo. */
    public synchronized void alterar(Consumer<EstadoSistema> operacao) {
        EstadoSistema novoEstado = copiar(estado);
        operacao.accept(novoEstado);
        gravar(novoEstado);
        estado = copiar(novoEstado);
    }

    @Override
    public void close() {
        try {
            if (trava != null) {
                trava.release();
            }
            if (canalDeTrava != null) {
                canalDeTrava.close();
            }
        } catch (IOException ignorada) {
            // Fechando a aplicação: não há o que fazer se a trava já foi liberada.
        }
    }

    private void travarPasta() throws IOException {
        Path arquivoDeTrava = arquivo.resolveSibling(arquivo.getFileName() + ".lock");
        canalDeTrava = FileChannel.open(arquivoDeTrava, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            trava = canalDeTrava.tryLock();
        } catch (OverlappingFileLockException e) {
            throw new IOException("O sistema já está aberto nesta pasta.");
        }
        if (trava == null) {
            throw new IOException("O sistema já está aberto nesta pasta.");
        }
    }

    private EstadoSistema lerArquivo() throws IOException, ClassNotFoundException {
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(arquivo))) {
            entrada.setObjectInputFilter(ObjectInputFilter.Config.createFilter(FILTRO_DE_CLASSES));
            EstadoSistema lido = (EstadoSistema) entrada.readObject();
            if (lido.getUsuarios().isEmpty()) {
                throw new IOException("Arquivo existente sem administrador. Restaure um backup.");
            }
            return lido;
        }
    }

    /** Grava em arquivo temporário, guarda a versão anterior em ".bak" e troca os arquivos. */
    private void gravar(EstadoSistema novoEstado) {
        Path temporario = null;
        try {
            temporario = Files.createTempFile(arquivo.getParent(), "sape-", ".tmp");
            try (FileOutputStream saida = new FileOutputStream(temporario.toFile());
                    ObjectOutputStream objetos = new ObjectOutputStream(saida)) {
                objetos.writeObject(novoEstado);
                objetos.flush();
                saida.getFD().sync();
            }
            if (Files.exists(arquivo)) {
                Files.copy(arquivo, arquivo.resolveSibling(arquivo.getFileName() + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            substituirArquivo(temporario);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível salvar. A operação foi desfeita.", e);
        } finally {
            apagarSilenciosamente(temporario);
        }
    }

    private void substituirArquivo(Path temporario) throws IOException {
        try {
            Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void apagarSilenciosamente(Path caminho) {
        if (caminho == null) {
            return;
        }
        try {
            Files.deleteIfExists(caminho);
        } catch (IOException ignorada) {
            // O arquivo temporário é descartável.
        }
    }

    /** Cópia profunda por serialização, para isolar quem recebe o estado do estado persistido. */
    private static EstadoSistema copiar(EstadoSistema original) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream saida = new ObjectOutputStream(bytes)) {
                saida.writeObject(original);
            }
            try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                return (EstadoSistema) entrada.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Falha ao preparar os dados.", e);
        }
    }
}
