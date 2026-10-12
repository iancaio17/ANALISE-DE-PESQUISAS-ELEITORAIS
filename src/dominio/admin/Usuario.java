package dominio.admin;

import dominio.base.Pessoa;

public class Usuario extends Pessoa {

    private String email;
    private String senha;
    private String perfil;
    private boolean ativo;

    public Usuario(int id, String nome, String email, String senha, String perfil) {
        super(id, nome);
        this.email = email;
        this.perfil = perfil;
        this.ativo = true;


        setSenha(senha);
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setSenha(String senha) {
        if(senha == null || senha.trim().isEmpty()){
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia.");

        } else if(senha.length() < 8){
            throw new IllegalArgumentException("A senha tem que ter no minimo 8 caracteres.");

        } else if(!senha.matches(".*\\d.*")){
            throw new IllegalArgumentException("A senha tem que ter no minimo um numero.");

        } else if(!senha.matches(".[a-zA-Z].")){
            throw new IllegalArgumentException("A senha tem que ter no minimo uma letra.");

        } else {
            this.senha = senha;
        }
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
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
        System.out.println("Perfil: " + perfil);
        System.out.println("Status: " + (ativo ? "Ativo" : "Inativo"));
    }
}

