package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.EnderecoDAO;
import br.edu.ifpb.lojavirtual.model.Endereco;
import br.edu.ifpb.lojavirtual.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EnderecoFormController {
    @FXML private TextField ruaField, numeroField, bairroField, cidadeField, estadoField, cepField;
    private EnderecoDAO dao = new EnderecoDAO();

    @FXML
    private void handleSalvar() {
        try {
            Endereco e = new Endereco(ruaField.getText(), numeroField.getText(), "",
                    bairroField.getText(), cidadeField.getText(),
                    estadoField.getText(), cepField.getText());
            e.setIdUsuario(AuthService.getInstance().getUsuarioLogado().getId());
            dao.salvar(e);
            ((Stage) ruaField.getScene().getWindow()).close();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}