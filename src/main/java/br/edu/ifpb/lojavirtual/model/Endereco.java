package br.edu.ifpb.lojavirtual.model;

public class Endereco {

    private Integer id; // Novo atributo (Diagrama)
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private Integer idUsuario; // Novo atributo para ligar com a tabela de usuários

    // Construtor vazio (Muito importante para o EnderecoDAO)
    public Endereco() {
    }

    // Construtor original (mantido para facilitar criação de novos endereços no código)
    public Endereco(String rua, String numero, String complemento, String bairro, String cidade, String estado, String cep) {
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    // Construtor completo (com IDs)
    public Endereco(Integer id, String rua, String numero, String complemento, String bairro, String cidade, String estado, String cep, Integer idUsuario) {
        this.id = id;
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.idUsuario = idUsuario;
    }

    // --- GETTERS E SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    // --- MANTIVE SEU toString() QUE ESTAVA ÓTIMO ---
    @Override
    public String toString() {
        String comp = (complemento == null || complemento.trim().isEmpty()) ? "" : " - " + complemento;
        return String.format("%s, %s%s - %s\n%s-%s, CEP: %s",
                rua, numero, comp, bairro, cidade, estado, cep);
    }
}