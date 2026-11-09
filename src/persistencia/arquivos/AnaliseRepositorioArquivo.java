package persistencia.arquivos;

import dominio.analise.Analise;
import infraestrutura.excecoes.PersistenciaException;
import persistencia.repositorios.AnaliseRepositorio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementação do repositório usando serialização de objetos em um arquivo binário (.dat). */
public class AnaliseRepositorioArquivo implements AnaliseRepositorio {

    private final Path arquivo;
    private final Map<String, Analise> analises = new LinkedHashMap<>();

    public AnaliseRepositorioArquivo(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    @Override
    public synchronized void salvar(Analise analise) {
        analises.put(chave(analise.getCodigoPesquisa()), analise);
        gravar();
    }

    @Override
    public synchronized Optional<Analise> buscarPorCodigoPesquisa(String codigoPesquisa) {
        return Optional.ofNullable(analises.get(chave(codigoPesquisa)));
    }

    @Override
    public synchronized List<Analise> listarTodas() {
        return new ArrayList<>(analises.values());
    }

    private String chave(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    @SuppressWarnings("unchecked")
    private void carregar() {
        if (!Files.exists(arquivo)) return;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(arquivo))) {
            Map<String, Analise> lidas = (Map<String, Analise>) in.readObject();
            analises.putAll(lidas);
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo de análises '" + arquivo + "'.", e);
        }
    }

    private void gravar() {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) Files.createDirectories(pasta);
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporario))) {
                out.writeObject(new LinkedHashMap<>(analises));
            }
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo de análises '" + arquivo + "'.", e);
        }
    }
}
