package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.CategoriaDAO; // Importe adicionado
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Categoria; // Importe adicionado
import br.edu.ifpb.lojavirtual.model.Produto;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.service.AuthService;
import br.edu.ifpb.lojavirtual.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminController {

    @FXML
    private TextField searchTextField;
    @FXML
    private Button addProductButton;
    @FXML
    private HBox categoryHBox;
    @FXML
    private TilePane productTilePane;

    private Usuario adminLogado;
    private ProdutoDAO produtoDAO;

    @FXML
    public void initialize() {
        this.produtoDAO = new ProdutoDAO();
        this.adminLogado = AuthService.getInstance().getUsuarioLogado();

        if (adminLogado == null || !adminLogado.isAdmin()) {
            NavigationManager.getInstance().navigateToLogin();
            return;
        }

        searchTextField.textProperty().addListener((obs, oldText, newText) -> filterProducts(newText));
        criarBotoesDeCategoria();
        loadAllProducts();
    }

    @FXML
    private void handleAddProduct(ActionEvent event) {
        abrirModalProduto(null);
    }

    private void handleEditProduct(Produto produto) {
        abrirModalProduto(produto);
    }

    private void abrirModalProduto(Produto produto) {
        Window ownerWindow = productTilePane.getScene().getWindow();

        Object controller = NavigationManager.getInstance().setupModal(
                "/br/edu/ifpb/lojavirtual/view/CadastrarProdutoView.fxml",
                (produto == null) ? "Cadastro de Novo Produto" : "Editar Produto",
                ownerWindow
        );

        if (controller instanceof CadastrarProdutoController cadastrarController) {
            if (produto != null) {
                cadastrarController.carregarDadosParaEdicao(produto);
            }

            Stage modalStage = cadastrarController.getStage();
            if (modalStage != null) {
                modalStage.showAndWait();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro Crítico", "Não foi possível carregar a tela de cadastro."); // Encoding corrigido
        }

        loadAllProducts();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(productTilePane.getScene().getWindow());
        alert.showAndWait();
    }

    private boolean showConfirmacao(String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remover produto");
        alert.setHeaderText(null);
        alert.setContentText(content);

        ButtonType buttonTypeSim = new ButtonType("Sim");
        ButtonType buttonTypeNao = new ButtonType("Não"); // Encoding corrigido

        alert.getButtonTypes().setAll(buttonTypeSim, buttonTypeNao);

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == buttonTypeSim;
    }

    private void criarBotoesDeCategoria() {
        categoryHBox.getChildren().clear();

        // Botão "Todos" recebe uma String como identificador (UserData)
        Button todosButton = criarBotaoEstilizado("Todos", "Todos");
        categoryHBox.getChildren().add(todosButton);

        try {
            // Busca as categorias reais no banco de dados
            CategoriaDAO categoriaDAO = new CategoriaDAO();
            List<Categoria> categorias = categoriaDAO.findAll();

            for (Categoria categoria : categorias) {
                // Passa o objeto Categoria inteiro como UserData do botão!
                Button categoriaButton = criarBotaoEstilizado(categoria.getNome(), categoria);
                categoryHBox.getChildren().add(categoriaButton);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os botões de categoria.");
            e.printStackTrace();
        }
    }

    // Método atualizado para receber o Object userData (pode ser String "Todos" ou um objeto Categoria)
    private Button criarBotaoEstilizado(String nome, Object userData) {
        Button button = new Button(nome);
        button.setUserData(userData);
        button.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #00A60E; -fx-border-radius: 20; -fx-border-width: 2; -fx-text-fill: #333333; -fx-padding: 8 20; -fx-cursor: hand;");
        button.setFont(new Font("System", 16.0));
        button.setOnAction(this::handleCategoryFilter);
        return button;
    }

    @FXML
    private void handleCategoryFilter(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        Object userData = clickedButton.getUserData(); // Pegamos o objeto salvo no botão

        if (userData instanceof String && "Todos".equals(userData)) {
            loadAllProducts();
        } else if (userData instanceof Categoria) {
            // Se for uma Categoria, pegamos o ID dela para filtrar no banco!
            Categoria categoriaSelecionada = (Categoria) userData;
            filterProductsByCategory(categoriaSelecionada.getId());
        }
    }

    private void handleRemoveProduct(Produto produto) {
        if (showConfirmacao("Deseja remover o produto '" + produto.getNome() + "'?")) {
            try {
                if (produtoDAO.delete(produto.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Remover Produto", "Produto '" + produto.getNome() + "' removido com sucesso!");
                    loadAllProducts();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Remover Produto", "Falha ao remover o produto.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Erro ao acessar o banco de dados.");
                e.printStackTrace();
            }
        }
    }

    private VBox createProductCard(Produto produto) {
        VBox card = new VBox(10);
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        card.setPrefHeight(400.0);
        card.setStyle("-fx-background-color: white; -fx-border-color: #00A60E; -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 15;");

        ImageView imageView = new ImageView(produto.getImage());
        imageView.setFitHeight(150.0);
        imageView.setFitWidth(180.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(produto.getNome());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 5px 0;");
        nameLabel.setWrapText(true);

        Label descriptionLabel = new Label(produto.getDescricao());
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-padding: 5px 0; -fx-text-fill: #555555;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefHeight(100);

        Label priceLabel = new Label("R$ " + String.format("%.2f", produto.getPreco()).replace('.', ','));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00A60E; -fx-padding: 5px 0 15px 0;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox actionButtons = new HBox(5);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER);

        Button removeButton = new Button("Remover");
        removeButton.setStyle("-fx-background-color: #F43C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 6px 12px; -fx-cursor: hand;");
        removeButton.setOnAction(e -> handleRemoveProduct(produto));

        Button editButton = new Button("Editar");
        editButton.setStyle("-fx-background-color: #00A60E; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 6px 12px; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditProduct(produto));

        actionButtons.getChildren().addAll(removeButton, editButton);

        card.getChildren().addAll(imageView, nameLabel, descriptionLabel, priceLabel, actionButtons);

        return card;
    }

    private void loadAllProducts() {
        if (produtoDAO != null) {
            try {
                List<Produto> produtos = produtoDAO.findAll();
                displayProducts(produtos);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
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
                showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // MÉTODO ATUALIZADO: Agora recebe o ID da Categoria em vez de uma String
    private void filterProductsByCategory(int idCategoria) {
        if (produtoDAO != null) {
            try {
                List<Produto> produtos = produtoDAO.findByCategoryId(idCategoria);
                displayProducts(produtos);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
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
            noProductsLabel.setFont(new Font(20));
            productTilePane.getChildren().add(noProductsLabel);
        }
    }
    @FXML
    void handleLogout(ActionEvent event) {
        AuthService.getInstance().logout(); // Limpa a sessão
        NavigationManager.getInstance().navigateToLogin(); // Volta para o login
    }
}