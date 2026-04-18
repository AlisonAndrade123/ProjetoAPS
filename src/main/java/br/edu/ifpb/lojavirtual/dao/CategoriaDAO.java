package br.edu.ifpb.lojavirtual.dao;

import br.edu.ifpb.lojavirtual.model.Categoria;
import br.edu.ifpb.lojavirtual.util.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public void salvar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categorias (nome) VALUES (?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, categoria.getNome());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) categoria.setId(rs.getInt(1));
            }
        }
    }

    public List<Categoria> findAll() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nome FROM categorias ORDER BY nome ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categorias.add(new Categoria(rs.getInt("id"), rs.getString("nome")));
            }
        }
        return categorias;
    }
}