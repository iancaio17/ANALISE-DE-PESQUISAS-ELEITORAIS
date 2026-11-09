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

        // CORREÇÃO: pesquisa "Processada" também não pode receber novas entrevistas
        if (pesquisaAlvo.getSituacao().equalsIgnoreCase("Encerrada")
                || pesquisaAlvo.getSituacao().equalsIgnoreCase("Processada")) {
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

    // NOVO: usado pelo AnaliseService (módulos conversam por serviços, não por repositórios)
    public List<Entrevista> getEntrevistasPorPesquisa(String codigoPesquisa) {
        return bancoEntrevistas.stream()
                .filter(e -> e.getPesquisa().getCodigo().equalsIgnoreCase(codigoPesquisa))
                .collect(Collectors.toList());
    }

    public List<Entrevista> getBancoEntrevistas() { return bancoEntrevistas; }
}
