package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.PedidoDAO;
import br.edu.ifpb.lojavirtual.model.Pedido;
import br.edu.ifpb.lojavirtual.model.PedidoItem;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.model.StatusPedido; // Importando o Enum
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
import javafx.scene.text.Font;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoricoController {

    @FXML private VBox pedidosContainerVBox;
    @FXML private Label tituloListaLabel;

    private Usuario usuarioLogado;
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @FXML
    public void initialize() {
        // Recupera o usuário da sessão
        this.usuarioLogado = AuthService.getInstance().getUsuarioLogado();

        // Limpa a tela antes de carregar
        pedidosContainerVBox.getChildren().clear();

        carregarHistorico();
    }

    private void carregarHistorico() {
        if (usuarioLogado == null) {
            tituloListaLabel.setText("Faça o login para ver seu histórico.");
            return;
        }

        try {
            // Busca os pedidos filtrados pelo ID do usuário logado
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
            tituloListaLabel.setText("Erro ao carregar o histórico no banco de dados.");
        }
    }

    private Node criarCardPedido(Pedido pedido) {
        // Container principal do Card
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");

        // Formatação da Data do Pedido
        String dataFormatada;
        try {
            LocalDateTime dataDoPedido = LocalDateTime.parse(pedido.getDataPedido());
            DateTimeFormatter formatoParaUsuario = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            dataFormatada = dataDoPedido.format(formatoParaUsuario);
        } catch (Exception e) {
            dataFormatada = pedido.getDataPedido();
        }

        // Cabeçalho do Card (ID, Status e Data)
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Pedido #" + pedido.getId());
        idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // --- LÓGICA DE CORES DO STATUS PARA O CLIENTE ---
        Label statusLabel = new Label(pedido.getStatus().name());
        String corStatus = switch (pedido.getStatus()) {
            case PAGO -> "#2196F3";      // Azul
            case ENVIADO -> "#FF9800";   // Laranja
            case ENTREGUE -> "#00A60E";  // Verde
            case CANCELADO -> "#F44336"; // Vermelho
            default -> "#757575";        // Cinza (Pendente)
        };
        statusLabel.setStyle("-fx-background-color: " + corStatus + "; -fx-text-fill: white; " +
                "-fx-padding: 3 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label dataLabel = new Label(dataFormatada);
        dataLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #777;");

        header.getChildren().addAll(idLabel, statusLabel, dataLabel);

        // Seção de Itens (Lista de produtos dentro do pedido)
        VBox itensVBox = new VBox(12);
        itensVBox.setPadding(new Insets(10, 0, 10, 10));

        for (PedidoItem item : pedido.getItens()) {
            HBox itemBox = new HBox(15);
            itemBox.setAlignment(Pos.CENTER_LEFT);

            // Imagem do Produto
            ImageView imageView = new ImageView(item.getProduto().getImage());
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);

            // Nome e Quantidade
            Label itemLabel = new Label(item.getQuantidade() + "x " + item.getProduto().getNome());
            itemLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #444;");

            // Subtotal do item
            Label precoLabel = new Label(String.format("R$ %.2f", item.getPrecoUnitario() * item.getQuantidade()));
            precoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

            // Espaçador para jogar o preço para a direita
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            itemBox.getChildren().addAll(imageView, itemLabel, spacer, precoLabel);
            itensVBox.getChildren().add(itemBox);
        }

        // Rodapé do Card (Valor Total)
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label totalLabel = new Label("Total Pago: " + String.format("R$ %.2f", pedido.getValorTotal()));
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00A60E;");
        footer.getChildren().add(totalLabel);

        // Adiciona todas as partes ao Card principal
        card.getChildren().addAll(header, new Separator(), itensVBox, footer);

        // Adiciona uma margem entre os cards na lista
        VBox.setMargin(card, new Insets(0, 0, 15, 0));

        return card;
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        NavigationManager.getInstance().navigateToProductsView();
    }
}