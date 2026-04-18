package br.edu.ifpb.lojavirtual.model;

public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String senha;
    private PerfilUsuario perfil; // Atributo atualizado (usando o Enum)

    public Usuario() {
    }

    // Construtor sem ID (para novos cadastros)
    public Usuario(String nome, String email, String senha, PerfilUsuario perfil) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
    }

    // Construtor com ID (para buscar do banco de dados)
    public Usuario(int id, String nome, String email, String senha, PerfilUsuario perfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    // Método mantido para não quebrar suas validações de Login
    public boolean isAdmin() {
        return this.perfil != null && this.perfil == PerfilUsuario.ADMIN;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", perfil=" + perfil + // Atualizado no toString
                '}';
    }
}