package br.edu.ifpb.lojavirtual.service;

import br.edu.ifpb.lojavirtual.dao.UsuarioDAO;
import br.edu.ifpb.lojavirtual.model.PerfilUsuario;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.util.AppException;

import java.util.Optional;

public class AuthService {

    private static AuthService instance;

    private Usuario usuarioLogado;

    private final UsuarioDAO usuarioDAO;

    private AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public Usuario login(String email, String senha) throws AppException {
        if (email == null || email.trim().isEmpty() || senha == null || senha.isEmpty()) {
            throw new AppException("E-mail e senha são obrigatórios.");
        }
        Usuario usuario = usuarioDAO.autenticar(email, senha);

        if (usuario == null) {
            throw new AppException("E-mail ou senha inválidos.");
        }

        this.usuarioLogado = usuario;

        System.out.println("Usuário logado com sucesso: " + usuario.getNome() +
                ". Endereço carregado: " + (usuario.getEndereco() != null ? "Sim" : "Não"));

        return usuario;
    }

    public Usuario register(String nome, String email, String senha, boolean isAdmin) throws AppException {
        if (nome == null || nome.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                senha == null || senha.isEmpty()) {
            throw new AppException("Todos os campos são obrigatórios para o registro.");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new AppException("Formato de e-mail inválido.");
        }

        PerfilUsuario perfil = isAdmin ? PerfilUsuario.ADMIN : PerfilUsuario.CLIENTE;
        Usuario novoUsuario = new Usuario(nome, email, senha, perfil);
        return usuarioDAO.save(novoUsuario);
    }

    public Usuario getUsuarioLogado() {
        return this.usuarioLogado;
    }

    public void logout() {
        this.usuarioLogado = null;
    }
}

