package dominio.admin;

import dominio.base.Pessoa;
import java.util.Set;

public class Usuario extends Pessoa {

    private String email;
    private String senha;
    private Set<Perfil> perfis;
    private boolean ativo;

    public Usuario(int id, String nome, String email, String senha, Set<Perfil> perfis) {
        super(id, nome);
        setPerfis(perfis);
        this.ativo = true;
        setEmail(email);
        setSenha(senha);
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()){
            throw new IllegalArgumentException("O E-mail não pode ser nulo ou vazio.");
        }

        if (!email.matches(".+@.+\\..+")) {
            throw new IllegalArgumentException("Formato de E-mail inválido.");
        }
        this.email = email;

    }

    public void setSenha(String senha) {
        if(senha == null || senha.trim().isEmpty()){
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia.");

        } else if(senha.length() < 8){
            throw new IllegalArgumentException("A senha tem que ter no minimo 8 caracteres.");

        } else if(!senha.matches(".*\\d.*")){
            throw new IllegalArgumentException("A senha tem que ter no minimo um numero.");

        } else if(!senha.matches(".*[a-zA-Z].*")){
            throw new IllegalArgumentException("A senha tem que ter no minimo uma letra.");

        } else {
            this.senha = senha;
        }
    }

    public void setPerfis(Set<Perfil> perfis) {
        if(perfis == null || perfis.isEmpty()){
            throw new IllegalArgumentException("O usuário deve possuir pelo menos um perfil.");
        }
        this.perfis = Set.copyOf(perfis);
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean autenticar(String senha) {
        return this.senha.equals(senha) && this.ativo;
    }

    @Override
    public void exibirDetalhes() {
        System.out.println("=== Detalhes do Usuário ===");
        System.out.println("ID: " + getId());
        System.out.println("Nome: " + getNome());
        System.out.println("E-mail: " + email);
        System.out.println("Perfis: " + getPerfis());
        System.out.println("Status: " + (ativo ? "Ativo" : "Inativo"));
    }
}


