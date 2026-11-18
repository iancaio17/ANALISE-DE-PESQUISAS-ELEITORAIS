package apresentacao.pesquisas;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.filtro;
import static apresentacao.componentes.Componentes.grade;

import aplicacao.SistemaContexto;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.TipoParametro;
import dominio.base.Genero;
import dominio.base.PerfilDemografico;
import dominio.pesquisa.Pesquisa;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Janela que define a meta de entrevistas de um grupo da amostra. "Todas" vale para qualquer categoria. */
class FormularioMeta {

    private final Window janela;
    private final SistemaContexto contexto;
    private final Pesquisa pesquisa;

    private final ComboBox<String> regiao;
    private final ComboBox<String> faixaEtaria;
    private final ComboBox<String> escolaridade;
    private final ComboBox<String> renda;
    private final ComboBox<String> genero = filtro(Genero.textos());
    private final TextField quantidade = entrada("Quantidade mínima");

    FormularioMeta(Window janela, SistemaContexto contexto, Pesquisa pesquisa) {
        this.janela = janela;
        this.contexto = contexto;
        this.pesquisa = pesquisa;
        this.regiao = filtro(pesquisa.getRegioes());
        this.faixaEtaria = filtro(contexto.getAdministracao().valores(TipoParametro.FAIXA_ETARIA));
        this.escolaridade = filtro(contexto.getAdministracao().valores(TipoParametro.ESCOLARIDADE));
        this.renda = filtro(contexto.getAdministracao().valores(TipoParametro.FAIXA_RENDA));
    }

    void abrir() {
        GridPane grade = grade();
        campo(grade, 0, 0, "Região", regiao);
        campo(grade, 0, 1, "Faixa etária", faixaEtaria);
        campo(grade, 1, 0, "Escolaridade", escolaridade);
        campo(grade, 1, 1, "Renda", renda);
        campo(grade, 2, 0, "Gênero", genero);
        campo(grade, 2, 1, "Meta", quantidade);
        Dialogos.formulario(janela, "Meta do plano amostral",
                conteudo(grade, aviso("Todas funciona como qualquer categoria. "
                        + "Repetir o mesmo grupo atualiza sua meta.")),
                this::salvar);
    }

    private void salvar() {
        PerfilDemografico grupo = new PerfilDemografico(Leitura.valorFiltro(faixaEtaria),
                Leitura.valorFiltro(regiao), Leitura.valorFiltro(escolaridade), Leitura.valorFiltro(renda),
                Leitura.valorFiltro(genero));
        contexto.getPesquisas().salvarMeta(pesquisa.getCodigo(), grupo, Leitura.inteiro(quantidade, "Meta"));
    }
}
