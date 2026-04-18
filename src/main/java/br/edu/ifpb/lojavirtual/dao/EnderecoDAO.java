package br.edu.ifpb.lojavirtual.dao;

import br.edu.ifpb.lojavirtual.model.Endereco;
import br.edu.ifpb.lojavirtual.util.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnderecoDAO {

    public void salvar(Endereco endereco) throws SQLException {
        String sql = "INSERT INTO enderecos (rua, numero, complemento, bairro, cidade, estado, cep, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, endereco.getRua());
            pstmt.setString(2, endereco.getNumero());
            pstmt.setString(3, endereco.getComplemento());
            pstmt.setString(4, endereco.getBairro());
            pstmt.setString(5, endereco.getCidade());
            pstmt.setString(6, endereco.getEstado());
            pstmt.setString(7, endereco.getCep());
            pstmt.setInt(8, endereco.getIdUsuario());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    endereco.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Endereco> buscarPorUsuario(int idUsuario) throws SQLException {
        List<Endereco> enderecos = new ArrayList<>();
        String sql = "SELECT * FROM enderecos WHERE id_usuario = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Endereco end = new Endereco(
                        rs.getString("rua"), rs.getString("numero"), rs.getString("complemento"),
                        rs.getString("bairro"), rs.getString("cidade"), rs.getString("estado"), rs.getString("cep")
                );
                end.setId(rs.getInt("id"));
                end.setIdUsuario(idUsuario);
                enderecos.add(end);
            }
        }
        return enderecos;
    }
}