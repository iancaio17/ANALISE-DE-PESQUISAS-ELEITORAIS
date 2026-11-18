package apresentacao.coleta;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.secundario;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.coleta.Entrevistador;
import dominio.coleta.Participante;
import dominio.pesquisa.Pesquisa;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Registro de uma entrevista: escolhe pesquisa, entrevistador e participante e responde o questionário. */
class AbaRegistroRespostas extends Aba {

    AbaRegistroRespostas(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        ComboBox<Pesquisa> pesquisa = combo(pesquisas().listarEmColeta(), "Pesquisa em coleta");
        pesquisa.setPrefWidth(350);
        ComboBox<Entrevistador> entrevistador = combo(coleta().listarEntrevistadores(), "Entrevistador");
        ComboBox<Participante> participante = combo(List.<Participante>of(), "Participante disponível");
        TextField numero = entrada("");
        numero.setEditable(false);
        numero.setPrefWidth(110);
        VBox areaQuestionario = new VBox(16);

        pesquisa.setOnAction(evento -> carregarParticipantes(pesquisa.getValue(), participante, numero));

        Button iniciar = botao("Iniciar entrevista", () -> {
            Pesquisa escolhida = Leitura.obrigatorio(pesquisa, "a pesquisa");
            Participante entrevistado = Leitura.obrigatorio(participante, "o participante");
            Entrevistador responsavel = Leitura.obrigatorio(entrevistador, "o entrevistador");
            pesquisa.setDisable(true);
            participante.setDisable(true);
            entrevistador.setDisable(true);
            mostrarQuestionario(areaQuestionario, escolhida, entrevistado, responsavel, numero);
        });
        iniciar.disableProperty().bind(pesquisa.disabledProperty());

        return pagina(
                painel(linha(campo("Pesquisa", pesquisa), campo("Entrevistador", entrevistador),
                        campo("Participante", participante), campo("Entrevista nº", numero), iniciar)),
                areaQuestionario,
                aviso("A entrevista será salva apenas ao concluir. Cada participante fictício pode participar de "
                        + "uma entrevista. Cadastre novos participantes na aba Configuração da amostra."));
    }

    private void carregarParticipantes(Pesquisa pesquisa, ComboBox<Participante> participante, TextField numero) {
        if (pesquisa == null) {
            return;
        }
        participante.getItems().setAll(coleta().participantesDisponiveis(pesquisa.getCodigo()));
        participante.setValue(null);
        numero.setText(String.valueOf(coleta().proximoNumero(pesquisa.getCodigo())));
    }

    private void mostrarQuestionario(VBox area, Pesquisa pesquisa, Participante participante,
            Entrevistador entrevistador, TextField numero) {
        QuestionarioEntrevista questionario = new QuestionarioEntrevista(pesquisa);
        Button concluir = botao("Concluir entrevista", () -> {
            coleta().registrarEntrevista(pesquisa.getCodigo(), Leitura.inteiro(numero, "Número da entrevista"),
                    participante.getId(), entrevistador.getId(), questionario.coletarRespostas());
            atualizar();
            Dialogos.sucesso("Entrevista registrada. Os dados foram salvos.");
        });
        area.getChildren().setAll(questionario, linha(concluir, secundario("Cancelar entrevista", this::atualizar)));
    }
}
