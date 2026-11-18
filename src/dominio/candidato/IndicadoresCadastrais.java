package dominio.candidato;

import dominio.pesquisa.Cargo;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Indicadores cadastrais de candidatos e partidos (totais, ativos, por cargo, por partido e média). */
public final class IndicadoresCadastrais {

    private final long totalCandidatos;
    private final long candidaturasAtivas;
    private final long totalPartidos;
    private final long partidosAtivos;
    private final long candidatosComPartido;
    private final Map<Cargo, Long> candidatosPorCargo = new EnumMap<>(Cargo.class);
    private final Map<Partido, Long> candidatosPorPartido = new LinkedHashMap<>();

    private IndicadoresCadastrais(List<Candidato> candidatos, List<Partido> partidos) {
        this.totalCandidatos = candidatos.size();
        this.candidaturasAtivas = candidatos.stream().filter(Candidato::isAtivo).count();
        this.totalPartidos = partidos.size();
        this.partidosAtivos = partidos.stream().filter(Partido::isAtivo).count();
        this.candidatosComPartido = candidatos.stream().filter(c -> c.getPartido() != null).count();
        for (Cargo cargo : Cargo.values()) {
            candidatosPorCargo.put(cargo, candidatos.stream().filter(c -> c.getCargo() == cargo).count());
        }
        for (Partido partido : partidos) {
            long quantidade = candidatos.stream().filter(c -> c.pertenceAoPartido(partido.getId())).count();
            candidatosPorPartido.put(partido, quantidade);
        }
    }

    public static IndicadoresCadastrais calcular(List<Candidato> candidatos, List<Partido> partidos) {
        return new IndicadoresCadastrais(candidatos, partidos);
    }

    public long getTotalCandidatos() {
        return totalCandidatos;
    }

    public long getCandidaturasAtivas() {
        return candidaturasAtivas;
    }

    public long getCandidaturasInativas() {
        return totalCandidatos - candidaturasAtivas;
    }

    public long getTotalPartidos() {
        return totalPartidos;
    }

    public long getPartidosAtivos() {
        return partidosAtivos;
    }

    public Map<Cargo, Long> getCandidatosPorCargo() {
        return candidatosPorCargo;
    }

    public Map<Partido, Long> getCandidatosPorPartido() {
        return candidatosPorPartido;
    }

    /** Candidatos vinculados a partidos divididos pelo total de partidos (zero se não há partidos). */
    public double getMediaCandidatosPorPartido() {
        return totalPartidos == 0 ? 0 : (double) candidatosComPartido / totalPartidos;
    }
}
