package br.edu.ifpb.lojavirtual.service;

import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Produto;

import java.sql.SQLException;
import java.util.List;

public class ProdutoService {

    private ProdutoDAO produtoDAO;

    public ProdutoService(ProdutoDAO produtoDAO) {
        this.produtoDAO = produtoDAO;
    }

    public List<Produto> getAllProdutos() throws SQLException {
        return produtoDAO.findAll();
    }

    // Alterado para receber o ID da categoria
    public List<Produto> getProdutosPorCategoria(int idCategoria) throws SQLException {
        return produtoDAO.findByCategoryId(idCategoria);
    }

    public List<Produto> searchProdutos(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return produtoDAO.findAll();
        }
        return produtoDAO.search(searchTerm);
    }

    public void saveProduto(Produto produto) throws SQLException {
        produtoDAO.save(produto);
    }

    public boolean updateProduto(Produto produto) throws SQLException {
        return produtoDAO.update(produto);
    }

    public boolean deleteProduto(int id) throws SQLException {
        return produtoDAO.delete(id);
    }
}