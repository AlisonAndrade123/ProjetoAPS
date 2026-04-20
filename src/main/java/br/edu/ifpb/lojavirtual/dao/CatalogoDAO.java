package br.edu.ifpb.lojavirtual.dao;

import br.edu.ifpb.lojavirtual.model.Catalogo;
import br.edu.ifpb.lojavirtual.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CatalogoDAO {

    public void salvar(Catalogo catalogo) throws SQLException {
        String sql = "INSERT INTO catalogos (nome) VALUES (?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, catalogo.getNome());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    catalogo.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Catalogo> listarTodos() throws SQLException {
        List<Catalogo> catalogos = new ArrayList<>();
        String sql = "SELECT * FROM catalogos";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Catalogo c = new Catalogo(rs.getInt("id"), rs.getString("nome"));
                catalogos.add(c);
            }
        }
        return catalogos;
    }

    public void vincularProduto(int idCatalogo, int idProduto) throws SQLException {
        String sql = "INSERT OR IGNORE INTO catalogo_produtos (id_catalogo, id_produto) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCatalogo);
            stmt.setInt(2, idProduto);
            stmt.executeUpdate();
        }
    }

    public void desvincularProduto(int idCatalogo, int idProduto) throws SQLException {
        String sql = "DELETE FROM catalogo_produtos WHERE id_catalogo = ? AND id_produto = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCatalogo);
            stmt.setInt(2, idProduto);
            stmt.executeUpdate();
        }
    }
}