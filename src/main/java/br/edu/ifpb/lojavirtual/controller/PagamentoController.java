package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.model.*;
import br.edu.ifpb.lojavirtual.pagamento.MetodoPagamento;
import br.edu.ifpb.lojavirtual.pagamento.PagamentoBoleto;
import br.edu.ifpb.lojavirtual.pagamento.PagamentoPix;
import br.edu.ifpb.lojavirtual.service.AuthService;
import br.edu.ifpb.lojavirtual.service.PedidoService;
import br.edu.ifpb.lojavirtual.util.CarrinhoManager;
import br.edu.ifpb.lojavirtual.util.NavigationManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PagamentoController {

    @FXML private VBox paymentOptionsVBox;
    @FXML private VBox resumoVBox;
    @FXML private VBox processamentoBox;
    @FXML private Label tituloLabel;
    @FXML private Label resumoTituloLabel;
    @FXML private Label totalTituloLabel;
    @FXML private Label totalLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label freteLabel;
    @FXML private Button confirmarPagamentoButton;
    @FXML private Button btnVoltar;
    @FXML private HBox opcoesPagamentoHBox;
    @FXML private StackPane painelMetodoPagamento;

    private final CarrinhoManager carrinhoManager = CarrinhoManager.getInstance();
    private ToggleGroup toggleGroup;
    private double valorTotalCompra;

    public void inicializar(double valorTotal) {
        this.valorTotalCompra = valorTotal;
        String valorFormatado = String.format("R$ %.2f", valorTotal).replace('.', ',');
        totalLabel.setText(valorFormatado);
        subtotalLabel.setText(valorFormatado);
        freteLabel.setText("R$ 0,00");
        confirmarPagamentoButton.setText("Pagar " + valorFormatado);
    }

    @FXML
    public void initialize() {
        aplicarEstilos();
        configurarMetodosPagamento();
    }

    @FXML
    void handleVoltar(ActionEvent event) {
        NavigationManager.getInstance().navigateToCart();
    }

    @FXML
    void handleConfirmarPagamento() {
        setEstadoProcessamento(true);

        Usuario usuarioLogado = AuthService.getInstance().getUsuarioLogado();
        Map<Produto, Integer> itensDoCarrinhoMap = carrinhoManager.getItens();
        Endereco endereco = carrinhoManager.getEnderecoEntrega();

        if (usuarioLogado == null || endereco == null) {
            showAlert(Alert.AlertType.ERROR, "Erro Crítico", "Dados do usuário ou endereço não encontrados.");
            setEstadoProcessamento(false);
            return;
        }

        // Criar o Pedido
        Pedido novoPedido = new Pedido();
        novoPedido.setUsuarioId(usuarioLogado.getId());
        novoPedido.setValorTotal(this.valorTotalCompra);
        novoPedido.setDataPedido(java.time.LocalDateTime.now().toString());
        novoPedido.setEndereco(endereco);

        // --- DEFINIÇÃO DO STATUS INICIAL ---
        novoPedido.setStatus(StatusPedido.PAGO);

        List<PedidoItem> itensDoPedido = new ArrayList<>();
        itensDoCarrinhoMap.forEach((produto, quantidade) -> {
            PedidoItem item = new PedidoItem();
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(produto.getPreco());
            itensDoPedido.add(item);
        });
        novoPedido.setItens(itensDoPedido);

        // --- BACKGROUND THREAD: Processamento sem travar a UI ---
        new Thread(() -> {
            try {
                // 1. Persistência e Baixa de Estoque
                PedidoService pedidoService = new PedidoService();
                pedidoService.finalizarPedido(novoPedido);

                // 2. Lógica do Método de Pagamento (Strategy)
                if (toggleGroup.getSelectedToggle() != null) {
                    MetodoPagamento metodo = (MetodoPagamento) toggleGroup.getSelectedToggle().getUserData();
                    metodo.processar(novoPedido);
                }

                // 3. Sucesso na Thread Principal
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Compra finalizada com sucesso!");
                    carrinhoManager.limparCarrinho();
                    NavigationManager.getInstance().navigateToProductsView();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erro na Finalização", e.getMessage());
                    setEstadoProcessamento(false);
                });
            }
        }).start();
    }

    private void setEstadoProcessamento(boolean processando) {
        confirmarPagamentoButton.setDisable(processando);
        if (btnVoltar != null) btnVoltar.setDisable(processando);

        if (processamentoBox != null) {
            processamentoBox.setVisible(processando);
            processamentoBox.setManaged(processando);
        }
    }

    private void configurarMetodosPagamento() {
        List<MetodoPagamento> metodosDisponiveis = List.of(new PagamentoPix(), new PagamentoBoleto());
        toggleGroup = new ToggleGroup();

        for (MetodoPagamento metodo : metodosDisponiveis) {
            ToggleButton tb = new ToggleButton(metodo.getNome());
            tb.setToggleGroup(toggleGroup);
            tb.setUserData(metodo);
            tb.setStyle("-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 20;");
            opcoesPagamentoHBox.getChildren().add(tb);
        }

        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                MetodoPagamento metodoSelecionado = (MetodoPagamento) newToggle.getUserData();
                Node interfaceVisual = metodoSelecionado.gerarComponenteVisual();
                painelMetodoPagamento.getChildren().setAll(interfaceVisual);
            }
        });

        if (!opcoesPagamentoHBox.getChildren().isEmpty()) {
            toggleGroup.selectToggle((Toggle) opcoesPagamentoHBox.getChildren().get(0));
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        if (Platform.isFxApplicationThread()) {
            exibirAlerta(alertType, title, message);
        } else {
            Platform.runLater(() -> exibirAlerta(alertType, title, message));
        }
    }

    private void exibirAlerta(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void aplicarEstilos() {
        String cardStyle = "-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);";
        paymentOptionsVBox.setStyle(cardStyle);
        resumoVBox.setStyle(cardStyle);
        tituloLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        resumoTituloLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        totalTituloLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        totalLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00A60E;");
        confirmarPagamentoButton.setStyle("-fx-background-color: #00A60E; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 25; -fx-cursor: hand;");

        if (btnVoltar != null) {
            btnVoltar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        }
    }
}