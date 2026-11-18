package apresentacao.candidatos;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.indicador;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.tabela;
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Leitura;
import dominio.candidato.IndicadoresCadastrais;
import dominio.candidato.Partido;
import dominio.pesquisa.Cargo;
import javafx.scene.Node;
import javafx.scene.control.TableView;

/** Totais de candidatos e partidos. */
class AbaIndicadores extends Aba {

    AbaIndicadores(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        IndicadoresCadastrais indicadores = candidatos().gerarIndicadores();

        TableView<Cargo> porCargo = tabela();
        colunas(porCargo,
                coluna("Cargo", cargo -> cargo),
                coluna("Candidatos", cargo -> indicadores.getCandidatosPorCargo().getOrDefault(cargo, 0L)));
        porCargo.getItems().setAll(Cargo.values());

        TableView<Partido> porPartido = tabela();
        colunas(porPartido,
                coluna("Partido", Partido::getSigla),
                coluna("Candidatos", partido -> indicadores.getCandidatosPorPartido().get(partido)));
        porPartido.getItems().setAll(indicadores.getCandidatosPorPartido().keySet());

        return pagina(
                linha(indicador(indicadores.getTotalCandidatos(), "Candidatos"),
                        indicador(indicadores.getCandidaturasAtivas(), "Candidaturas ativas"),
                        indicador(indicadores.getCandidaturasInativas(), "Candidaturas inativas"),
                        indicador(indicadores.getTotalPartidos(), "Partidos"),
                        indicador(indicadores.getPartidosAtivos(), "Partidos ativos"),
                        indicador(Leitura.percentual(indicadores.getMediaCandidatosPorPartido()),
                                "Média de candidatos por partido")),
                painel(titulo("Por cargo"), porCargo),
                painel(titulo("Por partido"), porPartido),
                aviso("Pré-candidato, Candidato e Validada contam como candidaturas ativas. Inativa é excluída. "
                        + "A média usa candidatos vinculados divididos pelo total de partidos."));
    }
}
