package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.ProdutoDAO; // Importe adicionado
import br.edu.ifpb.lojavirtual.model.Endereco;
import br.edu.ifpb.lojavirtual.model.Produto;
import br.edu.ifpb.lojavirtual.util.CarrinhoManager;
import br.edu.ifpb.lojavirtual.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.sql.SQLException;
import java.util.Map;

public class CarrinhoController {
    @FXML private VBox itensCarrinhoVBox;
    @FXML private Button limparCarrinhoButton;
    @FXML private Button continuarComprandoButton;
    @FXML private Label subtotalLabel;
    @FXML private Label freteLabel;
    @FXML private Label totalLabel;
    @FXML private TextField cepField;
    @FXML private TextField ruaField;
    @FXML private TextField numeroField;
    @FXML private TextField complementoField;
    @FXML private TextField bairroField;
    @FXML private TextField cidadeField;
    @FXML private TextField estadoField;
    @FXML private Button pagamentoButton;

    private final CarrinhoManager carrinhoManager = CarrinhoManager.getInstance();

    @FXML
    public void initialize() {
        limparCarrinhoButton.setOnAction(e -> handleLimparCarrinho());
        continuarComprandoButton.setOnAction(e -> handleContinuarComprando());
        popularItensCarrinho();
    }

    @FXML
    private void handleIrParaPagamento(ActionEvent event) {
        // 1. Validação de campos vazios
        if (ruaField.getText().trim().isEmpty() || numeroField.getText().trim().isEmpty() ||
                bairroField.getText().trim().isEmpty() || cidadeField.getText().trim().isEmpty() ||
                estadoField.getText().trim().isEmpty() || cepField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor, preencha todos os campos de endereço antes de prosseguir.");
            return;
        }

        // 2. VERIFICAÇÃO DE ESTOQUE ANTES DE NAVEGAR
        try {
            ProdutoDAO produtoDAO = new ProdutoDAO();
            for (Map.Entry<Produto, Integer> entry : carrinhoManager.getItens().entrySet()) {
                Produto pDoCarrinho = entry.getKey();
                int qtdDesejada = entry.getValue();

                // Busca o produto atualizado direto do banco de dados
                Produto pDoBanco = produtoDAO.findById(pDoCarrinho.getId());

                if (pDoBanco == null || pDoBanco.getQuantidade() < qtdDesejada) {
                    int estoqueDisponivel = (pDoBanco != null) ? pDoBanco.getQuantidade() : 0;
                    showAlert(Alert.AlertType.ERROR, "Estoque Insuficiente",
                            "Infelizmente o produto '" + pDoCarrinho.getNome() + "' não possui estoque suficiente.\n" +
                                    "Disponível: " + estoqueDisponivel + " unidade(s).");
                    return; // Interrompe o processo e não vai para a tela de pagamento
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao verificar estoque no banco de dados.");
            return;
        }

        // 3. Se passou pela validação de estoque, salva o endereço e prossegue
        Endereco enderecoDeEntrega = new Endereco(
                ruaField.getText(), numeroField.getText(), complementoField.getText(),
                bairroField.getText(), cidadeField.getText(), estadoField.getText(), cepField.getText()
        );

        carrinhoManager.setEnderecoEntrega(enderecoDeEntrega);

        double valorTotal = carrinhoManager.calcularTotal();
        PagamentoController pagamentoController = NavigationManager.getInstance().navigateToPagamento();

        if (pagamentoController != null) {
            pagamentoController.inicializar(valorTotal);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de pagamento.");
        }
    }

    private void popularItensCarrinho() {
        itensCarrinhoVBox.getChildren().clear();
        Map<Produto, Integer> itens = carrinhoManager.getItens();
        if (itens.isEmpty()) {
            Label carrinhoVazioLabel = new Label("Seu carrinho está vazio.");
            carrinhoVazioLabel.setFont(new Font("System Bold", 18));
            itensCarrinhoVBox.getChildren().add(carrinhoVazioLabel);
            pagamentoButton.setDisable(true);
        } else {
            pagamentoButton.setDisable(false);
            for (Map.Entry<Produto, Integer> entry : itens.entrySet()) {
                HBox itemNode = criarItemCarrinhoNode(entry.getKey(), entry.getValue());
                itensCarrinhoVBox.getChildren().add(itemNode);
            }
        }
        atualizarResumo();
    }

    private void atualizarResumo() {
        double subtotal = carrinhoManager.calcularTotal();
        double frete = 0.0;
        double total = subtotal + frete;
        subtotalLabel.setText(String.format("R$ %.2f", subtotal).replace('.', ','));
        freteLabel.setText(String.format("R$ %.2f", frete).replace('.', ','));
        totalLabel.setText(String.format("R$ %.2f", total).replace('.', ','));
    }

    private void handleLimparCarrinho() {
        carrinhoManager.limparCarrinho();
        popularItensCarrinho();
    }

    private void handleContinuarComprando() {
        NavigationManager.getInstance().navigateToProductsView();
    }

    private HBox criarItemCarrinhoNode(Produto produto, int quantidade) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setMinHeight(110.0);
        hBox.setMaxHeight(150.0);
        hBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #00A60E; -fx-border-width: 2; -fx-border-radius: 12;");

        ImageView imageView = new ImageView(produto.getImage());
        imageView.setFitHeight(90.0);
        imageView.setFitWidth(90.0);
        imageView.setPreserveRatio(true);
        HBox.setMargin(imageView, new Insets(0, 0, 0, 15));

        VBox infoVBox = new VBox();
        HBox.setHgrow(infoVBox, javafx.scene.layout.Priority.ALWAYS);
        infoVBox.setPadding(new Insets(0, 0, 0, 20));

        Label nomeLabel = new Label(produto.getNome());
        nomeLabel.setFont(new Font("System Bold", 18.0));
        VBox.setMargin(nomeLabel, new Insets(0, 0, 5, 0));

        Label precoLabel = new Label(String.format("R$ %.2f", produto.getPreco()).replace('.', ','));
        precoLabel.setTextFill(javafx.scene.paint.Color.valueOf("#00a60e"));
        precoLabel.setFont(new Font("System Bold", 16.0));

        Label descricaoLabel = new Label(produto.getDescricao());
        descricaoLabel.setFont(new Font("System", 14.0));
        descricaoLabel.setMaxWidth(450.0);
        descricaoLabel.setWrapText(true);

        infoVBox.getChildren().addAll(nomeLabel, descricaoLabel, precoLabel);

        VBox controlesVBox = new VBox(5.0);
        controlesVBox.setAlignment(Pos.CENTER_RIGHT);
        controlesVBox.setPadding(new Insets(0, 15, 0, 0));

        HBox qtdHBox = new HBox(5.0);
        qtdHBox.setAlignment(Pos.CENTER);
        qtdHBox.setMinWidth(180.0);

        Label qtdLabel = new Label("Quantidade:");
        qtdLabel.setTextFill(javafx.scene.paint.Color.valueOf("#333333"));
        qtdLabel.setFont(new Font(15.0));

        Button upButton = new Button("+");
        upButton.setPrefSize(30, 30);
        upButton.setStyle("-fx-background-color: #00A60E; -fx-background-radius: 5; -fx-cursor: hand; -fx-text-fill: white; -fx-font-weight: bold;");
        upButton.setOnAction(e -> {
            carrinhoManager.incrementarQuantidade(produto);
            popularItensCarrinho();
        });

        TextField qtdTextField = new TextField(String.valueOf(quantidade));
        qtdTextField.setAlignment(Pos.CENTER);
        qtdTextField.setPrefSize(40.0, 30.0);
        qtdTextField.setEditable(false);

        Button downButton = new Button("-");
        downButton.setPrefSize(30, 30);
        downButton.setStyle("-fx-background-color: #DC3545; -fx-background-radius: 5; -fx-cursor: hand; -fx-text-fill: white; -fx-font-weight: bold;");
        downButton.setOnAction(e -> {
            carrinhoManager.decrementarQuantidade(produto);
            popularItensCarrinho();
        });

        qtdHBox.getChildren().addAll(qtdLabel, downButton, qtdTextField, upButton);

        Button removerButton = new Button("Remover");
        removerButton.setStyle("-fx-background-color: #DC3545; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: white; -fx-font-weight: bold;");
        removerButton.setOnAction(e -> {
            carrinhoManager.removerProduto(produto);
            popularItensCarrinho();
        });

        VBox.setMargin(removerButton, new Insets(10, 0, 0, 0));
        controlesVBox.getChildren().addAll(qtdHBox, removerButton);

        hBox.getChildren().addAll(imageView, infoVBox, controlesVBox);
        return hBox;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (itensCarrinhoVBox.getScene() != null) {
            alert.initOwner(itensCarrinhoVBox.getScene().getWindow());
        }
        alert.showAndWait();
    }
}