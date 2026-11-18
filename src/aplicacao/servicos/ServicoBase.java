package aplicacao.servicos;

import aplicacao.SistemaContexto;
import dominio.admin.Perfil;
import dominio.analise.Relatorio;
import infraestrutura.exportacao.ExportadorFactory;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import persistencia.EstadoSistema;

/**
 * Classe base dos serviços de cada módulo. Reúne o acesso ao repositório e a verificação do perfil
 * do usuário logado, para que cada serviço trate apenas as regras do seu módulo.
 */
abstract class ServicoBase {

    protected final SistemaContexto contexto;

    protected ServicoBase(SistemaContexto contexto) {
        this.contexto = contexto;
    }

    /** Lê uma cópia do estado, exigindo login e (se informados) um dos perfis. */
    protected EstadoSistema ler(Perfil... perfisPermitidos) {
        EstadoSistema estado = contexto.getRepositorio().ler();
        contexto.exigirAcesso(estado, perfisPermitidos);
        return estado;
    }

    /** Executa uma alteração transacional exigindo o perfil informado. */
    protected void alterar(Perfil perfilExigido, Consumer<EstadoSistema> alteracao) {
        contexto.getRepositorio().alterar(estado -> {
            contexto.exigirAcesso(estado, perfilExigido);
            alteracao.accept(estado);
        });
    }

    /** Exporta um relatório para arquivo, exigindo o perfil informado. */
    protected void exportar(Perfil perfilExigido, Relatorio relatorio, String formato, Path destino) {
        ler(perfilExigido);
        ExportadorFactory.criar(formato).exportar(relatorio, destino);
    }

    protected static int proximoId(IntStream ids) {
        return ids.max().orElse(0) + 1;
    }
}
