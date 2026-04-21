package br.edu.ifpb.lojavirtual.model;

public enum StatusPedido {
    PENDENTE,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    public boolean podeMudarPara(StatusPedido novoStatus) {
        return switch (this) {
            case PENDENTE -> novoStatus == PAGO || novoStatus == CANCELADO;
            case PAGO -> novoStatus == ENVIADO || novoStatus == CANCELADO;
            case ENVIADO -> novoStatus == ENTREGUE || novoStatus == CANCELADO;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}