package dominio.analise;

import dominio.admin.Perfil;
import dominio.admin.Usuario;
import java.util.ArrayList;
import java.util.List;

/** Relatório administrativo: usuários ativos, inativos e quantidade por perfil. */
public class RelatorioUsuarios extends Relatorio {

    private final List<Usuario> usuarios;

    public RelatorioUsuarios(List<Usuario> usuarios) {
        this.usuarios = List.copyOf(usuarios);
    }

    public long quantidadeAtivos() {
        return usuarios.stream().filter(Usuario::isAtivo).count();
    }

    public long quantidadeInativos() {
        return usuarios.size() - quantidadeAtivos();
    }

    /** Uma conta com vários perfis é contabilizada em cada um deles. */
    public long quantidadePorPerfil(Perfil perfil) {
        return usuarios.stream().filter(u -> u.possuiPerfil(perfil)).count();
    }

    @Override
    public String getTitulo() {
        return "Relatório de usuários";
    }

    @Override
    public List<String[]> gerarLinhas() {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[] {"RELATÓRIO DE USUÁRIOS · SISTEMA SIMULADO"});
        linhas.add(new String[] {"Ativos", String.valueOf(quantidadeAtivos())});
        linhas.add(new String[] {"Inativos", String.valueOf(quantidadeInativos())});
        linhas.add(new String[] {"Perfil", "Quantidade"});
        for (Perfil perfil : Perfil.values()) {
            linhas.add(new String[] {perfil.toString(), String.valueOf(quantidadePorPerfil(perfil))});
        }
        linhas.add(new String[] {"Nome", "E-mail", "Perfis", "Status"});
        for (Usuario usuario : usuarios) {
            linhas.add(new String[] {usuario.getNome(), usuario.getEmail(), usuario.getPerfis().toString(),
                    usuario.isAtivo() ? "Ativo" : "Inativo"});
        }
        return linhas;
    }
}
