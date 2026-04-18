package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.CatalogoDAO;
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Catalogo;
import br.edu.ifpb.lojavirtual.model.Produto;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciarCatalogoController {

    @FXML private TextField txtNomeCatalogo;
    @FXML private ComboBox<Catalogo> cbCatalogos;
    @FXML private ListView<Produto> listDisponiveis;
    @FXML private ListView<Produto> listVinculados;
    private javafx.stage.Stage stage;


    private CatalogoDAO catalogoDAO = new CatalogoDAO();
    private ProdutoDAO produtoDAO = new ProdutoDAO();

    @FXML
    public void initialize() {
        carregarComboCatalogos();
    }

    public void setStage(javafx.stage.Stage stage) {
        this.stage = stage;
        // Exibe a janela como modal (bloqueando a tela de trás)
        this.stage.showAndWait();
    }

    private void carregarComboCatalogos() {
        try {
            List<Catalogo> catalogos = catalogoDAO.listarTodos();
            cbCatalogos.setItems(FXCollections.observableArrayList(catalogos));
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao carregar catálogos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void criarCatalogo(ActionEvent event) {
        String nome = txtNomeCatalogo.getText();
        if (nome == null || nome.trim().isEmpty()) {
            mostrarAlerta("Aviso", "Digite um nome para o catálogo.", Alert.AlertType.WARNING);
            return;
        }
        try {
            catalogoDAO.salvar(new Catalogo(nome));
            txtNomeCatalogo.clear();
            carregarComboCatalogos();
            mostrarAlerta("Sucesso", "Catálogo criado!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao salvar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void aoSelecionarCatalogo(ActionEvent event) {
        atualizarListasDeProdutos();
    }

    private void atualizarListasDeProdutos() {
        Catalogo selecionado = cbCatalogos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            listDisponiveis.getItems().clear();
            listVinculados.getItems().clear();
            return;
        }

        try {
            // Pega todos os produtos e os produtos que já estão neste catálogo
            List<Produto> todosProdutos = produtoDAO.findAll();
            List<Produto> produtosVinculados = produtoDAO.listarPorCatalogo(selecionado.getId());

            // Filtra os disponíveis (aqueles que NÃO estão na lista de vinculados)
            List<Integer> idsVinculados = produtosVinculados.stream().map(Produto::getId).toList();
            List<Produto> produtosDisponiveis = todosProdutos.stream()
                    .filter(p -> !idsVinculados.contains(p.getId()))
                    .collect(Collectors.toList());

            // Atualiza a tela
            listVinculados.setItems(FXCollections.observableArrayList(produtosVinculados));
            listDisponiveis.setItems(FXCollections.observableArrayList(produtosDisponiveis));

        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao carregar produtos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void adicionarProduto(ActionEvent event) {
        Catalogo catalogo = cbCatalogos.getSelectionModel().getSelectedItem();
        Produto produto = listDisponiveis.getSelectionModel().getSelectedItem();

        if (catalogo == null || produto == null) return;

        try {
            catalogoDAO.vincularProduto(catalogo.getId(), produto.getId());
            atualizarListasDeProdutos(); // Recarrega as listas instantaneamente
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao adicionar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void removerProduto(ActionEvent event) {
        Catalogo catalogo = cbCatalogos.getSelectionModel().getSelectedItem();
        Produto produto = listVinculados.getSelectionModel().getSelectedItem();

        if (catalogo == null || produto == null) return;

        try {
            catalogoDAO.desvincularProduto(catalogo.getId(), produto.getId());
            atualizarListasDeProdutos(); // Recarrega as listas instantaneamente
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao remover: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}