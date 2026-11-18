package dominio.admin;

import dominio.base.Pessoa;
import infraestrutura.utils.Senhas;
import infraestrutura.utils.Validacao;
import java.util.Locale;
import java.util.Set;

/** Usuário do sistema. Um usuário pode ter um ou mais perfis e só acessa se estiver ativo. */
public class Usuario extends Pessoa {

    private static final long serialVersionUID = 1L;
    private static final String FORMATO_EMAIL = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+";

    private String email;
    private String senhaCriptografada;
    private Set<Perfil> perfis;
    private boolean ativo = true;

    public Usuario(int id, String nome, String email, String senha, Set<Perfil> perfis) {
        super(id, nome);
        setEmail(email);
        setSenha(senha);
        setPerfis(perfis);
    }

    public String getEmail() {
        return email;
    }

    /** Devolve o hash da senha (nunca o texto digitado). */
    public String getSenha() {
        return senhaCriptografada;
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean possuiPerfil(Perfil perfil) {
        return perfis.contains(perfil);
    }

    public boolean isAdministrador() {
        return possuiPerfil(Perfil.ADMINISTRADOR);
    }

    public void setEmail(String email) {
        String normalizado = Validacao.texto(email, "E-mail").toLowerCase(Locale.ROOT);
        Validacao.exigir(normalizado.matches(FORMATO_EMAIL), "Formato de e-mail inválido.");
        this.email = normalizado;
    }

    /** Valida a política de senhas e guarda apenas o hash. */
    public void setSenha(String senha) {
        this.senhaCriptografada = Senhas.gerar(senha);
    }

    public void setPerfis(Set<Perfil> perfis) {
        Validacao.exigir(perfis != null && !perfis.isEmpty(), "O usuário deve possuir pelo menos um perfil.");
        this.perfis = Set.copyOf(perfis);
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /** Usuários inativos nunca são autenticados. */
    public boolean autenticar(String senha) {
        return ativo && Senhas.confere(senha, senhaCriptografada);
    }

    @Override
    public String descrever() {
        return getNome() + " · " + email;
    }
}
