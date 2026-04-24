package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.CatalogoDAO;
import br.edu.ifpb.lojavirtual.dao.CategoriaDAO;
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Catalogo;
import br.edu.ifpb.lojavirtual.model.Categoria;
import br.edu.ifpb.lojavirtual.model.Produto;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.service.AuthService;
import br.edu.ifpb.lojavirtual.util.CarrinhoManager;
import br.edu.ifpb.lojavirtual.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ProdutosController {

    @FXML private TextField searchTextField;
    @FXML private ScrollPane categoryScrollPane;
    @FXML private HBox categoryHBox;
    @FXML private TilePane productTilePane;
    @FXML private Button cartButton, historyButton, profileButton, btnLimparFiltro;
    @FXML private ComboBox<Catalogo> cbCatalogosCliente;
    @FXML private Label lblTituloSessao;

    private Catalogo catalogoAtivo = null;
    private Categoria categoriaAtiva = null;
    private Usuario usuarioLogado;
    private ProdutoDAO produtoDAO;
    private CatalogoDAO catalogoDAO = new CatalogoDAO();
    private String termoBusca = "";

    public Catalogo getCatalogoAtivo() {
        return catalogoAtivo;
    }

    public void setCatalogoAtivo(Catalogo catalogoAtivo) {
        this.catalogoAtivo = catalogoAtivo;
    }

    public Categoria getCategoriaAtiva() {
        return categoriaAtiva;
    }

    public void setCategoriaAtiva(Categoria categoriaAtiva) {
        this.categoriaAtiva = categoriaAtiva;
    }

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
            searchTextField.textProperty().addListener((obs, oldText, newText) -> {
                this.termoBusca = newText;
                aplicarFiltros();
            });
        }
        criarBotoesDeCategoria();
        loadAllProducts();
        carregarCatalogos();
    }

    @FXML
    private void handleMeusEnderecos(ActionEvent event) {
        if (this.usuarioLogado == null) {
            showAlert(AlertType.WARNING, "Login Necessário", "Faça login para gerenciar endereços.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/edu/ifpb/lojavirtual/view/MeusEnderecosView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Gerenciar Meus Endereços");
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void criarBotoesDeCategoria() {
        categoryHBox.getChildren().clear();
        Button todosButton = criarBotaoEstilizado("Todos", "Todos");
        categoryHBox.getChildren().add(todosButton);

        try {
            CategoriaDAO categoriaDAO = new CategoriaDAO();
            List<Categoria> categorias = categoriaDAO.findAll();

            for (Categoria cat : categorias) {
                categoryHBox.getChildren().add(criarBotaoEstilizado(cat.getNome(), cat));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Button criarBotaoEstilizado(String nome, Object userData) {
        Button button = new Button(nome);
        button.setUserData(userData);
        button.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #00A60E; -fx-border-radius: 20; -fx-background-radius: 20; -fx-border-width: 2; -fx-padding: 8 20; -fx-cursor: hand;");
        button.setOnAction(this::handleCategoryButtonAction);
        return button;
    }

    @FXML
    private void handleCategoryButtonAction(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        Object userData = clickedButton.getUserData();

        if (userData instanceof String && "Todos".equals(userData)) {
            setCategoriaAtiva(null);
        } else if (userData instanceof Categoria) {
            setCategoriaAtiva((Categoria) userData);
        }

        aplicarFiltros();
    }
    private void loadAllProducts() {
        try { displayProducts(produtoDAO.findAll()); } catch (SQLException e) { e.printStackTrace(); }
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
        Button detalhesButton = new Button("Avaliações");
        detalhesButton.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #00A60E; -fx-text-fill: #333; -fx-border-radius: 5; -fx-padding: 8px 16px; -fx-cursor: hand;");
        detalhesButton.setOnAction(event -> abrirModalDetalhes(produto));

        Button buyButton = new Button("Comprar");
        buyButton.setStyle("-fx-background-color: #00A60E; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 8px 16px; -fx-cursor: hand;");
        buyButton.setOnAction(event -> handleComprarProduto(produto));
        HBox botoesBox = new HBox(10, detalhesButton, buyButton);
        botoesBox.setAlignment(javafx.geometry.Pos.CENTER);

        card.getChildren().addAll(imageView, nameLabel, priceLabel, spacer, botoesBox);
        return card;
    }
    private void abrirModalDetalhes(Produto produto) {
        Window ownerWindow = productTilePane.getScene().getWindow();

        Object controller = NavigationManager.getInstance().setupModal(
                "/br/edu/ifpb/lojavirtual/view/ProdutoDetalhesView.fxml",
                "Detalhes do Produto",
                ownerWindow
        );

        if (controller instanceof ProdutoDetalhesController detalhesController) {
            detalhesController.setStage(detalhesController.getStage());
            detalhesController.inicializarDados(produto);

            Stage modalStage = detalhesController.getStage();
            if (modalStage != null) {
                modalStage.showAndWait();
            }
        } else {
            showAlert(AlertType.ERROR, "Erro", "Não foi possível abrir os detalhes do produto.");
        }
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
        if (productTilePane.getScene() != null) {
            alert.initOwner(productTilePane.getScene().getWindow());
        }
        alert.showAndWait();
    }

    @FXML
    void handleLogout(ActionEvent event) {
        AuthService.getInstance().logout();
        NavigationManager.getInstance().navigateToLogin();
    }

    private void carregarCatalogos() {
        try {
            List<Catalogo> catalogos = catalogoDAO.listarTodos();
            Catalogo todos = new Catalogo(); todos.setNome("Todos os Produtos");
            catalogos.add(0, todos);
            cbCatalogosCliente.getItems().setAll(catalogos);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void filtrarPorCatalogo(ActionEvent event) {
        this.catalogoAtivo = cbCatalogosCliente.getSelectionModel().getSelectedItem();
        aplicarFiltros();
    }

    @FXML
    public void limparFiltro(ActionEvent event) {
        cbCatalogosCliente.getSelectionModel().clearSelection();
        this.catalogoAtivo = null; this.categoriaAtiva = null; this.termoBusca = "";
        if (searchTextField != null) searchTextField.clear();
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        try {
            List<Produto> produtos;

            if (catalogoAtivo != null && !catalogoAtivo.getNome().equals("Todos os Produtos")) {
                produtos = produtoDAO.listarPorCatalogo(catalogoAtivo.getId());
            } else {
                produtos = produtoDAO.findAll();
            }

            if (categoriaAtiva != null) {
                produtos = produtos.stream()
                        .filter(p -> p.getCategoria() != null && p.getCategoria().getId().equals(categoriaAtiva.getId()))
                        .collect(Collectors.toList());
            }

            if (termoBusca != null && !termoBusca.trim().isEmpty()) {
                String termo = termoBusca.toLowerCase();
                produtos = produtos.stream()
                        .filter(p -> p.getNome().toLowerCase().contains(termo))
                        .collect(Collectors.toList());
            }

            displayProducts(produtos);
            atualizarFeedbackVisual();

        } catch (SQLException e) {
            System.err.println("Erro ao aplicar filtros cruzados: " + e.getMessage());
        }
    }

    private void atualizarFeedbackVisual() {
        StringBuilder titulo = new StringBuilder();
        boolean temFiltro = false;

        if (catalogoAtivo != null && !catalogoAtivo.getNome().equals("Todos os Produtos")) {
            titulo.append("Catálogo: ").append(catalogoAtivo.getNome());
            temFiltro = true;
        }

        if (categoriaAtiva != null) {
            if (temFiltro) titulo.append("  |  ");
            titulo.append("Categoria: ").append(categoriaAtiva.getNome());
            temFiltro = true;
        }

        if (!temFiltro) {
            titulo.append("Todos os Produtos");
        }

        lblTituloSessao.setText(titulo.toString());
        btnLimparFiltro.setVisible(temFiltro);
    }
}