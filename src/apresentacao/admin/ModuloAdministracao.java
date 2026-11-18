package apresentacao.admin;

import aplicacao.SistemaContexto;
import apresentacao.base.ModuloView;

/** Módulo Administração: usuários, permissões, parâmetros e relatório de usuários. */
public class ModuloAdministracao extends ModuloView {

    public ModuloAdministracao(SistemaContexto contexto) {
        super(contexto);
        adicionar("Usuários", () -> new AbaUsuarios(this));
        adicionar("Permissões", () -> new AbaPermissoes(this));
        adicionar("Parâmetros", () -> new AbaParametros(this));
        adicionar("Relatório de usuários", () -> new AbaRelatorioUsuarios(this));
    }
}
