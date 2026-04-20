package br.edu.ifpb.lojavirtual.pagamento;

import br.edu.ifpb.lojavirtual.model.Pedido;
import javafx.scene.Node;

public interface MetodoPagamento {

    String getNome();

    Node gerarComponenteVisual();

    void processar(Pedido pedido) throws Exception;
}