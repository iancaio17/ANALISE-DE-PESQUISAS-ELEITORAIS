package aplicacao.servicos;

import dominio.pesquisa.Pesquisa;
import java.util.ArrayList;
import java.util.List;

public class PesquisaService {

    private List<Pesquisa> bancoPesquisas;

    public PesquisaService() {
        this.bancoPesquisas = new ArrayList<>();
    }

    public void cadastrarPesquisa(Pesquisa pesquisa) {
        if (pesquisa == null) throw new IllegalArgumentException("Pesquisa não pode ser nula.");

        if (pesquisa.getTipo().equalsIgnoreCase("Intenção de voto") && pesquisa.getCandidatosAssociados().isEmpty()) {
            throw new IllegalStateException("Erro: Pesquisas de intenção de voto devem possuir pelo menos um candidato associado.");
        }

        bancoPesquisas.add(pesquisa);
        System.out.println("Sucesso: Pesquisa '" + pesquisa.getCodigo() + " - " + pesquisa.getNome() + "' cadastrada no sistema.");
    }

    public Pesquisa buscarPesquisaPorCodigo(String codigo) {
        for (Pesquisa p : bancoPesquisas) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Erro: Pesquisa não encontrada.");
    }

    public List<Pesquisa> getBancoPesquisas() { return bancoPesquisas; }
}