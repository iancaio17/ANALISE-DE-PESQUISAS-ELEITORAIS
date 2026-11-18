package apresentacao.coleta;

import aplicacao.SistemaContexto;
import apresentacao.base.ModuloView;

/** Módulo Coleta e Amostra: cadastros de apoio, registro de entrevistas e consulta. */
public class ModuloColeta extends ModuloView {

    public static final int ABA_REGISTRO = 1;

    public ModuloColeta(SistemaContexto contexto) {
        super(contexto);
        adicionar("Configuração da amostra", () -> new AbaConfiguracaoAmostra(this));
        adicionar("Registro de respostas", () -> new AbaRegistroRespostas(this));
        adicionar("Consulta da coleta", () -> new AbaConsultaColeta(this));
    }
}
