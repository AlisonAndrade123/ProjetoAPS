package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.PedidoDAO;
import br.edu.ifpb.lojavirtual.model.Pedido;
import br.edu.ifpb.lojavirtual.model.PedidoItem;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.service.AuthService;
import br.edu.ifpb.lojavirtual.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoricoController {

    @FXML
    private VBox pedidosContainerVBox;
    @FXML
    private Label tituloListaLabel;

    private Usuario usuarioLogado;
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @FXML
    public void initialize() {
        // Pega o usuário da sessão atual
        this.usuarioLogado = AuthService.getInstance().getUsuarioLogado();

        // Limpa o container antes de carregar para evitar duplicatas visuais
        pedidosContainerVBox.getChildren().clear();

        carregarHistorico();
    }

    private void carregarHistorico() {
        if (usuarioLogado == null) {
            tituloListaLabel.setText("Faça o login para ver seu histórico.");
            return;
        }

        try {
            // Chamada ao DAO filtrando pelo ID do usuário logado
            List<Pedido> pedidos = pedidoDAO.buscarPorUsuario(usuarioLogado.getId());

            if (pedidos.isEmpty()) {
                tituloListaLabel.setText("Você ainda não fez nenhuma compra.");
            } else {
                tituloListaLabel.setText("Meus Pedidos Anteriores (" + pedidos.size() + ")");
                for (Pedido pedido : pedidos) {
                    Node cardPedido = criarCardPedido(pedido);
                    pedidosContainerVBox.getChildren().add(cardPedido);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            tituloListaLabel.setText("Erro ao carregar o histórico.");
        }
    }

    private Node criarCardPedido(Pedido pedido) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");

        // Formatação de Data
        String dataFormatada;
        try {
            LocalDateTime dataDoPedido = LocalDateTime.parse(pedido.getDataPedido());
            DateTimeFormatter formatoParaUsuario = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            dataFormatada = dataDoPedido.format(formatoParaUsuario);
        } catch (Exception e) {
            dataFormatada = pedido.getDataPedido(); // fallback
        }

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Pedido #" + pedido.getId());
        idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label dataLabel = new Label(dataFormatada);
        dataLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #777;");

        header.getChildren().addAll(idLabel, dataLabel);

        VBox itensVBox = new VBox(10);
        itensVBox.setPadding(new Insets(10, 0, 10, 10));

        for (PedidoItem item : pedido.getItens()) {
            HBox itemBox = new HBox(15);
            itemBox.setAlignment(Pos.CENTER_LEFT);

            ImageView imageView = new ImageView(item.getProduto().getImage());
            imageView.setFitHeight(45);
            imageView.setFitWidth(45);
            imageView.setPreserveRatio(true);

            Label itemLabel = new Label(item.getQuantidade() + "x " + item.getProduto().getNome());
            itemLabel.setStyle("-fx-font-size: 14px;");

            Label precoLabel = new Label(String.format("R$ %.2f", item.getPrecoUnitario() * item.getQuantidade()));
            precoLabel.setStyle("-fx-font-weight: bold;");

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            itemBox.getChildren().addAll(imageView, itemLabel, spacer, precoLabel);
            itensVBox.getChildren().add(itemBox);
        }

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label totalLabel = new Label("Total: " + String.format("R$ %.2f", pedido.getValorTotal()));
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00A60E;");
        footer.getChildren().add(totalLabel);

        card.getChildren().addAll(header, new Separator(), itensVBox, footer);
        VBox.setMargin(card, new Insets(0, 0, 15, 0));

        return card;
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        NavigationManager.getInstance().navigateToProductsView();
    }
}