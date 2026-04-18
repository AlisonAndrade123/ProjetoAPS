package br.edu.ifpb.lojavirtual.dao;

import br.edu.ifpb.lojavirtual.model.Categoria;
import br.edu.ifpb.lojavirtual.model.Produto;
import br.edu.ifpb.lojavirtual.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public void save(Produto produto) throws SQLException {
        String sql = "INSERT INTO produtos (nome, descricao, preco, quantidade, id_categoria, nome_arquivo_imagem) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, produto.getNome());
            pstmt.setString(2, produto.getDescricao());
            pstmt.setDouble(3, produto.getPreco());
            pstmt.setInt(4, produto.getQuantidade());

            // Pega o ID do objeto Categoria que está dentro do Produto
            pstmt.setInt(5, produto.getCategoria().getId());

            pstmt.setString(6, produto.getNomeArquivoImagem());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        produto.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    public List<Produto> findAll() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        // Fazemos um JOIN com a tabela categorias para pegar o nome dela também
        String sql = "SELECT p.id, p.nome, p.descricao, p.preco, p.quantidade, p.id_categoria, c.nome AS categoria_nome, p.nome_arquivo_imagem " +
                "FROM produtos p JOIN categorias c ON p.id_categoria = c.id";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                produtos.add(extrairProdutoDoResultSet(rs));
            }
        }
        return produtos;
    }

    // Alterado para buscar por ID da Categoria (mais seguro e rápido)
    public List<Produto> findByCategoryId(int idCategoria) throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT p.id, p.nome, p.descricao, p.preco, p.quantidade, p.id_categoria, c.nome AS categoria_nome, p.nome_arquivo_imagem " +
                "FROM produtos p JOIN categorias c ON p.id_categoria = c.id " +
                "WHERE p.id_categoria = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCategoria);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    produtos.add(extrairProdutoDoResultSet(rs));
                }
            }
        }
        return produtos;
    }

    public List<Produto> search(String searchTerm) throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT p.id, p.nome, p.descricao, p.preco, p.quantidade, p.id_categoria, c.nome AS categoria_nome, p.nome_arquivo_imagem " +
                "FROM produtos p JOIN categorias c ON p.id_categoria = c.id " +
                "WHERE p.nome LIKE ? OR p.descricao LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            pstmt.setString(2, "%" + searchTerm + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    produtos.add(extrairProdutoDoResultSet(rs));
                }
            }
        }
        return produtos;
    }

    public boolean update(Produto produto) throws SQLException {
        String sql = "UPDATE produtos SET nome = ?, descricao = ?, preco = ?, quantidade = ?, id_categoria = ?, nome_arquivo_imagem = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, produto.getNome());
            pstmt.setString(2, produto.getDescricao());
            pstmt.setDouble(3, produto.getPreco());
            pstmt.setInt(4, produto.getQuantidade());
            pstmt.setInt(5, produto.getCategoria().getId());
            pstmt.setString(6, produto.getNomeArquivoImagem());
            pstmt.setInt(7, produto.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM produtos WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Produto findById(int id) throws SQLException {
        String sql = "SELECT p.id, p.nome, p.descricao, p.preco, p.quantidade, p.id_categoria, c.nome AS categoria_nome, p.nome_arquivo_imagem " +
                "FROM produtos p JOIN categorias c ON p.id_categoria = c.id " +
                "WHERE p.id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extrairProdutoDoResultSet(rs);
                }
            }
        }
        return null;
    }

    // NOVO MÉTODO: Será usado na hora de aprovar o pagamento!
    public boolean atualizarEstoque(int idProduto, int novaQuantidade) throws SQLException {
        String sql = "UPDATE produtos SET quantidade = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, novaQuantidade);
            pstmt.setInt(2, idProduto);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Método auxiliar para evitar repetição de código
    private Produto extrairProdutoDoResultSet(ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setId(rs.getInt("id"));
        produto.setNome(rs.getString("nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setPreco(rs.getDouble("preco"));
        produto.setQuantidade(rs.getInt("quantidade"));
        produto.setNomeArquivoImagem(rs.getString("nome_arquivo_imagem"));

        // Montando o objeto Categoria com os dados do JOIN
        Categoria categoria = new Categoria();
        categoria.setId(rs.getInt("id_categoria"));
        categoria.setNome(rs.getString("categoria_nome"));
        produto.setCategoria(categoria);

        return produto;
    }
}