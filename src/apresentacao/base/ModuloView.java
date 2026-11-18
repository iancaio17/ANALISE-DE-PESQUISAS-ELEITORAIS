package apresentacao.base;

import apresentacao.componentes.Componentes;
import aplicacao.SistemaContexto;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

/**
 * Tela de um módulo: um conjunto de abas. O conteúdo de cada aba é montado só quando ela é aberta
 * e remontado por {@link #atualizar()} depois de qualquer alteração de dados.
 */
public abstract class ModuloView extends TabPane {

    private final SistemaContexto contexto;
    private final Map<Tab, Supplier<Aba>> abas = new LinkedHashMap<>();

    protected ModuloView(SistemaContexto contexto) {
        this.contexto = contexto;
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getSelectionModel().selectedItemProperty().addListener((observado, anterior, atual) -> {
            if (atual != null) {
                carregar(atual);
            }
        });
    }

    /** Registra uma aba. A fábrica é chamada a cada carga, para que a aba sempre reflita os dados atuais. */
    protected void adicionar(String titulo, Supplier<Aba> fabrica) {
        Tab tab = new Tab(titulo);
        abas.put(tab, fabrica);
        getTabs().add(tab);
        if (getTabs().size() == 1) {
            carregar(tab);
        }
    }

    private void carregar(Tab tab) {
        try {
            tab.setContent(abas.get(tab).get().construir());
        } catch (Exception e) {
            e.printStackTrace();
            tab.setContent(Componentes.pagina(Componentes.aviso(e.getMessage())));
        }
    }

    /** Recarrega a aba aberta, refletindo os dados mais recentes. */
    public void atualizar() {
        carregar(getSelectionModel().getSelectedItem());
    }

    public void abrirAba(int indice) {
        getSelectionModel().select(indice);
    }

    public SistemaContexto getContexto() {
        return contexto;
    }

    public Window janela() {
        return getScene() == null ? null : getScene().getWindow();
    }
}
