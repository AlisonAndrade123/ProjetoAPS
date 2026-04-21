package br.edu.ifpb.lojavirtual.pagamento;

import br.edu.ifpb.lojavirtual.model.Pedido;
import br.edu.ifpb.lojavirtual.model.Usuario;
import br.edu.ifpb.lojavirtual.dao.UsuarioDAO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PagamentoBoleto implements MetodoPagamento {

    @Override
    public String getNome() {
        return "Boleto";
    }

    @Override
    public Node gerarComponenteVisual() {
        VBox layout = new VBox(20.0);
        layout.setPadding(new Insets(20));
        Label instrucao = new Label("Após finalizar a compra, seu boleto será gerado automaticamente em formato PDF.");
        instrucao.setWrapText(true);
        instrucao.setStyle("-fx-font-size: 14px;");
        Button btnInfo = new Button("Informações sobre o Pagamento");
        btnInfo.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #00A60E; -fx-cursor: hand;");
        layout.getChildren().addAll(instrucao, btnInfo);
        return layout;
    }

    @Override
    public void processar(Pedido pedido) throws Exception {
        gerarBoletoPDF(pedido);
    }

    private void gerarBoletoPDF(Pedido pedido) throws Exception {
        File diretorio = new File("boletos");
        if (!diretorio.exists()) diretorio.mkdir();

        String caminhoArquivo = "boletos/Boleto_Pedido_" + pedido.getId() + ".pdf";
        File file = new File(caminhoArquivo);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));

        document.open();

        com.lowagie.text.Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        com.lowagie.text.Font fonteNegrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        com.lowagie.text.Font fonteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph p = new Paragraph("BOLETO BANCÁRIO - LOJA VIRTUAL", fonteTitulo);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
        document.add(new Paragraph(" "));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 3);
        String dataVencimento = new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(new Phrase("Beneficiário: Loja Virtual APS S.A.", fonteNormal));
        table.addCell(new Phrase("Vencimento: " + dataVencimento, fonteNegrito));
        table.addCell(new Phrase("Pedido nº: " + pedido.getId(), fonteNormal));
        table.addCell(new Phrase("Valor do Documento: R$ " + String.format("%.2f", pedido.getValorTotal()), fonteNegrito));
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Linha Digitável:", fonteNegrito));
        document.add(new Paragraph("00190.00009 02305.050016 00000.000017 1 95430000135000", fonteNormal));
        document.add(new Paragraph("\n\nInstruções: Pague em qualquer banco ou casa lotérica até o vencimento.", fonteNormal));
        document.close();
        abrirArquivo(file);
    }

    private void abrirArquivo(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
            }
        } catch (Exception e) {
            System.err.println("Erro ao abrir o boleto: " + e.getMessage());
        }
    }
}