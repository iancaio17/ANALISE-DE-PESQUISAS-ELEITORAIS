package apresentacao.pesquisas;

import static apresentacao.componentes.Componentes.combo;

import dominio.pesquisa.Pesquisa;
import java.util.List;
import javafx.scene.control.ComboBox;

/** Combo de pesquisas que lembra a pesquisa escolhida ao trocar de aba. */
final class SeletorPesquisa {

    private SeletorPesquisa() {
    }

    /**
     * Cria o combo já com a pesquisa em foco do módulo selecionada (ou a primeira da lista).
     * Ao trocar de pesquisa, guarda a escolha e recarrega a aba.
     */
    static ComboBox<Pesquisa> criar(ModuloPesquisas modulo, List<Pesquisa> pesquisas) {
        ComboBox<Pesquisa> seletor = combo(pesquisas, "Selecione a pesquisa");
        seletor.setPrefWidth(400);
        seletor.getItems().stream()
                .filter(p -> p.getCodigo().equals(modulo.getCodigoSelecionado()))
                .findFirst()
                .ifPresent(seletor::setValue);
        if (seletor.getValue() == null && !seletor.getItems().isEmpty()) {
            seletor.getSelectionModel().selectFirst();
        }
        if (seletor.getValue() != null) {
            modulo.setCodigoSelecionado(seletor.getValue().getCodigo());
        }
        seletor.setOnAction(evento -> {
            Pesquisa escolhida = seletor.getValue();
            modulo.setCodigoSelecionado(escolhida == null ? null : escolhida.getCodigo());
            modulo.atualizar();
        });
        return seletor;
    }
}
