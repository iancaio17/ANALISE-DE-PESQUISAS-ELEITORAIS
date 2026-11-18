package infraestrutura.exportacao;

import dominio.analise.Relatorio;
import infraestrutura.excecoes.ExportacaoException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Template Method: define os passos comuns da exportação (criar a pasta e gravar os bytes).
 * As subclasses só decidem como o conteúdo do arquivo é montado.
 */
public abstract class ExportadorArquivo implements ExportadorRelatorio {

    @Override
    public final void exportar(Relatorio relatorio, Path destino) {
        try {
            byte[] conteudo = montarConteudo(relatorio);
            Path pasta = destino.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            Files.write(destino, conteudo);
        } catch (IOException e) {
            throw new ExportacaoException("Não foi possível gravar o arquivo "
                    + getExtensao().toUpperCase() + " em '" + destino + "'.", e);
        }
    }

    /** Monta os bytes do arquivo no formato da subclasse. */
    protected abstract byte[] montarConteudo(Relatorio relatorio) throws IOException;
}
