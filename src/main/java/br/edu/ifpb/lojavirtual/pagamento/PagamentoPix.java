package br.edu.ifpb.lojavirtual.pagamento;

import br.edu.ifpb.lojavirtual.model.Pedido;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.UUID;

public class PagamentoPix implements MetodoPagamento {

    @Override
    public String getNome() {
        return "PIX";
    }

    @Override
    public Node gerarComponenteVisual() {
        VBox layout = new VBox(15.0);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label instrucao1 = new Label("1. Escaneie o código QR com seu celular");
        instrucao1.setStyle("-fx-font-weight: bold;");

        ImageView qrCodeImageView = new ImageView();
        try {
            Image qrCode = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/br/edu/ifpb/lojavirtual/imagens/qr.jpeg")));
            qrCodeImageView.setImage(qrCode);
        } catch (Exception e) {
            System.err.println("Erro ao carregar QR: " + e.getMessage());
        }
        qrCodeImageView.setFitHeight(150.0);
        qrCodeImageView.setFitWidth(150.0);

        Label instrucao2 = new Label("2. Ou copie o código abaixo:");

        String payloadPix = "00020126330014BR.GOV.BCB.PIX0111" + UUID.randomUUID().toString().replace("-", "").substring(0,25).toUpperCase();
        TextField pixCodeTextField = new TextField(payloadPix);
        pixCodeTextField.setEditable(false);
        pixCodeTextField.setPrefWidth(300.0);

        Button copiarChaveButton = new Button("Copiar");
        copiarChaveButton.setStyle("-fx-background-color: #00A60E; -fx-text-fill: white; -fx-cursor: hand;");

        copiarChaveButton.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(pixCodeTextField.getText());
            clipboard.setContent(content);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setContentText("Código PIX copiado com sucesso!");
            alert.showAndWait();
        });

        HBox copyBox = new HBox(5.0, pixCodeTextField, copiarChaveButton);
        copyBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(instrucao1, qrCodeImageView, instrucao2, copyBox);
        return layout;
    }

    @Override
    public void processar(Pedido pedido) throws Exception {
        System.out.println("Pagamento PIX registrado para o pedido: " + pedido.getId());
    }
}