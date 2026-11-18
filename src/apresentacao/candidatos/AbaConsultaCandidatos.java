package apresentacao.candidatos;

import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.secundario;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import dominio.candidato.Candidato;
import dominio.candidato.Partido;
import dominio.candidato.SituacaoCandidatura;
import dominio.pesquisa.Cargo;
import dominio.pesquisa.Pesquisa;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** Consulta de candidatos combinando nome, cargo, partido, situação e pesquisa. */
class AbaConsultaCandidatos extends Aba {

    AbaConsultaCandidatos(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        TextField nome = entrada("Nome do candidato");
        ComboBox<Cargo> cargo = combo(Cargo.values(), "Todos os cargos");
        ComboBox<Partido> partido = combo(candidatos().getBancoPartidos(), "Todos os partidos");
        ComboBox<SituacaoCandidatura> situacao = combo(SituacaoCandidatura.values(), "Todas as situações");
        ComboBox<Pesquisa> pesquisa = combo(pesquisas().listarTodas(), "Todas as pesquisas");
        TableView<Candidato> tabela = TabelasCandidato.criarTabelaCandidatos();

        Runnable filtrar = () -> tabela.getItems().setAll(candidatos().consultarCandidatos(
                nome.getText(), cargo.getValue(), partido.getValue(), situacao.getValue(), pesquisa.getValue()));
        filtrar.run();

        return pagina(
                painel(linha(campo("Nome", nome), campo("Cargo", cargo), campo("Partido", partido),
                        campo("Situação", situacao), campo("Pesquisa", pesquisa),
                        botao("Filtrar", filtrar), secundario("Limpar filtros", this::atualizar))),
                painel(tabela));
    }
}
