package persistencia;

import dominio.admin.Parametro;
import dominio.admin.Usuario;
import dominio.analise.Analise;
import dominio.candidato.Candidato;
import dominio.candidato.Partido;
import dominio.coleta.Entrevista;
import dominio.coleta.Entrevistador;
import dominio.coleta.Participante;
import dominio.pesquisa.Pesquisa;
import infraestrutura.excecoes.RegraNegocioException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Conjunto de todos os dados do sistema, gravado em um único arquivo.
 * As listas devolvidas são as do próprio estado: só os serviços, dentro de uma transação do
 * repositório, devem alterá-las. A interface gráfica recebe sempre cópias.
 */
public final class EstadoSistema implements Serializable {

    private static final long serialVersionUID = 2L;

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Parametro> parametros = new ArrayList<>();
    private final List<Partido> partidos = new ArrayList<>();
    private final List<Candidato> candidatos = new ArrayList<>();
    private final List<Pesquisa> pesquisas = new ArrayList<>();
    private final List<Entrevistador> entrevistadores = new ArrayList<>();
    private final List<Participante> participantes = new ArrayList<>();
    private final List<Entrevista> entrevistas = new ArrayList<>();
    private final Map<String, Analise> analises = new LinkedHashMap<>();
    private boolean demonstracaoCarregada;

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Parametro> getParametros() {
        return parametros;
    }

    public List<Partido> getPartidos() {
        return partidos;
    }

    public List<Candidato> getCandidatos() {
        return candidatos;
    }

    public List<Pesquisa> getPesquisas() {
        return pesquisas;
    }

    public List<Entrevistador> getEntrevistadores() {
        return entrevistadores;
    }

    public List<Participante> getParticipantes() {
        return participantes;
    }

    public List<Entrevista> getEntrevistas() {
        return entrevistas;
    }

    /** Análises salvas, indexadas pelo código da pesquisa. */
    public Map<String, Analise> getAnalises() {
        return analises;
    }

    public boolean isDemonstracaoCarregada() {
        return demonstracaoCarregada;
    }

    public void setDemonstracaoCarregada(boolean demonstracaoCarregada) {
        this.demonstracaoCarregada = demonstracaoCarregada;
    }

    // ------------------------------------------------------------------ buscas

    public Optional<Usuario> buscarUsuario(int id) {
        return usuarios.stream().filter(u -> u.getId() == id).findFirst();
    }

    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        String procurado = email == null ? "" : email.trim();
        return usuarios.stream().filter(u -> u.getEmail().equalsIgnoreCase(procurado)).findFirst();
    }

    public Optional<Parametro> buscarParametro(int id) {
        return parametros.stream().filter(p -> p.getId() == id).findFirst();
    }

    public Optional<Partido> buscarPartido(String id) {
        return partidos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Optional<Candidato> buscarCandidato(int id) {
        return candidatos.stream().filter(c -> c.getId() == id).findFirst();
    }

    public Optional<Participante> buscarParticipante(int id) {
        return participantes.stream().filter(p -> p.getId() == id).findFirst();
    }

    public Optional<Entrevistador> buscarEntrevistador(int id) {
        return entrevistadores.stream().filter(e -> e.getId() == id).findFirst();
    }

    /** Procura a pesquisa pelo código; lança exceção de regra de negócio se não existir. */
    public Pesquisa exigirPesquisa(String codigo) {
        return pesquisas.stream()
                .filter(p -> p.getCodigo().equalsIgnoreCase(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("Pesquisa não encontrada."));
    }

    public List<Entrevista> entrevistasDaPesquisa(String codigo) {
        return entrevistas.stream().filter(e -> e.getPesquisa().getCodigo().equals(codigo)).toList();
    }

    /** Pesquisas que já associaram o candidato (a associação impede a edição ou exclusão dele). */
    public boolean candidatoEstaEmPesquisa(int idCandidato) {
        return pesquisas.stream().anyMatch(p -> p.possuiCandidato(idCandidato));
    }
}
