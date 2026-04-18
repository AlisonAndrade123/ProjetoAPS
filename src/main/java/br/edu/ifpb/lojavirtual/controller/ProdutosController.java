package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.CategoriaDAO; // Importe adicionado
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Categoria; // Importe adicionado
import br.edu.ifpb.lojavirtual.model.Produto;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.service.AuthService;
import br.edu.ifpb.lojavirtual.util.CarrinhoManager;
import br.edu.ifpb.lojavirtual.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.sql.SQLException;
import java.util.List;

public class ProdutosController {

    @FXML private TextField searchTextField;
    @FXML private ScrollPane categoryScrollPane;
    @FXML private HBox categoryHBox;
    @FXML private TilePane productTilePane;
    @FXML private Button cartButton;
    @FXML private Button historyButton;
    @FXML private Button profileButton;

    private Usuario usuarioLogado;
    private ProdutoDAO produtoDAO;

    @FXML
    public void initialize() {
        this.usuarioLogado = AuthService.getInstance().getUsuarioLogado();
        this.produtoDAO = new ProdutoDAO();

        if (this.usuarioLogado != null) {
            profileButton.setText("Olá, " + this.usuarioLogado.getNome().split(" ")[0]);
        } else {
            profileButton.setText("Visitante");
        }

        if (searchTextField != null) {
            searchTextField.textProperty().addListener((obs, oldText, newText) -> filterProducts(newText));
        }
        criarBotoesDeCategoria();
        loadAllProducts();
    }

    private void criarBotoesDeCategoria() {
        categoryHBox.getChildren().clear();

        // Botão "Todos"
        Button todosButton = criarBotaoEstilizado("Todos", "Todos");
        categoryHBox.getChildren().add(todosButton);

        try {
            // Agora as categorias vêm do banco!
            CategoriaDAO categoriaDAO = new CategoriaDAO();
            List<Categoria> categorias = categoriaDAO.findAll();

            for (Categoria cat : categorias) {
                Button categoriaButton = criarBotaoEstilizado(cat.getNome(), cat);
                categoryHBox.getChildren().add(categoriaButton);
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erro", "Não foi possível carregar as categorias.");
            e.printStackTrace();
        }
    }

    private Button criarBotaoEstilizado(String nome, Object userData) {
        Button button = new Button(nome);
        button.setUserData(userData);
        button.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #00A60E; -fx-border-radius: 20; -fx-border-width: 2; -fx-text-fill: #333333; -fx-padding: 8 20; -fx-cursor: hand;");
        button.setFont(new Font("System", 16.0));
        button.setOnAction(this::handleCategoryButtonAction);
        return button;
    }

    @FXML
    private void handleCategoryButtonAction(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        Object userData = clickedButton.getUserData();

        if (userData instanceof String && "Todos".equals(userData)) {
            loadAllProducts();
        } else if (userData instanceof Categoria) {
            filterProductsByCategory(((Categoria) userData).getId());
        }
    }

    private void loadAllProducts() {
        if (produtoDAO != null) {
            try {
                List<Produto> produtos = produtoDAO.findAll();
                displayProducts(produtos);
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Erro de Banco de Dados", "Erro ao carregar produtos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void filterProducts(String searchText) {
        if (produtoDAO != null) {
            try {
                List<Produto> produtos = produtoDAO.search(searchText);
                displayProducts(produtos);
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Erro de Banco de Dados", "Erro ao pesquisar produtos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Alterado para filtrar pelo ID da categoria
    private void filterProductsByCategory(int idCategoria) {
        if (produtoDAO != null) {
            try {
                List<Produto> produtos = produtoDAO.findByCategoryId(idCategoria);
                displayProducts(produtos);
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Erro de Banco de Dados", "Erro ao filtrar produtos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void displayProducts(List<Produto> produtos) {
        productTilePane.getChildren().clear();
        if (produtos != null && !produtos.isEmpty()) {
            for (Produto p : produtos) {
                VBox productCard = createProductCard(p);
                productTilePane.getChildren().add(productCard);
            }
        } else {
            Label noProductsLabel = new Label("Nenhum produto encontrado.");
            noProductsLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #555555;");
            productTilePane.getChildren().add(noProductsLabel);
        }
    }

    private VBox createProductCard(Produto produto) {
        VBox card = new VBox(10);
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        card.setPrefWidth(280.0);
        card.setPrefHeight(380.0);
        card.setStyle("-fx-background-color: white; -fx-border-color: #00A60E; -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 15;");

        ImageView imageView = new ImageView(produto.getImage());
        imageView.setFitHeight(150.0);
        imageView.setFitWidth(180.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(produto.getNome());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 5px 0;");
        nameLabel.setWrapText(true);
        VBox.setMargin(nameLabel, new Insets(15, 0, 0, 0));

        Label priceLabel = new Label("R$ " + String.format("%.2f", produto.getPreco()).replace('.', ','));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00A60E;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button buyButton = new Button("Comprar");
        buyButton.setStyle("-fx-background-color: #00A60E; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 8px 16px; -fx-cursor: hand;");
        buyButton.setOnAction(event -> handleComprarProduto(produto));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, spacer, buyButton);
        return card;
    }

    private void handleComprarProduto(Produto produto) {
        if (this.usuarioLogado == null) {
            showAlert(AlertType.WARNING, "Login Necessário", "Você precisa estar logado para adicionar produtos ao carrinho.");
            return;
        }
        CarrinhoManager.getInstance().adicionarProduto(produto);
        showAlert(AlertType.INFORMATION, "Produto Adicionado", "Você adicionou '" + produto.getNome() + "' ao carrinho!");
    }

    @FXML
    private void handleCartButtonAction(ActionEvent event) {
        if (this.usuarioLogado == null) {
            showAlert(AlertType.WARNING, "Login Necessário", "Você precisa estar logado para visualizar o carrinho.");
            return;
        }
        NavigationManager.getInstance().navigateToCart();
    }

    @FXML
    private void handleHistoryButtonAction(ActionEvent event) {
        NavigationManager.getInstance().navigateToHistory();
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(productTilePane.getScene().getWindow());
        alert.showAndWait();
    }
    @FXML
    void handleLogout(ActionEvent event) {
        // Opcional: Mostrar uma confirmação
        AuthService.getInstance().logout();
        NavigationManager.getInstance().navigateToLogin();
    }
}