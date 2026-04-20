package br.edu.ifpb.lojavirtual.model;

public enum StatusPedido {
    PENDENTE,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    /**
     * Define as regras de transição permitidas.
     */
    public boolean podeMudarPara(StatusPedido novoStatus) {
        return switch (this) {
            case PENDENTE -> novoStatus == PAGO || novoStatus == CANCELADO;
            case PAGO -> novoStatus == ENVIADO || novoStatus == CANCELADO;
            case ENVIADO -> novoStatus == ENTREGUE || novoStatus == CANCELADO;
            case ENTREGUE, CANCELADO -> false; // Status finais
        };
    }
}