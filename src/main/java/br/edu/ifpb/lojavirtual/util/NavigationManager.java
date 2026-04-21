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
        if (primaryStage.getScene() == null) {
            return new Scene(root);
        } else {
            return new Scene(root, primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
        }
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

            if (controller instanceof CadastrarProdutoController cadastrarController) {
                cadastrarController.setStage(modalStage);
                cadastrarController.setProdutoDAO(new ProdutoDAO());
            }
            else if (controller instanceof br.edu.ifpb.lojavirtual.controller.GerenciarCatalogoController catalogoController) {
                catalogoController.setStage(modalStage);
            }
            else if (controller instanceof br.edu.ifpb.lojavirtual.controller.ProdutoDetalhesController detalhesController) {
                detalhesController.setStage(modalStage);
            }
            else if (controller instanceof br.edu.ifpb.lojavirtual.controller.GerenciarCategoriasController categoriaController) {
                categoriaController.setStage(modalStage);
            }

            else if (controller instanceof br.edu.ifpb.lojavirtual.controller.GerenciarPedidosController ordersController) {
                ordersController.setStage(modalStage);
            }

            return controller;
        } catch (IOException e) {
            System.err.println("Erro ao carregar o modal FXML: " + fxmlPath);
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

    public void navigateToGerenciarEnderecos() {
        navigateTo("/br/edu/ifpb/lojavirtual/view/GerenciarEnderecos.fxml", "Meus Endereços");
    }

    public void navigateToMeusEnderecos() {
        navigateTo("/br/edu/ifpb/lojavirtual/view/MeusEnderecosView.fxml", "Meus Endereços");
    }
}