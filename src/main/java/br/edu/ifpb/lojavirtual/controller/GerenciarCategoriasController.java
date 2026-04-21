package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.CategoriaDAO;
import br.edu.ifpb.lojavirtual.dao.ProdutoDAO;
import br.edu.ifpb.lojavirtual.model.Categoria;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class GerenciarCategoriasController {

    @FXML private TextField nomeCategoriaField;
    @FXML private TableView<Categoria> tabelaCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNome;
    @FXML private TableColumn<Categoria, Categoria> colAcoes;
    @FXML private Button btnSalvar;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Categoria categoriaEmEdicao = null;
    private Stage stage;

    public void setStage(Stage stage) { this.stage = stage; }
    public Stage getStage() { return this.stage; }

    @FXML
    public void initialize() {
        configurarTabela();
        carregarCategorias();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        colNome.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNome()));

        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button delBtn = new Button("Excluir");
            private final HBox pane = new HBox(8, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color: #ffc107; -fx-cursor: hand; -fx-font-weight: bold;");
                delBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

                editBtn.setOnAction(e -> prepararEdicao(getItem()));
                delBtn.setOnAction(e -> handleExcluir(getItem()));
            }

            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void carregarCategorias() {
        try {
            tabelaCategorias.setItems(FXCollections.observableArrayList(categoriaDAO.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSalvar(ActionEvent event) {
        String nome = nomeCategoriaField.getText().trim();
        if (nome.isEmpty()) {
            showAlert("Campo Vazio", "Por favor, digite um nome para a categoria.");
            return;
        }

        try {
            if (categoriaEmEdicao == null) {
                categoriaDAO.salvar(new Categoria(null, nome));
            } else {
                categoriaEmEdicao.setNome(nome);
                categoriaDAO.atualizar(categoriaEmEdicao);
                categoriaEmEdicao = null;
                btnSalvar.setText("Adicionar");
            }
            nomeCategoriaField.clear();
            carregarCategorias();

        } catch (SQLException e) {
            showAlert("Erro", "Não foi possível salvar. Verifique se o nome já existe.");
        }
    }

    private void prepararEdicao(Categoria c) {
        categoriaEmEdicao = c;
        nomeCategoriaField.setText(c.getNome());
        btnSalvar.setText("Atualizar");
        nomeCategoriaField.requestFocus();
    }

    private void handleExcluir(Categoria c) {
        try {
            ProdutoDAO produtoDAO = new ProdutoDAO();
            int quantidadeProdutos = produtoDAO.contarProdutosPorCategoria(c.getId());
            if (quantidadeProdutos > 0) {
                Alert alertErro = new Alert(Alert.AlertType.ERROR);
                alertErro.setTitle("Bloqueio de Exclusão");
                alertErro.setHeaderText("Não é possível excluir esta categoria!");
                alertErro.setContentText("A categoria '" + c.getNome() + "' possui " +
                        quantidadeProdutos + " produto(s) cadastrado(s).\n\n" +
                        "Para excluí-la, você deve primeiro remover os produtos ou mudar a categoria deles.");
                if (stage != null) alertErro.initOwner(stage);
                alertErro.showAndWait();
                return;
            }
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente excluir a categoria '" + c.getNome() + "'?", ButtonType.YES, ButtonType.NO);
            alertConfirm.setTitle("Confirmar Exclusão");
            alertConfirm.setHeaderText(null);
            if (stage != null) alertConfirm.initOwner(stage);

            if (alertConfirm.showAndWait().get() == ButtonType.YES) {
                if (categoriaDAO.excluir(c.getId())) {
                    carregarCategorias();
                    if (categoriaEmEdicao != null && categoriaEmEdicao.getId().equals(c.getId())) {
                        handleLimparForm();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erro de Banco", "Ocorreu um erro ao verificar os produtos vinculados.");
        }
    }
    private void handleLimparForm() {
        categoriaEmEdicao = null;
        nomeCategoriaField.clear();
        btnSalvar.setText("Adicionar");
    }

    @FXML void handleFechar() {
        if (stage != null) stage.close();
    }

    private void showAlert(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}