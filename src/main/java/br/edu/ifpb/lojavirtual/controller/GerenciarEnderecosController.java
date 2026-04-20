package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.EnderecoDAO;
import br.edu.ifpb.lojavirtual.model.Endereco;
import br.edu.ifpb.lojavirtual.service.AuthService;
import br.edu.ifpb.lojavirtual.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

public class GerenciarEnderecosController {
    @FXML private ListView<Endereco> listaEnderecos;
    private EnderecoDAO dao = new EnderecoDAO();

    @FXML
    public void initialize() {
        carregarLista();
        listaEnderecos.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Endereco item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getRua() + ", " + item.getNumero() + " - " + item.getCidade());
            }
        });
    }

    private void carregarLista() {
        try {
            listaEnderecos.getItems().setAll(dao.buscarPorUsuario(AuthService.getInstance().getUsuarioLogado().getId()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleAddEndereco() { /* Lógica para abrir um dialog de cadastro */ }
    @FXML private void handleRemover() { /* Lógica para deletar via DAO */ }
    @FXML private void handleVoltar() { NavigationManager.getInstance().navigateToProductsView(); }
}