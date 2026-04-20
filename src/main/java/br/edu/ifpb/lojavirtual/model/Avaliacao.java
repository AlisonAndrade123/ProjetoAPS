package br.edu.ifpb.lojavirtual.model;

public class Avaliacao {
    private Integer id;
    private String comentario;
    private Integer nota; // Ex: 1 a 5 estrelas
    private String dataAvaliacao;
    private Integer idUsuario;
    private Integer idProduto;

    // Atributo auxiliar para a Interface Gráfica
    private String nomeUsuario;

    public Avaliacao() {}

    public Avaliacao(String comentario, Integer nota, String dataAvaliacao, Integer idUsuario, Integer idProduto) {
        this.comentario = comentario;
        this.nota = nota;
        this.dataAvaliacao = dataAvaliacao;
        this.idUsuario = idUsuario;
        this.idProduto = idProduto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }

    public String getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(String dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdProduto() { return idProduto; }
    public void setIdProduto(Integer idProduto) { this.idProduto = idProduto; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
}