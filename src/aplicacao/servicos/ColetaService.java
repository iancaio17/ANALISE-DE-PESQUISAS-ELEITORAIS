package aplicacao.servicos;

import dominio.coleta.Entrevista;
import dominio.pesquisa.Pesquisa;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ColetaService {

    private List<Entrevista> bancoEntrevistas;

    public ColetaService() {
        this.bancoEntrevistas = new ArrayList<>();
    }

    public void registrarEntrevista(Entrevista entrevista) {
        if (entrevista == null) throw new IllegalArgumentException("Entrevista não pode ser nula.");

        Pesquisa pesquisaAlvo = entrevista.getPesquisa();

        // CORREÇÃO: pesquisa "Processada" também não pode receber novas entrevistas
        if (pesquisaAlvo.getSituacao().equalsIgnoreCase("Encerrada")
                || pesquisaAlvo.getSituacao().equalsIgnoreCase("Processada")) {
            throw new IllegalStateException("Erro: Coleta encerrada. Esta pesquisa não aceita novas entrevistas.");
        }

        long entrevistasRealizadas = bancoEntrevistas.stream()
                .filter(e -> e.getPesquisa().getCodigo().equals(pesquisaAlvo.getCodigo()))
                .count();

        if (entrevistasRealizadas >= pesquisaAlvo.getQuantidadePrevista()) {
            throw new IllegalStateException("Erro: Limite atingido. A quantidade prevista de entrevistas já foi alcançada.");
        }

        entrevista.concluirEntrevista();

        bancoEntrevistas.add(entrevista);
        System.out.println("Sucesso: Entrevista " + entrevista.getIdUnico() + " registrada com sucesso.");
    }

    // NOVO: usado pelo AnaliseService (módulos conversam por serviços, não por repositórios)
    public List<Entrevista> getEntrevistasPorPesquisa(String codigoPesquisa) {
        return bancoEntrevistas.stream()
                .filter(e -> e.getPesquisa().getCodigo().equalsIgnoreCase(codigoPesquisa))
                .collect(Collectors.toList());
    }

    public List<Entrevista> getBancoEntrevistas() { return bancoEntrevistas; }
}
