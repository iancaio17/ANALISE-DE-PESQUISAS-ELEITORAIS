package apresentacao.admin;

import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.indicador;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.tabela;
import static apresentacao.componentes.Componentes.texto;
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.BarraExportacao;
import dominio.admin.Perfil;
import dominio.analise.RelatorioUsuarios;
import javafx.scene.Node;
import javafx.scene.control.TableView;

/** Quantidade de usuários por situação e perfil, com exportação em CSV e PDF. */
class AbaRelatorioUsuarios extends Aba {

    AbaRelatorioUsuarios(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        RelatorioUsuarios relatorio = administracao().gerarRelatorioUsuarios();
        long ativos = relatorio.quantidadeAtivos();
        long inativos = relatorio.quantidadeInativos();

        TableView<Perfil> porPerfil = tabela();
        colunas(porPerfil,
                coluna("Perfil", perfil -> perfil),
                coluna("Quantidade", relatorio::quantidadePorPerfil));
        porPerfil.getItems().setAll(Perfil.values());

        BarraExportacao exportacao = new BarraExportacao(this::janela, () -> relatorio, "usuarios",
                administracao()::exportarRelatorio);
        exportacao.habilitar(true);

        return pagina(
                linha(indicador(ativos + inativos, "Usuários cadastrados"), indicador(ativos, "Ativos"),
                        indicador(inativos, "Inativos")),
                painel(titulo("Usuários por perfil"), porPerfil,
                        texto("Uma conta com vários perfis é contabilizada em cada perfil."), exportacao));
    }
}
