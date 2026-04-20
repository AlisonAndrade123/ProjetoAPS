package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.EnderecoDAO;
import br.edu.ifpb.lojavirtual.model.Endereco;
import br.edu.ifpb.lojavirtual.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MeusEnderecosController {

    @FXML private ListView<Endereco> enderecoListView;
    private final EnderecoDAO dao = new EnderecoDAO();

    @FXML
    public void initialize() {
        enderecoListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Endereco item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getRua() + ", " + item.getNumero() + " - " + item.getBairro() + " (" + item.getCidade() + ")");
                }
            }
        });
        carregarEnderecos();
    }

    private void carregarEnderecos() {
        try {
            enderecoListView.getItems().setAll(dao.buscarPorUsuario(AuthService.getInstance().getUsuarioLogado().getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdicionar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/edu/ifpb/lojavirtual/view/EnderecoForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Novo Endereço");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            carregarEnderecos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRemover() {
        Endereco sel = enderecoListView.getSelectionModel().getSelectedItem();
        if (sel != null) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente remover este endereço?", ButtonType.YES, ButtonType.NO);
            confirmacao.showAndWait();

            if (confirmacao.getResult() == ButtonType.YES) {
                try {
                    dao.remover(sel.getId());
                    carregarEnderecos();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Selecione um endereço para remover.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleFechar() {
        Stage stage = (Stage) enderecoListView.getScene().getWindow();
        stage.close();
    }
}