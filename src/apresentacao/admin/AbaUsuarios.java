package apresentacao.admin;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.botao;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.coluna;
import static apresentacao.componentes.Componentes.colunas;
import static apresentacao.componentes.Componentes.combo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.pagina;
import static apresentacao.componentes.Componentes.painel;
import static apresentacao.componentes.Componentes.perigo;
import static apresentacao.componentes.Componentes.secundario;
import static apresentacao.componentes.Componentes.tabela;

import apresentacao.base.Aba;
import apresentacao.base.ModuloView;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Perfil;
import dominio.admin.Usuario;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** Cadastro, busca, ativação e exclusão de usuários. */
class AbaUsuarios extends Aba {

    private static final String ATIVOS = "Ativos";
    private static final String INATIVOS = "Inativos";

    AbaUsuarios(ModuloView modulo) {
        super(modulo);
    }

    @Override
    public Node construir() {
        TextField busca = entrada("Buscar por nome");
        ComboBox<Perfil> perfil = combo(Perfil.values(), "Todos os perfis");
        ComboBox<String> situacao = combo(List.of(ATIVOS, INATIVOS), "Todos os status");
        TableView<Usuario> tabela = criarTabela();

        Runnable filtrar = () -> tabela.getItems().setAll(
                administracao().buscarUsuarios(busca.getText(), perfil.getValue(), ativoSelecionado(situacao)));
        filtrar.run();
        Runnable limpar = () -> {
            busca.clear();
            perfil.setValue(null);
            situacao.setValue(null);
            filtrar.run();
        };

        Button novo = botao("Novo usuário", () -> editar(null));
        novo.setId("novo-usuario");
        return pagina(
                painel(linha(campo("Nome", busca), campo("Perfil", perfil), campo("Status", situacao),
                        botao("Filtrar", filtrar), secundario("Limpar", limpar))),
                painel(linha(novo,
                        secundario("Editar", () -> editar(Leitura.selecionado(tabela))),
                        secundario("Ativar / desativar", () -> alternarSituacao(Leitura.selecionado(tabela))),
                        perigo("Excluir", () -> excluir(Leitura.selecionado(tabela)))), tabela),
                aviso("Senhas precisam de 8 caracteres, uma letra e um número. Contas inativas não conseguem entrar."));
    }

    private TableView<Usuario> criarTabela() {
        TableView<Usuario> tabela = tabela();
        tabela.setId("tabela-usuarios");
        colunas(tabela,
                coluna("ID", Usuario::getId),
                coluna("Nome", Usuario::getNome),
                coluna("E-mail", Usuario::getEmail),
                coluna("Perfis", Usuario::getPerfis),
                coluna("Situação", usuario -> usuario.isAtivo() ? "Ativo" : "Inativo"));
        return tabela;
    }

    /** Converte o combo de situação em filtro: nulo (todos), verdadeiro (ativos) ou falso (inativos). */
    private static Boolean ativoSelecionado(ComboBox<String> situacao) {
        return situacao.getValue() == null ? null : situacao.getValue().equals(ATIVOS);
    }

    private void editar(Usuario usuario) {
        new FormularioUsuario(janela(), administracao(), usuario).abrir();
        atualizar();
    }

    private void alternarSituacao(Usuario usuario) {
        administracao().salvarUsuario(usuario.getId(), usuario.getNome(), usuario.getEmail(), "",
                usuario.getPerfis(), !usuario.isAtivo());
        atualizar();
    }

    private void excluir(Usuario usuario) {
        if (Dialogos.confirmar("Excluir o usuário " + usuario.getNome() + "?")) {
            administracao().excluirUsuario(usuario.getId());
            atualizar();
        }
    }
}
