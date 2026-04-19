package br.edu.ifpb.lojavirtual.model;

import br.edu.ifpb.lojavirtual.util.DatabaseManager;
import javafx.scene.image.Image;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Produto {

    private int id;
    private String nome;
    private String descricao;
    private double preco;
    private int quantidade;

    // --- ALTERAÇÃO AQUI ---
    // Substituímos String por Categoria para relacionar os objetos
    private Categoria categoria;

    private String nomeArquivoImagem;
    private transient Image image;

    public Produto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    // --- GETTER E SETTER ATUALIZADOS ---
    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getNomeArquivoImagem() {
        return nomeArquivoImagem;
    }

    public void setNomeArquivoImagem(String nomeArquivoImagem) {
        this.nomeArquivoImagem = nomeArquivoImagem;
        this.image = null;
    }

    public Image getImage() {
        if (image == null) {
            if (this.nomeArquivoImagem != null && !this.nomeArquivoImagem.isEmpty()) {
                try {
                    File arquivoImagem = new File("imagens_produtos" + File.separator + this.nomeArquivoImagem);
                    if (arquivoImagem.exists()) {
                        this.image = new Image(arquivoImagem.toURI().toString());
                    } else {
                        loadPlaceholder();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadPlaceholder();
                }
            } else {
                loadPlaceholder();
            }
        }
        return image;
    }

    private void loadPlaceholder() {
        try {
            this.image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/br/edu/ifpb/lojavirtual/imagens/placeholder.png")));
        } catch (Exception e) {
            e.printStackTrace();
            this.image = null;
        }
    }

    // Adicione isso no seu ProdutoDAO.java
    public List<Produto> listarPorCatalogo(int idCatalogo) throws SQLException {
        List<Produto> produtos = new ArrayList<>();

        // A mágica acontece aqui: juntamos as tabelas 'produtos' e 'catalogo_produtos'
        String sql = "SELECT p.* FROM produtos p " +
                "INNER JOIN catalogo_produtos cp ON p.id = cp.id_produto " +
                "WHERE cp.id_catalogo = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCatalogo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Crie o objeto Produto.
                    // ATENÇÃO: Adapte este bloco de acordo com os atributos que você já tem
                    // no seu Produto e como o seu banco retorna (ex: getDouble ou getBigDecimal para preço).
                    Produto p = new Produto();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    p.setDescricao(rs.getString("descricao"));
                    p.setPreco(rs.getDouble("preco")); // Baseado no seu diagrama de classes
                    p.setQuantidade(rs.getInt("quantidade"));

                    produtos.add(p);
                }
            }
        }
        return produtos;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return id == produto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String nomeCategoria = (categoria != null) ? categoria.getNome() : "Sem categoria";

        return String.format("%s - R$ %.2f (%s)", nome, preco, nomeCategoria);
    }
}