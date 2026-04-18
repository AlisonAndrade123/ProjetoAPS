package br.edu.ifpb.lojavirtual.service;

import br.edu.ifpb.lojavirtual.dao.UsuarioDAO;
import br.edu.ifpb.lojavirtual.model.*;
import br.edu.ifpb.lojavirtual.util.GeradorNotaFiscalPDF;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotaFiscalService {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Gera a nota fiscal a partir de um pedido finalizado.
     * @param pedido O pedido completo vindo do banco/carrinho.
     */
    public void gerarNotaFiscal(Pedido pedido) throws Exception {
        // 1. Buscar o objeto Usuario completo para obter o nome do cliente
        Usuario usuario = usuarioDAO.findById(pedido.getUsuarioId());

        String nomeCliente = (usuario != null) ? usuario.getNome() : "Cliente ID: " + pedido.getUsuarioId();

        // 2. Transformar a lista de PedidoItem em uma lista linear de Produtos para o PDF
        // (O gerador espera uma lista onde se houver 2 itens iguais, eles aparecem 2 vezes ou são agrupados)
        List<Produto> produtosParaNota = new ArrayList<>();
        for (PedidoItem item : pedido.getItens()) {
            for (int i = 0; i < item.getQuantidade(); i++) {
                produtosParaNota.add(item.getProduto());
            }
        }

        // 3. Criar e popular o objeto NotaFiscal (Modelo)
        // Certifique-se de que a classe NotaFiscal tem o construtor vazio e os setters!
        NotaFiscal nf = new NotaFiscal();
        nf.setNumero(String.valueOf(pedido.getId()));
        nf.setDataEmissao(LocalDateTime.now());
        nf.setClienteNome(nomeCliente);
        nf.setProdutos(produtosParaNota);
        nf.setValorTotal(pedido.getValorTotal());

        // O Pedido deve carregar o objeto Endereco preenchido na tela de carrinho
        nf.setEnderecoEntrega(pedido.getEndereco());

        // 4. Chamar o utilitário de geração e abertura do arquivo PDF
        File arquivoPdf = GeradorNotaFiscalPDF.gerarPdf(nf);
        GeradorNotaFiscalPDF.abrirPdf(arquivoPdf);
    }
}