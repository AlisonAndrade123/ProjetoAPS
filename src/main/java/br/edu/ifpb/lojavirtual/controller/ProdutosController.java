package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.CatalogoDAO;
import br.edu.ifpb.lojavirtual.dao.CategoriaDAO; // Importe adicionado
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Catalogo;
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
import java.util.stream.Collectors;

public class ProdutosController {

    @FXML private TextField searchTextField;
    @FXML private ScrollPane categoryScrollPane;
    @FXML private HBox categoryHBox;
    @FXML private TilePane productTilePane;
    @FXML private Button cartButton;
    @FXML private Button historyButton;
    @FXML private Button profileButton;
    @FXML private ComboBox<Catalogo> cbCatalogosCliente;
    @FXML private Label lblTituloSessao;
    @FXML private Button btnLimparFiltro;

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
        button.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #00A60E; -fx-border-radius: 20; -fx-background-radius: 20; -fx-border-width: 2; -fx-text-fill: #333333; -fx-padding: 8 20; -fx-cursor: hand;");
        button.setFont(new Font("System", 16.0));
        button.setOnAction(this::handleCategoryButtonAction);
        return button;
    }

    @FXML
    private void handleCategoryButtonAction(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        Object userData = clickedButton.getUserData();

        // 1. Atualiza o estado da Categoria Ativa
        if (userData instanceof String && "Todos".equals(userData)) {
            setCategoriaAtiva(null); // Clicou em "Todos", então não tem categoria específica
        } else if (userData instanceof Categoria) {
            setCategoriaAtiva((Categoria) userData); // Salva a categoria clicada
        }

        // 2. Manda cruzar os dados (Catálogo atual + Categoria clicada)
        aplicarFiltros();
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

    private void carregarCatalogos() {
        try {
            List<Catalogo> catalogos = catalogoDAO.listarTodos();

            // Opção para o cliente voltar a ver todos os produtos
            Catalogo todos = new Catalogo();
            todos.setNome("Todos os Produtos");
            catalogos.add(0, todos);

            cbCatalogosCliente.getItems().setAll(catalogos);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar os catálogos: " + e.getMessage());
        }
    }

    @FXML
    public void filtrarPorCatalogo(ActionEvent event) {
        Catalogo selecionado = cbCatalogosCliente.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        // Apenas salva a escolha e manda aplicar!
        this.catalogoAtivo = selecionado;
        aplicarFiltros();
    }

    @FXML
    public void limparFiltro(ActionEvent event) {
        cbCatalogosCliente.getSelectionModel().clearSelection();

        // Zera TODOS os filtros salvos
        this.catalogoAtivo = null;
        this.categoriaAtiva = null;
        this.termoBusca = ""; // Zera o texto no estado

        if (searchTextField != null) {
            searchTextField.clear(); // Apaga o texto visualmente da barra
        }

        aplicarFiltros();
    }

    private void aplicarFiltros() {
        try {
            List<Produto> produtos;

            // 1. Busca os produtos base (Catálogo ativo ou Todos)
            if (catalogoAtivo != null && !catalogoAtivo.getNome().equals("Todos os Produtos")) {
                produtos = produtoDAO.listarPorCatalogo(catalogoAtivo.getId());
            } else {
                produtos = produtoDAO.findAll();
            }

            // 2. Cruza com a Categoria (se houver alguma selecionada)
            if (categoriaAtiva != null) {
                produtos = produtos.stream()
                        .filter(p -> p.getCategoria() != null && p.getCategoria().getId() == categoriaAtiva.getId())
                        .collect(Collectors.toList());
            }

            // 3. Cruza com o Texto de Busca (se houver algo digitado)
            if (termoBusca != null && !termoBusca.trim().isEmpty()) {
                String termo = termoBusca.toLowerCase();
                produtos = produtos.stream()
                        .filter(p -> p.getNome().toLowerCase().contains(termo))
                        .collect(Collectors.toList());
            }

            // 4. Atualiza a tela
            displayProducts(produtos);
            atualizarFeedbackVisual();

        } catch (SQLException e) {
            System.err.println("Erro ao aplicar filtros cruzados: " + e.getMessage());
        }
    }

    private void atualizarFeedbackVisual() {
        StringBuilder titulo = new StringBuilder();
        boolean temFiltro = false;

        // Monta o texto do Catálogo
        if (catalogoAtivo != null && !catalogoAtivo.getNome().equals("Todos os Produtos")) {
            titulo.append("Catálogo: ").append(catalogoAtivo.getNome());
            temFiltro = true;
        }

        // Monta o texto da Categoria
        if (categoriaAtiva != null) {
            if (temFiltro) titulo.append("  |  "); // Separador se já tiver catálogo
            titulo.append("Categoria: ").append(categoriaAtiva.getNome());
            temFiltro = true;
        }

        // Se não tiver nenhum filtro
        if (!temFiltro) {
            titulo.append("Todos os Produtos");
        }

        lblTituloSessao.setText(titulo.toString());
        btnLimparFiltro.setVisible(temFiltro);
    }

}