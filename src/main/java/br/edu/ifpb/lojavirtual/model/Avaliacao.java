package br.edu.ifpb.lojavirtual.model;

import java.util.Date;

public class Avaliacao {
    private Integer id;
    private String comentario;
    private Integer nota; // de 1 a 5
    private Date dataAvaliacao;
    private Integer idUsuario;
    private Integer idProduto;

    public Avaliacao(Integer id, String comentario, Integer nota, Date dataAvaliacao, Integer idUsuario, Integer idProduto) {
        this.id = id;
        this.comentario = comentario;
        this.nota = nota;
        this.dataAvaliacao = dataAvaliacao;
        this.idUsuario = idUsuario;
        this.idProduto = idProduto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public Date getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(Date dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Integer idProduto) {
        this.idProduto = idProduto;
    }
}