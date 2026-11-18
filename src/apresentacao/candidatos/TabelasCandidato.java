package apresentacao.candidatos;

import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.tabela;

import dominio.candidato.Candidato;
import javafx.scene.control.TableView;

/** Tabela de candidatos usada em mais de uma aba. */
final class TabelasCandidato {

    private TabelasCandidato() {
    }

    static TableView<Candidato> criarTabelaCandidatos() {
        TableView<Candidato> tabela = tabela();
        colunas(tabela,
                coluna("Nome", Candidato::getNome),
                coluna("Número", Candidato::getNumeroIdentificador),
                coluna("Cargo", Candidato::getCargo),
                coluna("Partido", c -> c.getPartido() == null ? "Sem partido" : c.getPartido().getSigla()),
                coluna("Situação", Candidato::getSituacao));
        return tabela;
    }
}
