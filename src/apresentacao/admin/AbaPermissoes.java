package apresentacao.admin;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.tabela;
import static apresentacao.componentes.Componentes.titulo;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Leitura;
import dominio.admin.Usuario;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

/** Matriz de acesso por perfil e atalho para editar os perfis de um usuário. */
class AbaPermissoes extends Aba {

    /** Uma linha da matriz: funcionalidade e o nível de acesso de cada perfil. */
    private record Permissao(String funcionalidade, String administrador, String pesquisador, String analista) {
    }

    AbaPermissoes(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        TableView<Permissao> matriz = tabela();
        colunas(matriz,
                coluna("Funcionalidade", Permissao::funcionalidade),
                coluna("Administrador", Permissao::administrador),
                coluna("Pesquisador", Permissao::pesquisador),
                coluna("Analista", Permissao::analista));
        matriz.getItems().addAll(
                new Permissao("Usuários e parâmetros", "Total", "—", "—"),
                new Permissao("Pesquisas e questionário", "Total", "Gerenciar", "Consultar"),
                new Permissao("Coleta e amostra", "Total", "Gerenciar", "—"),
                new Permissao("Análises e relatórios", "Total", "—", "Gerenciar"),
                new Permissao("Candidatos e partidos", "Total", "Consultar", "Consultar"));

        ComboBox<Usuario> usuario = combo(administracao().listarUsuarios(), "Selecione o usuário");
        return pagina(
                aviso("As permissões são definidas pelos perfis. A conta pode combinar mais de um perfil."),
                painel(titulo("Matriz de acesso"), matriz),
                painel(titulo("Atribuir perfis"), linha(campo("Usuário", usuario),
                        botao("Editar perfis", () -> editarPerfis(Leitura.obrigatorio(usuario, "o usuário"))))));
    }

    private void editarPerfis(Usuario usuario) {
        new FormularioUsuario(janela(), administracao(), usuario).abrir();
        atualizar();
    }
}
