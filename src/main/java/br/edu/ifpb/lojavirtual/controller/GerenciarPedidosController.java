package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.PedidoDAO;
import br.edu.ifpb.lojavirtual.model.Pedido;
import br.edu.ifpb.lojavirtual.model.StatusPedido;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;

public class GerenciarPedidosController {

    @FXML private TableView<Pedido> tabelaPedidos;
    @FXML private TableColumn<Pedido, Integer> colId;
    @FXML private TableColumn<Pedido, String> colData;
    @FXML private TableColumn<Pedido, String> colTotal;
    @FXML private TableColumn<Pedido, String> colStatus;
    @FXML private TableColumn<Pedido, Pedido> colAcoes; // Esta coluna precisa do objeto Pedido

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private Stage stage;

    public void setStage(Stage stage) { this.stage = stage; }
    public Stage getStage() { return stage; }

    @FXML
    public void initialize() {
        configurarTabela();
        carregarPedidos();
    }

    private void configurarTabela() {
        // Colunas normais
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colData.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataPedido()));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("R$ %.2f", d.getValue().getValorTotal())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));

        // --- LINHA QUE ESTAVA FALTANDO ---
        // Dizemos para a coluna de ações que o "valor" dela é o pedido inteiro da linha
        colAcoes.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));

        // Agora o CellFactory vai funcionar porque ele terá um objeto Pedido para trabalhar
        colAcoes.setCellFactory(param -> new TableCell<Pedido, Pedido>() {
            private final ComboBox<StatusPedido> comboStatus = new ComboBox<>(FXCollections.observableArrayList(StatusPedido.values()));

            {
                comboStatus.setPromptText("Mudar...");
                comboStatus.setPrefWidth(150);

                comboStatus.setOnAction(e -> {
                    Pedido p = getItem(); // Pega o pedido da linha atual
                    StatusPedido novoStatus = comboStatus.getValue();

                    if (p != null && novoStatus != null && !novoStatus.equals(p.getStatus())) {
                        // Aplica a regra de negócio do Enum
                        if (p.getStatus().podeMudarPara(novoStatus)) {
                            atualizarStatusPedido(p, novoStatus);
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Transição Inválida",
                                    "Não é permitido mudar de " + p.getStatus() + " para " + novoStatus);
                            // Reseta o combo para o valor antigo
                            comboStatus.setValue(p.getStatus());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Atualiza o valor do ComboBox sem disparar o evento de mudança
                    comboStatus.getSelectionModel().select(item.getStatus());
                    setGraphic(comboStatus);
                }
            }
        });
    }

    private void carregarPedidos() {
        try {
            List<Pedido> lista = pedidoDAO.listarTodos();
            tabelaPedidos.setItems(FXCollections.observableArrayList(lista));
            tabelaPedidos.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void atualizarStatusPedido(Pedido p, StatusPedido novoStatus) {
        try {
            pedidoDAO.atualizarStatus(p.getId(), novoStatus);
            p.setStatus(novoStatus); // Atualiza o objeto na memória
            tabelaPedidos.refresh(); // Atualiza a linha na tabela
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar banco.");
        }
    }

    @FXML void handleFechar() { if(stage != null) stage.close(); }

    private void showAlert(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        if (stage != null) a.initOwner(stage);
        a.showAndWait();
    }
}