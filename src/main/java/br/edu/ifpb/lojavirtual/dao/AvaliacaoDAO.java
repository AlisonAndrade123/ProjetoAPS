package br.edu.ifpb.lojavirtual.dao;

import br.edu.ifpb.lojavirtual.model.Avaliacao;
import br.edu.ifpb.lojavirtual.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoDAO {

    // Equivale ao método "publicar()" do diagrama
    public void salvar(Avaliacao avaliacao) throws SQLException {
        String sql = "INSERT INTO avaliacoes (comentario, nota, data_avaliacao, id_usuario, id_produto) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, avaliacao.getComentario());
            pstmt.setInt(2, avaliacao.getNota());
            pstmt.setString(3, avaliacao.getDataAvaliacao());
            pstmt.setInt(4, avaliacao.getIdUsuario());
            pstmt.setInt(5, avaliacao.getIdProduto());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    avaliacao.setId(rs.getInt(1));
                }
            }
        }
    }

    // Equivale ao método "listarPorProduto()" do diagrama
    public List<Avaliacao> buscarPorProduto(int idProduto) throws SQLException {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        // Fazemos um JOIN com a tabela usuários para pegar o nome de quem avaliou
        String sql = "SELECT a.*, u.nome AS nome_usuario FROM avaliacoes a " +
                "JOIN usuarios u ON a.id_usuario = u.id " +
                "WHERE a.id_produto = ? ORDER BY a.id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProduto);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Avaliacao av = new Avaliacao();
                av.setId(rs.getInt("id"));
                av.setComentario(rs.getString("comentario"));
                av.setNota(rs.getInt("nota"));
                av.setDataAvaliacao(rs.getString("data_avaliacao"));
                av.setIdUsuario(rs.getInt("id_usuario"));
                av.setIdProduto(rs.getInt("id_produto"));

                // Pega o nome do usuário vindo do JOIN
                av.setNomeUsuario(rs.getString("nome_usuario"));

                avaliacoes.add(av);
            }
        }
        return avaliacoes;
    }

    // Equivale ao método "remover()" do diagrama
    public boolean deletar(int idAvaliacao) throws SQLException {
        String sql = "DELETE FROM avaliacoes WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idAvaliacao);
            return pstmt.executeUpdate() > 0;
        }
    }
}