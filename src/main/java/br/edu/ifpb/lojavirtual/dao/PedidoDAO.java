package br.edu.ifpb.lojavirtual.dao;

import br.edu.ifpb.lojavirtual.model.Categoria;
import br.edu.ifpb.lojavirtual.model.Pedido;
import br.edu.ifpb.lojavirtual.model.PedidoItem;
import br.edu.ifpb.lojavirtual.model.Produto;
import br.edu.ifpb.lojavirtual.model.StatusPedido; // Importado o novo Enum
import br.edu.ifpb.lojavirtual.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoDAO {

    /**
     * Salva um pedido completo.
     * Agora utiliza o status definido no objeto Pedido (ex: PAGO vindo do checkout).
     */
    public void salvar(Pedido pedido) throws SQLException {
        String sqlPedido = "INSERT INTO pedidos (usuario_id, data_pedido, valor_total, status) VALUES (?, ?, ?, ?)";
        String sqlItem = "INSERT INTO pedido_itens (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                pstmtPedido.setInt(1, pedido.getUsuarioId());
                pstmtPedido.setString(2, pedido.getDataPedido());
                pstmtPedido.setDouble(3, pedido.getValorTotal());

                // --- ATUALIZAÇÃO AQUI ---
                // Verifica se o pedido já tem um status (definido no PagamentoController como PAGO)
                StatusPedido statusASalvar = (pedido.getStatus() != null) ? pedido.getStatus() : StatusPedido.PENDENTE;
                pstmtPedido.setString(4, statusASalvar.name());

                pstmtPedido.executeUpdate();

                try (ResultSet generatedKeys = pstmtPedido.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        pedido.setId(generatedKeys.getInt(1));
                        pedido.setStatus(statusASalvar); // Garante que o objeto em memória tenha o status salvo
                    } else {
                        throw new SQLException("Falha ao criar o pedido, nenhum ID obtido.");
                    }
                }
            }

            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem)) {
                for (PedidoItem item : pedido.getItens()) {
                    pstmtItem.setInt(1, pedido.getId());
                    pstmtItem.setInt(2, item.getProduto().getId());
                    pstmtItem.setInt(3, item.getQuantidade());
                    pstmtItem.setDouble(4, item.getPrecoUnitario());
                    pstmtItem.addBatch();
                }
                pstmtItem.executeBatch();
            }
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Busca pedidos de um usuário específico, incluindo o status para acompanhamento (rastreio).
     */
    public List<Pedido> buscarPorUsuario(int usuarioId) throws SQLException {
        Map<Integer, Pedido> pedidosMap = new LinkedHashMap<>();

        String sql = "SELECT ped.id as pedido_id, ped.data_pedido, ped.valor_total, ped.status, " +
                "item.id as item_id, item.quantidade as item_quantidade, item.preco_unitario, " +
                "prod.id as produto_id, prod.nome as produto_nome, prod.descricao as produto_descricao, " +
                "prod.preco as produto_preco, prod.quantidade as produto_quantidade_estoque, " +
                "cat.id as id_categoria, cat.nome as categoria_nome, prod.nome_arquivo_imagem " +
                "FROM pedidos ped " +
                "JOIN pedido_itens item ON ped.id = item.pedido_id " +
                "JOIN produtos prod ON item.produto_id = prod.id " +
                "JOIN categorias cat ON prod.id_categoria = cat.id " +
                "WHERE ped.usuario_id = ? " +
                "ORDER BY ped.id DESC, item.id ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                int pedidoId = rs.getInt("pedido_id");

                Pedido pedido = pedidosMap.computeIfAbsent(pedidoId, id -> {
                    Pedido p = new Pedido();
                    p.setId(id);
                    p.setUsuarioId(usuarioId);
                    try {
                        p.setDataPedido(rs.getString("data_pedido"));
                        p.setValorTotal(rs.getDouble("valor_total"));

                        // Converte String do banco para o Enum StatusPedido
                        String st = rs.getString("status");
                        p.setStatus(StatusPedido.valueOf(st != null ? st : "PENDENTE"));

                        p.setItens(new ArrayList<>());
                    } catch (SQLException e) {
                        throw new RuntimeException("Erro ao ler dados do pedido", e);
                    }
                    return p;
                });

                Produto produto = new Produto();
                produto.setId(rs.getInt("produto_id"));
                produto.setNome(rs.getString("produto_nome"));
                produto.setDescricao(rs.getString("produto_descricao"));
                produto.setPreco(rs.getDouble("produto_preco"));
                produto.setQuantidade(rs.getInt("produto_quantidade_estoque"));
                produto.setNomeArquivoImagem(rs.getString("nome_arquivo_imagem"));

                Categoria categoria = new Categoria(rs.getInt("id_categoria"), rs.getString("categoria_nome"));
                produto.setCategoria(categoria);

                PedidoItem item = new PedidoItem();
                item.setId(rs.getInt("item_id"));
                item.setQuantidade(rs.getInt("item_quantidade"));
                item.setPrecoUnitario(rs.getDouble("preco_unitario"));
                item.setProduto(produto);

                pedido.getItens().add(item);
            }
        }
        return new ArrayList<>(pedidosMap.values());
    }

    /**
     * Lista todos os pedidos cadastrados no sistema (Visão do Administrador).
     */
    public List<Pedido> listarTodos() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, u.nome as nome_usuario FROM pedidos p " +
                "JOIN usuarios u ON p.usuario_id = u.id ORDER BY p.id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setId(rs.getInt("id"));
                pedido.setUsuarioId(rs.getInt("usuario_id"));
                pedido.setDataPedido(rs.getString("data_pedido"));
                pedido.setValorTotal(rs.getDouble("valor_total"));

                // Converte String do banco para Enum
                String statusStr = rs.getString("status");
                pedido.setStatus(StatusPedido.valueOf(statusStr != null ? statusStr : "PENDENTE"));

                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    /**
     * Atualiza o status do pedido no banco de dados.
     * @param pedidoId ID do pedido.
     * @param novoStatus Novo status do tipo Enum StatusPedido.
     */
    public void atualizarStatus(int pedidoId, StatusPedido novoStatus) throws SQLException {
        String sql = "UPDATE pedidos SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, novoStatus.name());
            pstmt.setInt(2, pedidoId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Regra de negócio: Verifica se o usuário já adquiriu o produto em algum pedido anterior.
     */
    public boolean jaComprouProduto(int usuarioId, int produtoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos p " +
                "JOIN pedido_itens i ON p.id = i.pedido_id " +
                "WHERE p.usuario_id = ? AND i.produto_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, produtoId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}