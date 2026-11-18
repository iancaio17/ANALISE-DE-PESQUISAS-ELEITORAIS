package apresentacao.base;

import aplicacao.SistemaContexto;
import aplicacao.servicos.AdministracaoService;
import aplicacao.servicos.AnaliseService;
import aplicacao.servicos.CandidatoService;
import aplicacao.servicos.ColetaService;
import aplicacao.servicos.PesquisaService;
import dominio.admin.Perfil;
import javafx.scene.Node;
import javafx.stage.Window;

/**
 * Uma aba de um módulo. As subclasses montam a tela em {@link #construir()}. Uma nova instância é criada
 * a cada carga da aba, então os campos da subclasse sempre começam com os dados atuais.
 */
public abstract class Aba {

    protected final SistemaContexto contexto;
    private final ModuloView modulo;

    protected Aba(ModuloView modulo) {
        this.modulo = modulo;
        this.contexto = modulo.getContexto();
    }

    /** Monta o conteúdo da aba com os dados atuais. */
    public abstract Node construir();

    protected ModuloView getModulo() {
        return modulo;
    }

    protected void atualizar() {
        modulo.atualizar();
    }

    protected Window janela() {
        return modulo.janela();
    }

    protected boolean permite(Perfil perfil) {
        return contexto.permite(perfil);
    }

    protected AdministracaoService administracao() {
        return contexto.getAdministracao();
    }

    protected PesquisaService pesquisas() {
        return contexto.getPesquisas();
    }

    protected ColetaService coleta() {
        return contexto.getColeta();
    }

    protected CandidatoService candidatos() {
        return contexto.getCandidatos();
    }

    protected AnaliseService analise() {
        return contexto.getAnalise();
    }
}
