package apresentacao;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.indicador;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.secundario;
import static apresentacao.componentes.Componentes.texto;
import static apresentacao.componentes.Componentes.titulo;

import aplicacao.SistemaContexto;
import aplicacao.servicos.DadosDemonstracao;
import apresentacao.componentes.Dialogos;
import dominio.admin.Perfil;
import dominio.pesquisa.Pesquisa;
import dominio.pesquisa.SituacaoPesquisa;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/** Página inicial: indicadores gerais e atalhos conforme os perfis do usuário. */
class TelaInicio {

    private final SistemaContexto contexto;
    private final Navegador navegador;

    TelaInicio(SistemaContexto contexto, Navegador navegador) {
        this.contexto = contexto;
        this.navegador = navegador;
    }

    ScrollPane construir() {
        return pagina(
                titulo("Olá, " + contexto.getUsuarioLogado().getNome()),
                texto("Acompanhe suas pesquisas e continue de onde parou."),
                montarIndicadores(),
                montarAtalhos(),
                aviso("Projeto didático · Utilize somente participantes, candidatos, partidos e respostas fictícios."));
    }

    private Node montarIndicadores() {
        List<Pesquisa> pesquisas = contexto.getPesquisas().listarTodas();
        return linha(
                indicador(pesquisas.size(), "Pesquisas cadastradas"),
                indicador(contar(pesquisas, SituacaoPesquisa.EM_COLETA), "Pesquisas em coleta"),
                indicador(contar(pesquisas, SituacaoPesquisa.PROCESSADA), "Pesquisas processadas"),
                indicador(pesquisas.stream().mapToInt(Pesquisa::getQuantidadeRealizada).sum(),
                        "Entrevistas concluídas"));
    }

    private static long contar(List<Pesquisa> pesquisas, SituacaoPesquisa situacao) {
        return pesquisas.stream().filter(p -> p.getSituacao() == situacao).count();
    }

    private VBox montarAtalhos() {
        VBox atalhos = painel(titulo("Seu espaço de trabalho"),
                texto("Acesse os módulos no menu lateral. As opções seguem os perfis da sua conta."));
        atalhos.getChildren().add(botao("Consultar pesquisas", navegador::abrirPesquisas));
        if (contexto.permite(Perfil.PESQUISADOR)) {
            atalhos.getChildren().add(botao("Registrar uma entrevista", navegador::abrirRegistroDeEntrevista));
        }
        if (contexto.permite(Perfil.ANALISTA)) {
            atalhos.getChildren().add(botao("Analisar resultados", navegador::abrirAnalise));
        }
        if (contexto.permite(Perfil.ADMINISTRADOR)) {
            atalhos.getChildren().add(secundario("Carregar demonstração fictícia", this::carregarDemonstracao));
        }
        return atalhos;
    }

    private void carregarDemonstracao() {
        if (Dialogos.confirmar("Adicionar usuários e dados fictícios de demonstração? "
                + "Disponível antes de cadastrar pesquisas, candidatos e partidos.")) {
            DadosDemonstracao.carregar(contexto);
            navegador.recarregarInicio();
        }
    }
}
