package br.edu.ifpb.lojavirtual.pagamento;

import br.edu.ifpb.lojavirtual.model.Pedido;
import javafx.scene.Node;

/**
 * Interface que define a estratégia de pagamento.
 * Une a parte visual (JavaFX) com a lógica de negócio (Pedido).
 */
public interface MetodoPagamento {

    // Retorna o nome do método (Ex: "PIX" ou "Boleto")
    String getNome();

    // Retorna o componente visual que será injetado na tela de pagamento
    Node gerarComponenteVisual();

    /**
     * Método responsável por executar a lógica específica de cada pagamento
     * no momento em que o usuário confirma a compra.
     * @param pedido O pedido que está sendo finalizado.
     */
    void processar(Pedido pedido) throws Exception;
}