package br.edu.ifpb.lojavirtual.util;

import br.edu.ifpb.lojavirtual.controller.CadastrarProdutoController;
import br.edu.ifpb.lojavirtual.controller.PagamentoController;
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

/**
 * Gerencia a navegação entre telas e a abertura de modais.
 */
public class NavigationManager {
    private static NavigationManager instance;
    private Stage primaryStage;

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Object navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            Scene scene = getScene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setMinWidth(960.0);
            primaryStage.setMinHeight(540.0);
            primaryStage.show();

            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Scene getScene(Parent root) {
        Scene scene;
        // Verifica se o Stage (janela) já tem uma Scene.
        if (primaryStage.getScene() == null) {
            scene = new Scene(root);
        } else {
            // Preserva o tamanho atual da janela ao navegar
            scene = new Scene(root, primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
        }
        return scene;
    }

    public Object setupModal(String fxmlPath, String title, Window ownerWindow) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            Stage modalStage = new Stage();
            modalStage.setTitle(title);
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.initOwner(ownerWindow);
            modalStage.initModality(Modality.APPLICATION_MODAL);

            // Verificações e configurações dos diferentes Modais do sistema
            if (controller instanceof CadastrarProdutoController cadastrarController) {
                cadastrarController.setStage(modalStage);
                cadastrarController.setProdutoDAO(new ProdutoDAO());
            }
            // Controller do Catálogo
            else if (controller instanceof br.edu.ifpb.lojavirtual.controller.GerenciarCatalogoController catalogoController) {
                catalogoController.setStage(modalStage);
            }
            // Controller de Detalhes/Avaliação do Produto
            else if (controller instanceof br.edu.ifpb.lojavirtual.controller.ProdutoDetalhesController detalhesController) {
                detalhesController.setStage(modalStage);
            }
            // NOVO: Controller de Gerenciamento de Categorias
            else if (controller instanceof br.edu.ifpb.lojavirtual.controller.GerenciarCategoriasController categoriaController) {
                categoriaController.setStage(modalStage);
            }

            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void navigateToLogin() {
        navigateTo("/br/edu/ifpb/lojavirtual/view/LoginView.fxml", "Sistema de Gerenciamento - Login");
    }

    public void navigateToAdminView() {
        navigateTo("/br/edu/ifpb/lojavirtual/view/AdminView.fxml", "Administração - Loja Virtual");
    }

    public void navigateToProductsView() {
        navigateTo("/br/edu/ifpb/lojavirtual/view/ProdutosView.fxml", "Nossa Loja");
    }

    public void navigateToCart() {
        navigateTo("/br/edu/ifpb/lojavirtual/view/CarrinhoView.fxml", "Meu Carrinho de Compras");
    }

    public void navigateToCadastro() {
        navigateTo("/br/edu/ifpb/lojavirtual/view/CadastroView.fxml", "Cadastro de Novo Usuário");
    }

    public PagamentoController navigateToPagamento() {
        Object controller = navigateTo("/br/edu/ifpb/lojavirtual/view/PagamentoView.fxml", "Finalizar Pagamento");
        return (controller instanceof PagamentoController) ? (PagamentoController) controller : null;
    }

    public void navigateToHistory() {
        navigateTo("/br/edu/ifpb/lojavirtual/view/HistoricoView.fxml", "Meu Histórico de Compras");
    }
}