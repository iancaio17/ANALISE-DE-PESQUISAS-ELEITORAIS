package aplicacao.servicos;

import dominio.candidato.Candidato;
import dominio.candidato.Partido;
import java.util.ArrayList;
import java.util.List;

public class CandidatoService {

    private List<Candidato> bancoCandidatos;
    private List<Partido> bancoPartidos;

    public CandidatoService() {
        this.bancoCandidatos = new ArrayList<>();
        this.bancoPartidos = new ArrayList<>();
    }

    public void cadastrarPartido(Partido novoPartido) {
        if (novoPartido == null) throw new IllegalArgumentException("Partido não pode ser nulo.");

        for (Partido p : bancoPartidos) {
            if (p.getSigla().equalsIgnoreCase(novoPartido.getSigla()) || p.getNumero() == novoPartido.getNumero()) {
                throw new IllegalArgumentException("Erro: Já existe um partido cadastrado com este número ou sigla.");
            }
        }

        bancoPartidos.add(novoPartido);
        System.out.println("Sucesso: Partido " + novoPartido.getSigla() + " cadastrado.");
    }

    public void cadastrarCandidato(Candidato novoCandidato) {
        if (novoCandidato == null) throw new IllegalArgumentException("Candidato não pode ser nulo.");

        for (Candidato c : bancoCandidatos) {
            if (c.getNumeroIdentificador() == novoCandidato.getNumeroIdentificador() &&
                    c.getCargo().equalsIgnoreCase(novoCandidato.getCargo())) {
                throw new IllegalArgumentException("Erro: Já existe um candidato com o número " + novoCandidato.getNumeroIdentificador() + " para o cargo de " + novoCandidato.getCargo() + ".");
            }
        }

        bancoCandidatos.add(novoCandidato);
        System.out.println("Sucesso: Candidato " + novoCandidato.getNome() + " cadastrado.");
    }

    public void associarCandidatoPartido(int idCandidato, int numeroPartido) {
        Candidato candidatoEncontrado = null;
        Partido partidoEncontrado = null;

        for (Candidato c : bancoCandidatos) {
            if (c.getId() == idCandidato) candidatoEncontrado = c;
        }

        for (Partido p : bancoPartidos) {
            if (p.getNumero() == numeroPartido) partidoEncontrado = p;
        }

        if (candidatoEncontrado == null || partidoEncontrado == null) {
            throw new IllegalArgumentException("Erro: Candidato ou Partido não localizado no sistema.");
        }

        partidoEncontrado.adicionarCandidato(candidatoEncontrado);
        candidatoEncontrado.setPartido(partidoEncontrado);

        System.out.println("Associação concluída: " + candidatoEncontrado.getNome() + " agora pertence ao partido " + partidoEncontrado.getSigla() + ".");
    }

    public List<Candidato> getBancoCandidatos() { return bancoCandidatos; }
    public List<Partido> getBancoPartidos() { return bancoPartidos; }
}