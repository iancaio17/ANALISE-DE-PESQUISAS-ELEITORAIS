package aplicacao.servicos;

import dominio.coleta.Entrevista;
import dominio.coleta.Entrevistador;
import dominio.coleta.Participante;
import dominio.pesquisa.Pesquisa;
import persistencia.repositorios.ColetaArquivoRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ColetaService {

    private final ColetaArquivoRepository repository;

    public ColetaService() {
        this.repository = ColetaArquivoRepository.getInstance();
    }

    public void registrarEntrevista(Entrevista entrevista) {
        if (entrevista == null) {
            throw new IllegalArgumentException("Erro: A entrevista não pode ser nula.");
        }

        Pesquisa pesquisaAlvo = entrevista.getPesquisa();
        if (pesquisaAlvo == null) {
            throw new IllegalArgumentException("Erro: A entrevista deve estar vinculada a uma pesquisa válida.");
        }

        if ("Encerrada".equalsIgnoreCase(pesquisaAlvo.getSituacao())) {
            throw new IllegalStateException("Erro: Coleta encerrada. Esta pesquisa não aceita novas entrevistas.");
        }

        List<Entrevista> todasEntrevistas = repository.listarEntrevistas();

        long entrevistasRealizadas = todasEntrevistas.stream()
                .filter(e -> e.getPesquisa().getCodigo().equals(pesquisaAlvo.getCodigo()))
                .count();

        if (entrevistasRealizadas >= pesquisaAlvo.getQuantidadePrevista()) {
            throw new IllegalStateException("Erro: Limite atingido. A quantidade prevista de entrevistas ("
                    + pesquisaAlvo.getQuantidadePrevista() + ") já foi alcançada.");
        }

        if (entrevista.getParticipante() != null) {
            boolean participanteRepetido = todasEntrevistas.stream()
                    .filter(e -> e.getPesquisa().getCodigo().equals(pesquisaAlvo.getCodigo()))
                    .anyMatch(e -> e.getParticipante() != null &&
                            e.getParticipante().equals(entrevista.getParticipante()));

            if (participanteRepetido) {
                throw new IllegalStateException("Erro: Este participante já respondeu a esta pesquisa.");
            }
        }

        entrevista.concluirEntrevista();
        repository.salvarEntrevista(entrevista);

        System.out.println("Sucesso: Entrevista " + entrevista.getIdUnico() + " registrada e salva em arquivo.");
    }

    public void cadastrarEntrevistador(Entrevistador entrevistador) {
        if (entrevistador == null) {
            throw new IllegalArgumentException("Erro: O entrevistador não pode ser nulo.");
        }
        repository.salvarEntrevistador(entrevistador);
    }

    public List<Entrevistador> listarEntrevistadores() {
        return repository.listarEntrevistadores();
    }

    public void cadastrarParticipante(Participante participante) {
        if (participante == null) {
            throw new IllegalArgumentException("Erro: O participante não pode ser nulo.");
        }
        repository.salvarParticipante(participante);
    }

    public List<Participante> listarParticipantes() {
        return repository.listarParticipantes();
    }

    public List<Entrevista> getBancoEntrevistas() {
        return repository.listarEntrevistas();
    }

    public List<Entrevista> buscarEntrevistasPorPesquisa(String codigoPesquisa) {
        return repository.listarEntrevistas().stream()
                .filter(e -> e.getPesquisa().getCodigo().equals(codigoPesquisa))
                .collect(Collectors.toList());
    }

    public List<Entrevista> buscarEntrevistasPorEntrevistador(Entrevistador entrevistador) {
        return repository.listarEntrevistas().stream()
                .filter(e -> entrevistador.equals(e.getEntrevistador()))
                .collect(Collectors.toList());
    }

    public double calcularProgressoColeta(Pesquisa pesquisa) {
        if (pesquisa == null || pesquisa.getQuantidadePrevista() <= 0) {
            return 0.0;
        }
        long coletadas = buscarEntrevistasPorPesquisa(pesquisa.getCodigo()).size();
        return Math.min(100.0, (coletadas * 100.0) / pesquisa.getQuantidadePrevista());
    }

    public void limparBancoDeColeta() {
        repository.limparTudo();
    }
}