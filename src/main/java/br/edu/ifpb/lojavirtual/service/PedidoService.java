package br.edu.ifpb.lojavirtual.service;

import br.edu.ifpb.lojavirtual.dao.PedidoDAO;
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Pedido;
import br.edu.ifpb.lojavirtual.model.PedidoItem;
import br.edu.ifpb.lojavirtual.model.Produto;
import java.sql.SQLException;

public class PedidoService {

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private NotaFiscalService notaFiscalService = new NotaFiscalService();

    public void finalizarPedido(Pedido pedido) throws Exception {
        for (PedidoItem item : pedido.getItens()) {
            Produto pBD = produtoDAO.findById(item.getProduto().getId());
            if (pBD.getQuantidade() < item.getQuantidade()) {
                throw new Exception("Estoque insuficiente para: " + pBD.getNome());
            }
        }
        pedidoDAO.salvar(pedido);
        for (PedidoItem item : pedido.getItens()) {
            Produto pBD = produtoDAO.findById(item.getProduto().getId());
            int novoEstoque = pBD.getQuantidade() - item.getQuantidade();
            produtoDAO.atualizarEstoque(pBD.getId(), novoEstoque);
        }
        notaFiscalService.gerarNotaFiscal(pedido);
    }
}