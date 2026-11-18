package aplicacao.servicos;

import dominio.admin.TipoParametro;
import dominio.base.Genero;
import dominio.base.PerfilDemografico;
import infraestrutura.utils.Validacao;
import persistencia.EstadoSistema;

/** Confere se as categorias de um perfil demográfico existem nos parâmetros cadastrados. */
final class ValidadorPerfil {

    private ValidadorPerfil() {
    }

    /**
     * @param parcial se verdadeiro, campos vazios são aceitos (uso em metas, onde vazio = qualquer categoria)
     */
    static void validar(EstadoSistema estado, PerfilDemografico perfil, boolean parcial) {
        Validacao.exigir(perfil != null, "Perfil demográfico obrigatório.");
        for (TipoParametro tipo : TipoParametro.values()) {
            String valor = valorDe(perfil, tipo);
            boolean vazioPermitido = parcial && valor.isBlank();
            boolean cadastrado = estado.getParametros().stream()
                    .anyMatch(p -> p.getTipo() == tipo && p.getValor().equals(valor));
            Validacao.exigir(vazioPermitido || cadastrado, "Categoria não cadastrada para " + tipo + ": " + valor);
        }
        boolean generoValido = parcial && perfil.getGenero().isBlank() || Genero.textos().contains(perfil.getGenero());
        Validacao.exigir(generoValido, "Gênero inválido.");
    }

    /** Valor do perfil correspondente a um tipo de parâmetro. */
    static String valorDe(PerfilDemografico perfil, TipoParametro tipo) {
        switch (tipo) {
            case FAIXA_ETARIA:
                return perfil.getFaixaEtaria();
            case ESCOLARIDADE:
                return perfil.getEscolaridade();
            case REGIAO:
                return perfil.getRegiao();
            default:
                return perfil.getFaixaRenda();
        }
    }
}
