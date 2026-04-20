package br.edu.ifpb.lojavirtual.model;

import java.time.LocalDateTime;
import java.util.List;

public class NotaFiscal {
    private String numero;
    private LocalDateTime dataEmissao;
    private String clienteNome;
    private List<Produto> produtos;
    private double valorTotal;
    private Endereco enderecoEntrega;

    public NotaFiscal() {
    }

    public NotaFiscal(String numero, LocalDateTime dataEmissao, String clienteNome, List<Produto> produtos, double valorTotal, Endereco enderecoEntrega) {
        this.numero = numero;
        this.dataEmissao = dataEmissao;
        this.clienteNome = clienteNome;
        this.produtos = produtos;
        this.valorTotal = valorTotal;
        this.enderecoEntrega = enderecoEntrega;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public List<Produto> getProdutos() { return produtos; }
    public void setProdutos(List<Produto> produtos) { this.produtos = produtos; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public Endereco getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(Endereco enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }
}