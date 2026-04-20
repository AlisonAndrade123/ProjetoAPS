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

    public void gerarNotaFiscal(Pedido pedido) throws Exception {
        Usuario usuario = usuarioDAO.findById(pedido.getUsuarioId());
        String nomeCliente = (usuario != null) ? usuario.getNome() : "Cliente ID: " + pedido.getUsuarioId();
        List<Produto> produtosParaNota = new ArrayList<>();
        for (PedidoItem item : pedido.getItens()) {
            for (int i = 0; i < item.getQuantidade(); i++) {
                produtosParaNota.add(item.getProduto());
            }
        }
        NotaFiscal nf = new NotaFiscal();
        nf.setNumero(String.valueOf(pedido.getId()));
        nf.setDataEmissao(LocalDateTime.now());
        nf.setClienteNome(nomeCliente);
        nf.setProdutos(produtosParaNota);
        nf.setValorTotal(pedido.getValorTotal());
        nf.setEnderecoEntrega(pedido.getEndereco());
        File arquivoPdf = GeradorNotaFiscalPDF.gerarPdf(nf);
        GeradorNotaFiscalPDF.abrirPdf(arquivoPdf);
    }
}