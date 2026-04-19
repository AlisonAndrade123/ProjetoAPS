package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.CatalogoDAO;
import br.edu.ifpb.lojavirtual.dao.CategoriaDAO;
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Catalogo;
import br.edu.ifpb.lojavirtual.model.Categoria;
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
import java.util.stream.Collectors;

public class AdminController {

    @FXML
    private TextField searchTextField;
    @FXML
    private Button addProductButton;
    @FXML
    private HBox categoryHBox;
    @FXML
    private TilePane productTilePane;
    @FXML
    private ComboBox<Catalogo> cbCatalogosAdmin;

    private Usuario adminLogado;
    private ProdutoDAO produtoDAO;
    private CatalogoDAO catalogoDAO = new CatalogoDAO();

    private Catalogo catalogoAtivo = null;
    private Categoria categoriaAtiva = null;
    private String termoBusca = "";

    @FXML
    private Label lblTituloSessao;
    @FXML
    private Button btnLimparFiltro;

    @FXML
    public void initialize() {
        this.produtoDAO = new ProdutoDAO();
        this.adminLogado = AuthService.getInstance().getUsuarioLogado();

        if (adminLogado == null || !adminLogado.isAdmin()) {
            NavigationManager.getInstance().navigateToLogin();
            return;
        }

        // Listener da Busca por Texto Unificado
        searchTextField.textProperty().addListener((obs, oldText, newText) -> {
            this.termoBusca = newText;
            aplicarFiltrosAdmin();
        });

        criarBotoesDeCategoria();
        carregarCatalogosNoMenu();

        // Carrega a tela inicialmente
        aplicarFiltrosAdmin();
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
            showAlert(Alert.AlertType.ERROR, "Erro Crítico", "Não foi possível carregar a tela de cadastro.");
        }

        // Atualiza a tela mantendo os filtros atuais após fechar o modal
        aplicarFiltrosAdmin();
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
        ButtonType buttonTypeNao = new ButtonType("Não");

        alert.getButtonTypes().setAll(buttonTypeSim, buttonTypeNao);

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == buttonTypeSim;
    }

    private void criarBotoesDeCategoria() {
        categoryHBox.getChildren().clear();

        Button todosButton = criarBotaoEstilizado("Todos", "Todos");
        categoryHBox.getChildren().add(todosButton);

        try {
            CategoriaDAO categoriaDAO = new CategoriaDAO();
            List<Categoria> categorias = categoriaDAO.findAll();

            for (Categoria categoria : categorias) {
                Button categoriaButton = criarBotaoEstilizado(categoria.getNome(), categoria);
                categoryHBox.getChildren().add(categoriaButton);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os botões de categoria.");
            e.printStackTrace();
        }
    }

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
        Object userData = clickedButton.getUserData();

        if (userData instanceof String && "Todos".equals(userData)) {
            this.categoriaAtiva = null; // Zera a categoria
        } else if (userData instanceof Categoria) {
            this.categoriaAtiva = (Categoria) userData; // Salva a categoria clicada
        }

        // Aplica o filtro unificado
        aplicarFiltrosAdmin();
    }

    private void handleRemoveProduct(Produto produto) {
        if (showConfirmacao("Deseja remover o produto '" + produto.getNome() + "'?")) {
            try {
                if (produtoDAO.delete(produto.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Remover Produto", "Produto '" + produto.getNome() + "' removido com sucesso!");
                    aplicarFiltrosAdmin(); // Atualiza a tela mantendo os filtros
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

    // =======================================================================
    // MOTOR DE FILTROS UNIFICADOS (IDÊNTICO AO DO CLIENTE)
    // =======================================================================
    private void aplicarFiltrosAdmin() {
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
                        // Removida a verificação da descrição para ficar igual ao cliente
                        .filter(p -> p.getNome().toLowerCase().contains(termo))
                        .collect(Collectors.toList());
            }

            // 4. Atualiza a tela
            displayProducts(produtos);
            atualizarFeedbackVisual();

        } catch (SQLException e) {
            System.err.println("Erro ao aplicar filtros cruzados no admin: " + e.getMessage());
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

    private void carregarCatalogosNoMenu() {
        try {
            List<Catalogo> catalogos = catalogoDAO.listarTodos();

            Catalogo todos = new Catalogo();
            todos.setNome("Todos os Produtos");
            catalogos.add(0, todos);

            cbCatalogosAdmin.getItems().setAll(catalogos);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar catálogos no menu: " + e.getMessage());
        }
    }

    @FXML
    public void filtrarPorCatalogo(ActionEvent event) {
        Catalogo selecionado = cbCatalogosAdmin.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        this.catalogoAtivo = selecionado;
        aplicarFiltrosAdmin();
    }

    @FXML
    void handleLogout(ActionEvent event) {
        AuthService.getInstance().logout();
        NavigationManager.getInstance().navigateToLogin();
    }

    @FXML
    public void handleManageCatalogs(javafx.event.ActionEvent event) {
        Window ownerWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        NavigationManager.getInstance().setupModal(
                "/br/edu/ifpb/lojavirtual/view/GerenciarCatalogoView.fxml",
                "Gerenciar Catálogos",
                ownerWindow
        );

        carregarCatalogosNoMenu();
        // Atualiza a tela caso um catálogo tenha sido alterado/excluído
        aplicarFiltrosAdmin();
    }

    @FXML
    public void limparFiltro(ActionEvent event) {
        // Limpa a interface
        cbCatalogosAdmin.getSelectionModel().clearSelection();
        if (searchTextField != null) {
            searchTextField.clear();
        }

        // Zera as variáveis de estado
        this.catalogoAtivo = null;
        this.categoriaAtiva = null;
        this.termoBusca = "";

        // Aplica (vai trazer tudo de volta)
        aplicarFiltrosAdmin();
    }

    private void atualizarFeedbackVisual() {
        if (lblTituloSessao == null || btnLimparFiltro == null) return;

        StringBuilder titulo = new StringBuilder();
        boolean temFiltro = false;

        // Monta o texto do Catálogo
        if (catalogoAtivo != null && !catalogoAtivo.getNome().equals("Todos os Produtos")) {
            titulo.append("Catálogo: ").append(catalogoAtivo.getNome());
            temFiltro = true;
        }

        // Monta o texto da Categoria
        if (categoriaAtiva != null) {
            if (temFiltro) titulo.append("  |  ");
            titulo.append("Categoria: ").append(categoriaAtiva.getNome());
            temFiltro = true;
        }

        // Se não tiver nenhum filtro
        if (!temFiltro) {
            titulo.append("Gerenciando Todos os Produtos");
        }

        lblTituloSessao.setText(titulo.toString());
        btnLimparFiltro.setVisible(temFiltro); // Mostra o botão vermelho só se tiver filtro
    }
}