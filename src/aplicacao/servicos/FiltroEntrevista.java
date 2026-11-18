package aplicacao.servicos;

import dominio.base.PerfilDemografico;
import dominio.coleta.Entrevista;
import dominio.coleta.Resposta;
import dominio.pesquisa.Pergunta;
import infraestrutura.utils.Validacao;
import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Filtros dos relatórios: características da amostra, período das entrevistas e candidato (opção)
 * escolhido em uma pergunta. Critério não informado não filtra.
 */
public class FiltroEntrevista implements Predicate<Entrevista> {

    private PerfilDemografico perfil = new PerfilDemografico("", "", "", "", "");
    private LocalDate inicio;
    private LocalDate fim;
    private String opcaoEscolhida = "";
    private Pergunta pergunta;

    public FiltroEntrevista perfil(PerfilDemografico perfil) {
        this.perfil = perfil;
        return this;
    }

    public FiltroEntrevista periodo(LocalDate inicio, LocalDate fim) {
        Validacao.exigir(inicio == null || fim == null || !fim.isBefore(inicio), "Período de filtro inválido.");
        this.inicio = inicio;
        this.fim = fim;
        return this;
    }

    /** Mantém só as entrevistas que escolheram a opção (ex.: nome do candidato) na pergunta. */
    public FiltroEntrevista opcaoEscolhida(Pergunta pergunta, String opcao) {
        this.pergunta = pergunta;
        this.opcaoEscolhida = opcao == null ? "" : opcao;
        return this;
    }

    public PerfilDemografico getPerfil() {
        return perfil;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public String getOpcaoEscolhida() {
        return opcaoEscolhida;
    }

    /** Verdadeiro quando nenhum critério foi informado (a análise salva pode ser usada como está). */
    public boolean estaVazio() {
        return perfil.equals(new PerfilDemografico("", "", "", "", ""))
                && inicio == null && fim == null && opcaoEscolhida.isEmpty();
    }

    @Override
    public boolean test(Entrevista entrevista) {
        return perfil.corresponde(entrevista.getParticipante().getPerfilDemografico())
                && dentroDoPeriodo(entrevista)
                && escolheuOpcao(entrevista);
    }

    private boolean dentroDoPeriodo(Entrevista entrevista) {
        LocalDate dia = entrevista.getData().toLocalDate();
        return (inicio == null || !dia.isBefore(inicio)) && (fim == null || !dia.isAfter(fim));
    }

    private boolean escolheuOpcao(Entrevista entrevista) {
        if (opcaoEscolhida.isEmpty()) {
            return true;
        }
        return entrevista.buscarResposta(pergunta)
                .map(Resposta::getOpcoesEscolhidas)
                .map(opcoes -> opcoes.stream().anyMatch(opcaoEscolhida::equalsIgnoreCase))
                .orElse(false);
    }
}
