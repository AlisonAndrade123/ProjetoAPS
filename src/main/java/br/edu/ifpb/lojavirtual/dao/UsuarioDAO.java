package br.edu.ifpb.lojavirtual.dao;

import br.edu.ifpb.lojavirtual.model.PerfilUsuario;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.util.AppException;
import br.edu.ifpb.lojavirtual.util.DatabaseManager;

import java.sql.*;
import java.util.Optional;

public class UsuarioDAO {

    public Usuario autenticar(String email, String senha) {
        String sql = "SELECT id, nome, email, senha, is_admin FROM usuarios WHERE email = ? AND senha = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extrairUsuarioDoResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT id, nome, email, senha, is_admin FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extrairUsuarioDoResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * NOVO MÉTODO: Busca o usuário pelo ID.
     * Necessário para gerar a Nota Fiscal com os dados do cliente.
     */
    public Usuario findById(int id) throws SQLException {
        String sql = "SELECT id, nome, email, senha, is_admin FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairUsuarioDoResultSet(rs);
                }
            }
        }
        return null;
    }

    public Usuario save(Usuario usuario) throws AppException {
        if (findByEmail(usuario.getEmail()).isPresent()) {
            throw new AppException("E-mail já cadastrado.");
        }

        String sql = "INSERT INTO usuarios (nome, email, senha, is_admin) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setBoolean(4, usuario.isAdmin());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar usuário, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao criar usuário, nenhum ID gerado.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AppException("Erro ao cadastrar usuário: " + e.getMessage());
        }
        return usuario;
    }

    /**
     * Método auxiliar privado para evitar repetição de código ao ler dados do banco.
     */
    private Usuario extrairUsuarioDoResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));

        boolean isAdminDB = rs.getBoolean("is_admin");
        usuario.setPerfil(isAdminDB ? PerfilUsuario.ADMIN : PerfilUsuario.CLIENTE);

        return usuario;
    }
}