package apresentacao.coleta;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.codigo;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.grade;

import aplicacao.SistemaContexto;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.TipoParametro;
import dominio.base.Genero;
import dominio.base.PerfilDemografico;
import dominio.coleta.Participante;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Janela de cadastro e edição de participante fictício e do seu perfil demográfico. */
class FormularioParticipante {

    private final Window janela;
    private final SistemaContexto contexto;
    private final TextField id;
    private final TextField nome = entrada("Identificação fictícia");
    private final ComboBox<String> faixaEtaria;
    private final ComboBox<String> regiao;
    private final ComboBox<String> escolaridade;
    private final ComboBox<String> renda;
    private final ComboBox<String> genero = combo(Genero.textos(), "Selecione");

    /** @param participante participante a editar, ou {@code null} para cadastrar um novo */
    FormularioParticipante(Window janela, SistemaContexto contexto, Participante participante) {
        this.janela = janela;
        this.contexto = contexto;
        this.id = codigo(participante == null ? contexto.getColeta().proximoIdParticipante() : participante.getId());
        this.faixaEtaria = combo(contexto.getAdministracao().valores(TipoParametro.FAIXA_ETARIA), "Selecione");
        this.regiao = combo(contexto.getAdministracao().valores(TipoParametro.REGIAO), "Selecione");
        this.escolaridade = combo(contexto.getAdministracao().valores(TipoParametro.ESCOLARIDADE), "Selecione");
        this.renda = combo(contexto.getAdministracao().valores(TipoParametro.FAIXA_RENDA), "Selecione");
        if (participante != null) {
            preencher(participante);
        }
    }

    private void preencher(Participante participante) {
        PerfilDemografico perfil = participante.getPerfilDemografico();
        nome.setText(participante.getNome());
        faixaEtaria.setValue(perfil.getFaixaEtaria());
        regiao.setValue(perfil.getRegiao());
        escolaridade.setValue(perfil.getEscolaridade());
        renda.setValue(perfil.getFaixaRenda());
        genero.setValue(perfil.getGenero());
    }

    void abrir() {
        GridPane grade = grade();
        campo(grade, 0, 0, "Código numérico", id);
        campo(grade, 0, 1, "Nome fictício", nome);
        campo(grade, 1, 0, "Faixa etária", faixaEtaria);
        campo(grade, 1, 1, "Região", regiao);
        campo(grade, 2, 0, "Escolaridade", escolaridade);
        campo(grade, 2, 1, "Renda", renda);
        campo(grade, 3, 0, "Gênero", genero);
        Dialogos.formulario(janela, "Participante fictício",
                conteudo(grade, aviso("Use apenas dados inventados para este projeto acadêmico.")), this::salvar);
    }

    private void salvar() {
        PerfilDemografico perfil = new PerfilDemografico(
                Leitura.obrigatorio(faixaEtaria, "a faixa etária"),
                Leitura.obrigatorio(regiao, "a região"),
                Leitura.obrigatorio(escolaridade, "a escolaridade"),
                Leitura.obrigatorio(renda, "a renda"),
                Leitura.obrigatorio(genero, "o gênero"));
        contexto.getColeta().salvarParticipante(Leitura.inteiro(id, "Código"), nome.getText(), perfil);
    }
}
