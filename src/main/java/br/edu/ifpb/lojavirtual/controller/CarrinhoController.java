package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.EnderecoDAO;
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.*;
import br.edu.ifpb.lojavirtual.service.AuthService;
import br.edu.ifpb.lojavirtual.util.CarrinhoManager;
import br.edu.ifpb.lojavirtual.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CarrinhoController {
    @FXML private VBox itensCarrinhoVBox;
    @FXML private Button limparCarrinhoButton, continuarComprandoButton, pagamentoButton;
    @FXML private Label subtotalLabel, freteLabel, totalLabel;
    @FXML private ComboBox<Endereco> enderecoComboBox;

    private final CarrinhoManager carrinhoManager = CarrinhoManager.getInstance();
    private final EnderecoDAO enderecoDAO = new EnderecoDAO();

    @FXML
    public void initialize() {
        limparCarrinhoButton.setOnAction(e -> handleLimparCarrinho());
        continuarComprandoButton.setOnAction(e -> handleContinuarComprando());

        configurarComboBoxEndereco();
        popularItensCarrinho();
    }

    private void configurarComboBoxEndereco() {
        enderecoComboBox.getItems().clear();
        Usuario user = AuthService.getInstance().getUsuarioLogado();
        if (user == null) return;
        try {
            List<Endereco> enderecos = enderecoDAO.buscarPorUsuario(user.getId());
            enderecoComboBox.getItems().addAll(enderecos);
            enderecoComboBox.setConverter(new StringConverter<>() {
                public String toString(Endereco e) { return e == null ? "" : e.getRua() + ", " + e.getNumero(); }
                public Endereco fromString(String s) { return null; }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAbrirCadastro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/edu/ifpb/lojavirtual/view/EnderecoForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            configurarComboBoxEndereco();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void popularItensCarrinho() {
        itensCarrinhoVBox.getChildren().clear();
        Map<Produto, Integer> itens = carrinhoManager.getItens();
        if (itens == null || itens.isEmpty()) {
            itensCarrinhoVBox.getChildren().add(new Label("Seu carrinho está vazio."));
            pagamentoButton.setDisable(true);
        } else {
            pagamentoButton.setDisable(false);
            for (Map.Entry<Produto, Integer> entry : itens.entrySet()) {
                itensCarrinhoVBox.getChildren().add(criarItemCarrinhoNode(entry.getKey(), entry.getValue()));
            }
        }
        atualizarResumo();
    }

    @FXML private void handleIrParaPagamento(ActionEvent event) {
        if (enderecoComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Selecione um endereço!");
            return;
        }
        carrinhoManager.setEnderecoEntrega(enderecoComboBox.getValue());
        PagamentoController pc = NavigationManager.getInstance().navigateToPagamento();
        if (pc != null) pc.inicializar(carrinhoManager.calcularTotal());
    }

    private void atualizarResumo() {
        double sub = carrinhoManager.calcularTotal();
        subtotalLabel.setText("Subtotal: R$ " + String.format("%.2f", sub));
        totalLabel.setText("Total: R$ " + String.format("%.2f", sub));
    }

    private void handleLimparCarrinho() { carrinhoManager.limparCarrinho(); popularItensCarrinho(); }
    private void handleContinuarComprando() { NavigationManager.getInstance().navigateToProductsView(); }
    private void showAlert(Alert.AlertType t, String tit, String msg) {
        Alert a = new Alert(t); a.setTitle(tit); a.setContentText(msg); a.show();
    }

    private HBox criarItemCarrinhoNode(Produto produto, int quantidade) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setMinHeight(110.0);
        hBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #00A60E; -fx-border-width: 2; -fx-border-radius: 12;");

        ImageView imageView = new ImageView(produto.getImage());
        imageView.setFitHeight(90.0); imageView.setFitWidth(90.0); imageView.setPreserveRatio(true);
        HBox.setMargin(imageView, new Insets(0, 0, 0, 15));

        VBox infoVBox = new VBox();
        HBox.setHgrow(infoVBox, Priority.ALWAYS);
        infoVBox.setPadding(new Insets(10, 20, 10, 20));

        Label nomeLabel = new Label(produto.getNome());
        nomeLabel.setFont(new Font("System Bold", 18.0));

        Label descricaoLabel = new Label(produto.getDescricao());
        descricaoLabel.setFont(new Font("System", 14.0));
        descricaoLabel.setWrapText(true);
        descricaoLabel.setMaxWidth(350.0);

        Label precoLabel = new Label(String.format("R$ %.2f", produto.getPreco()).replace('.', ','));
        precoLabel.setTextFill(Color.valueOf("#00a60e"));
        precoLabel.setFont(new Font("System Bold", 16.0));

        infoVBox.getChildren().addAll(nomeLabel, descricaoLabel, precoLabel);

        VBox controlesVBox = new VBox(5.0);
        controlesVBox.setAlignment(Pos.CENTER_RIGHT);
        controlesVBox.setPadding(new Insets(0, 15, 0, 0));

        HBox qtdHBox = new HBox(5.0);
        qtdHBox.setAlignment(Pos.CENTER);

        Button downButton = new Button("-");
        downButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white;");
        downButton.setOnAction(e -> { carrinhoManager.decrementarQuantidade(produto); popularItensCarrinho(); });

        TextField qtdTextField = new TextField(String.valueOf(quantidade));
        qtdTextField.setPrefWidth(40);
        qtdTextField.setEditable(false);

        Button upButton = new Button("+");
        upButton.setStyle("-fx-background-color: #00A60E; -fx-text-fill: white;");
        upButton.setOnAction(e -> { carrinhoManager.incrementarQuantidade(produto); popularItensCarrinho(); });

        qtdHBox.getChildren().addAll(downButton, qtdTextField, upButton);

        Button removerButton = new Button("Remover");
        removerButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white;");
        removerButton.setOnAction(e -> { carrinhoManager.removerProduto(produto); popularItensCarrinho(); });

        controlesVBox.getChildren().addAll(qtdHBox, removerButton);

        hBox.getChildren().addAll(imageView, infoVBox, controlesVBox);
        return hBox;
    }
}