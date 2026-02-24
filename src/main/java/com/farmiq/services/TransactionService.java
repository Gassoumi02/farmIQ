package com.farmiq.services;

import com.farmiq.models.Transaction;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionService {
    private static final Logger logger = LogManager.getLogger(TransactionService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public List<Transaction> getRecentTransactions(int limit) {
        try {
            com.farmiq.dao.TransactionDAO dao = new com.farmiq.dao.TransactionDAO();
            List<Transaction> all = dao.findAllWithUserInfo();
            return all.stream().limit(limit).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            logger.error("Erreur chargement transactions récentes", e);
            return new java.util.ArrayList<>();
        }
    }

    public void exportToCSV(List<Transaction> transactions, String filepath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write("ID;Type;Montant (DT);Date;Statut;Description;Créé le");
            writer.newLine();
            for (Transaction t : transactions) {
                writer.write(
                    t.getId() + ";" +
                    t.getType() + ";" +
                    String.format("%.2f", t.getMontant()) + ";" +
                    (t.getDate() != null ? t.getDate().format(DATE_FMT) : "") + ";" +
                    t.getStatut() + ";" +
                    (t.getDescription() != null ? t.getDescription().replace(";", ",") : "") + ";" +
                    (t.getCreatedAt() != null ? t.getCreatedAt().format(DATETIME_FMT) : "")
                );
                writer.newLine();
            }
            logger.info("Export CSV: {} lignes vers {}", transactions.size(), filepath);
        }
    }

    public void exportToPDF(List<Transaction> transactions, String filepath) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(filepath));
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(45, 90, 45));
        Paragraph title = new Paragraph("FarmIQ - Rapport des Transactions", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        document.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
        Paragraph datePara = new Paragraph("Généré le: " + LocalDate.now().format(DATE_FMT) + " | Total: " + transactions.size() + " transactions", subFont);
        datePara.setAlignment(Element.ALIGN_CENTER);
        datePara.setSpacingAfter(16);
        document.add(datePara);

        // Table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.7f, 1.2f, 1.4f, 1.3f, 1.3f, 3.1f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        BaseColor headerColor = new BaseColor(45, 90, 45);
        String[] headers = {"ID", "Type", "Montant (DT)", "Date", "Statut", "Description"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(7);
            table.addCell(cell);
        }

        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        boolean odd = true;
        double totalVentes = 0, totalAchats = 0;

        for (Transaction t : transactions) {
            BaseColor rowColor = odd ? BaseColor.WHITE : new BaseColor(245, 248, 245);
            odd = !odd;

            addCell(table, String.valueOf(t.getId()), rowFont, rowColor, Element.ALIGN_CENTER);
            BaseColor typeColor = "VENTE".equals(t.getType()) ? new BaseColor(39, 174, 96) : new BaseColor(231, 76, 60);
            PdfPCell typeCell = new PdfPCell(new Phrase(t.getType(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, typeColor)));
            typeCell.setBackgroundColor(rowColor); typeCell.setPadding(6);
            typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(typeCell);
            addCell(table, String.format("%.2f", t.getMontant()), rowFont, rowColor, Element.ALIGN_RIGHT);
            addCell(table, t.getDate() != null ? t.getDate().format(DATE_FMT) : "", rowFont, rowColor, Element.ALIGN_CENTER);
            addCell(table, t.getStatut() != null ? t.getStatut() : "", rowFont, rowColor, Element.ALIGN_CENTER);
            addCell(table, t.getDescription() != null ? t.getDescription() : "", rowFont, rowColor, Element.ALIGN_LEFT);

            if ("VENTE".equals(t.getType()) && !"ANNULEE".equals(t.getStatut())) totalVentes += t.getMontant();
            if ("ACHAT".equals(t.getType()) && !"ANNULEE".equals(t.getStatut())) totalAchats += t.getMontant();
        }
        document.add(table);

        // Summary
        Font sumFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
        Paragraph summary = new Paragraph(
            String.format("\nTotal Ventes: %.2f DT    |    Total Achats: %.2f DT    |    Bénéfice: %.2f DT",
                totalVentes, totalAchats, totalVentes - totalAchats), sumFont);
        summary.setAlignment(Element.ALIGN_CENTER);
        summary.setSpacingBefore(16);
        document.add(summary);

        document.close();
        logger.info("Export PDF: {} transactions vers {}", transactions.size(), filepath);
    }

    private void addCell(PdfPTable table, String text, Font font, BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }
}
