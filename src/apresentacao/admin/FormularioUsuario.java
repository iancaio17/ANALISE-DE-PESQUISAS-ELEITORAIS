package apresentacao.admin;

import static apresentacao.componentes.Componentes.aviso;
import static apresentacao.componentes.Componentes.campo;
import static apresentacao.componentes.Componentes.codigo;
import static apresentacao.componentes.Componentes.conteudo;
import static apresentacao.componentes.Componentes.entrada;
import static apresentacao.componentes.Componentes.grade;
import static apresentacao.componentes.Componentes.linha;
import static apresentacao.componentes.Componentes.titulo;

import aplicacao.servicos.AdministracaoService;
import apresentacao.componentes.Dialogos;
import apresentacao.componentes.Leitura;
import dominio.admin.Perfil;
import dominio.admin.Usuario;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Janela de cadastro e edição de usuário (inclui a atribuição de perfis). */
class FormularioUsuario {

    private final Window janela;
    private final AdministracaoService servico;
    private final Usuario usuario;

    private final TextField id;
    private final TextField nome = entrada("Nome fictício");
    private final TextField email = entrada("email@simulado.local");
    private final PasswordField senha = new PasswordField();
    private final CheckBox ativo = new CheckBox("Usuário ativo");
    private final Map<Perfil, CheckBox> perfis = new LinkedHashMap<>();

    /** @param usuario usuário a editar, ou {@code null} para cadastrar um novo */
    FormularioUsuario(Window janela, AdministracaoService servico, Usuario usuario) {
        this.janela = janela;
        this.servico = servico;
        this.usuario = usuario;
        this.id = codigo(usuario == null ? servico.proximoIdUsuario() : usuario.getId());
        preencherCampos();
    }

    private void preencherCampos() {
        senha.setPromptText(usuario == null ? "Mínimo de 8 caracteres" : "Deixe em branco para manter a senha");
        ativo.setSelected(usuario == null || usuario.isAtivo());
        for (Perfil perfil : Perfil.values()) {
            CheckBox caixa = new CheckBox(perfil.toString());
            caixa.setSelected(usuario != null && usuario.getPerfis().contains(perfil));
            perfis.put(perfil, caixa);
        }
        if (usuario != null) {
            nome.setText(usuario.getNome());
            email.setText(usuario.getEmail());
        }
    }

    void abrir() {
        Dialogos.formulario(janela, usuario == null ? "Cadastro de usuário" : "Editar usuário", montar(), this::salvar);
    }

    private Node montar() {
        GridPane grade = grade();
        campo(grade, 0, 0, "ID", id);
        campo(grade, 0, 1, "Nome", nome);
        campo(grade, 1, 0, "E-mail", email);
        campo(grade, 1, 1, "Senha", senha);
        return conteudo(grade, titulo("Perfis de acesso"), linha(perfis.values().toArray(Node[]::new)), ativo,
                aviso("Um usuário pode ter mais de um perfil. Mantenha pelo menos um administrador ativo."));
    }

    private void salvar() {
        Set<Perfil> escolhidos = perfis.entrySet().stream()
                .filter(par -> par.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        servico.salvarUsuario(Leitura.inteiro(id, "ID"), nome.getText(), email.getText(), senha.getText(),
                escolhidos, ativo.isSelected());
    }
}
