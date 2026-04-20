package br.edu.ifpb.lojavirtual.controller;

import br.edu.ifpb.lojavirtual.dao.AvaliacaoDAO;
import br.edu.ifpb.lojavirtual.dao.PedidoDAO;
import br.edu.ifpb.lojavirtual.model.Avaliacao;
import br.edu.ifpb.lojavirtual.model.Produto;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProdutoDetalhesController {

    @FXML private ImageView produtoImageView;
    @FXML private Label nomeProdutoLabel;
    @FXML private Label precoProdutoLabel;
    @FXML private Label descricaoProdutoLabel;
    @FXML private VBox avaliacoesVBox;
    @FXML private ComboBox<Integer> notaComboBox;
    @FXML private TextArea comentarioTextArea;
    @FXML private VBox formAvaliacao; // A caixa que contém o formulário

    private Stage stage;
    private Produto produtoAtual;
    private final AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return this.stage;
    }

    @FXML
    public void initialize() {
        notaComboBox.getItems().addAll(1, 2, 3, 4, 5);
    }

    public void inicializarDados(Produto produto) {
        this.produtoAtual = produto;
        nomeProdutoLabel.setText(produto.getNome());
        precoProdutoLabel.setText(String.format("R$ %.2f", produto.getPreco()).replace('.', ','));
        descricaoProdutoLabel.setText(produto.getDescricao());
        produtoImageView.setImage(produto.getImage());

        carregarAvaliacoes();
        verificarPermissaoParaAvaliar(); // Chamada da regra de negócio
    }

    private void verificarPermissaoParaAvaliar() {
        Usuario logado = AuthService.getInstance().getUsuarioLogado();

        // 1. Se for Admin, ele não avalia (ele modera), então esconde o formulário
        if (logado == null || logado.isAdmin()) {
            formAvaliacao.setVisible(false);
            formAvaliacao.setManaged(false);
            return;
        }

        try {
            PedidoDAO pedidoDAO = new PedidoDAO();
            boolean jaComprou = pedidoDAO.jaComprouProduto(logado.getId(), produtoAtual.getId());

            if (jaComprou) {
                // Usuário comprou! Mostra o formulário
                formAvaliacao.setVisible(true);
                formAvaliacao.setManaged(true);
            } else {
                // Não comprou! Esconde o formulário e talvez mostre uma mensagem informativa
                formAvaliacao.setVisible(false);
                formAvaliacao.setManaged(false);

                // Opcional: Adicionar um aviso simples na lista de avaliações
                Label aviso = new Label("Você precisa comprar este produto para poder avaliá-lo.");
                aviso.setStyle("-fx-text-fill: #777; -fx-font-style: italic;");
                avaliacoesVBox.getChildren().add(0, aviso);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarAvaliacoes() {
        avaliacoesVBox.getChildren().clear();
        try {
            List<Avaliacao> avaliacoes = avaliacaoDAO.buscarPorProduto(produtoAtual.getId());

            if (avaliacoes.isEmpty()) {
                avaliacoesVBox.getChildren().add(new Label("Nenhuma avaliação ainda. Seja o primeiro a avaliar!"));
            } else {
                for (Avaliacao av : avaliacoes) {
                    VBox cardAvaliacao = criarCardAvaliacao(av);
                    avaliacoesVBox.getChildren().add(cardAvaliacao);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            avaliacoesVBox.getChildren().add(new Label("Erro ao carregar avaliações."));
        }
    }

    private VBox criarCardAvaliacao(Avaliacao av) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #E0E0E0;");

        // --- CABEÇALHO DO COMENTÁRIO ---
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Tratamento caso a avaliação seja muito antiga e não tenha nome de usuário
        String nomeAvaliador = (av.getNomeUsuario() != null) ? av.getNomeUsuario() : "Usuário Anônimo";
        Label infoLabel = new Label(nomeAvaliador + " - " + av.getDataAvaliacao());
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");

        header.getChildren().add(infoLabel);

        // --- MÁGICA AQUI: BOTÃO EXCLUIR SÓ PARA ADMIN ---
        Usuario logado = AuthService.getInstance().getUsuarioLogado();
        if (logado != null && logado.isAdmin()) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnExcluir = new Button("Excluir");
            btnExcluir.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 3 8; -fx-background-radius: 4;");

            // Ação de excluir no banco
            btnExcluir.setOnAction(e -> handleExcluirAvaliacao(av));

            header.getChildren().addAll(spacer, btnExcluir);
        }
        // --------------------------------------------------

        // Avaliação em Estrelas (Visual)
        String estrelas = "★".repeat(av.getNota()) + "☆".repeat(5 - av.getNota());
        Label notaLabel = new Label(estrelas);
        notaLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 16px;");

        Label comentario = new Label(av.getComentario());
        comentario.setWrapText(true);

        card.getChildren().addAll(header, notaLabel, comentario);
        return card;
    }

    // --- FUNÇÃO PARA O ADMIN EXCLUIR O COMENTÁRIO ---
    private void handleExcluirAvaliacao(Avaliacao avaliacao) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Excluir Avaliação");
        confirm.setHeaderText("Moderação de Comentário");
        confirm.setContentText("Deseja realmente excluir esta avaliação?");
        if (stage != null) confirm.initOwner(stage);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Deleta do banco de dados
                avaliacaoDAO.deletar(avaliacao.getId());

                // Recarrega a lista na tela para o comentário sumir imediatamente
                carregarAvaliacoes();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível excluir a avaliação no banco de dados.");
            }
        }
    }

    @FXML
    void handlePublicarAvaliacao(ActionEvent event) {
        Usuario logado = AuthService.getInstance().getUsuarioLogado();

        if (logado == null) {
            showAlert(Alert.AlertType.WARNING, "Login Necessário", "Você precisa estar logado para avaliar um produto.");
            return;
        }

        if (notaComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Nota Inválida", "Por favor, selecione uma nota de 1 a 5 estrelas.");
            return;
        }

        String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        Avaliacao novaAvaliacao = new Avaliacao(
                comentarioTextArea.getText(),
                notaComboBox.getValue(),
                dataAtual,
                logado.getId(),
                produtoAtual.getId()
        );

        try {
            avaliacaoDAO.salvar(novaAvaliacao);

            // Limpa os campos após publicar
            comentarioTextArea.clear();
            notaComboBox.setValue(null);

            // Atualiza a tela
            carregarAvaliacoes();
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Sua avaliação foi publicada com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu uma falha ao salvar sua avaliação.");
        }
    }

    private void showAlert(Alert.AlertType tipo, String titulo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        if (stage != null) alert.initOwner(stage);
        alert.showAndWait();
    }
}