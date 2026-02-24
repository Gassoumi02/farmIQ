package com.farmiq.controllers;

import com.farmiq.dao.TransactionDAO;
import com.farmiq.models.Transaction;
import com.farmiq.models.User;
import com.farmiq.services.TransactionService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TransactionController {
    private static final Logger logger = LogManager.getLogger(TransactionController.class);

    // ===== TABLE =====
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> colId;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, Double> colMontant;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colStatut;
    @FXML private TableColumn<Transaction, String> colUserNom;

    // ===== BUTTONS =====
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnDetails;
    @FXML private Button btnRapport;
    @FXML private Button btnExporter;
    @FXML private Button btnRafraichir;
    @FXML private Button btnDeconnexion;

    // ===== FILTERS =====
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private DatePicker filterDateDebut;
    @FXML private DatePicker filterDateFin;

    // ===== STATS =====
    @FXML private Label totalTransactionsLabel;
    @FXML private Label totalVentesLabel;
    @FXML private Label totalAchatsLabel;
    @FXML private Label beneficeLabel;
    @FXML private Label statusLabel;
    @FXML private Label selectionLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private StackPane loaderPane;

    // ===== DATA =====
    private TransactionDAO transactionDAO;
    private TransactionService transactionService;
    private ObservableList<Transaction> masterList;
    private FilteredList<Transaction> filteredList;
    private SortedList<Transaction> sortedList;
    private User currentUser;
    private boolean isAdmin;

    @FXML
    public void initialize() {
        transactionDAO = new TransactionDAO();
        transactionService = new TransactionService();
        masterList = FXCollections.observableArrayList();

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }
        isAdmin = currentUser.isAdmin();

        setupTable();
        setupFilters();
        setupSearch();
        updateUserLabels();
        loadTransactions();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // User column - only visible for admin
        if (colUserNom != null) {
            colUserNom.setVisible(isAdmin);
            colUserNom.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getUserNom() != null ? cd.getValue().getUserNom() : "—")
            );
        }

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add("VENTE".equals(item) ? "badge-actif" : "badge-inactif");
                setGraphic(badge); setText(null);
            }
        });

        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMontant.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f DT", item));
            }
        });

        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(col -> new TableCell<Transaction, LocalDate>() {
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(fmt));
            }
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        filteredList = new FilteredList<>(masterList, p -> true);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(transactionsTable.comparatorProperty());
        transactionsTable.setItems(sortedList);

        transactionsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean sel = n != null;
            if (btnModifier != null) btnModifier.setDisable(!sel);
            if (btnSupprimer != null) btnSupprimer.setDisable(!sel);
            if (btnDetails != null) btnDetails.setDisable(!sel);
            if (selectionLabel != null && sel)
                selectionLabel.setText("Sélection: " + n.getType() + " - " + String.format("%.2f DT", n.getMontant()));
        });
    }

    private void setupFilters() {
        if (filterTypeCombo != null) {
            filterTypeCombo.setItems(FXCollections.observableArrayList("Tous", "VENTE", "ACHAT"));
            filterTypeCombo.setValue("Tous");
            filterTypeCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        }
        if (filterStatutCombo != null) {
            filterStatutCombo.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE", "VALIDEE", "ANNULEE"));
            filterStatutCombo.setValue("Tous");
            filterStatutCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        }
        if (filterDateDebut != null) filterDateDebut.valueProperty().addListener((obs, o, n) -> applyFilters());
        if (filterDateFin != null) filterDateFin.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        }
    }

    private void applyFilters() {
        String search = searchField != null && searchField.getText() != null
            ? searchField.getText().toLowerCase().trim() : "";
        String type = filterTypeCombo != null ? filterTypeCombo.getValue() : "Tous";
        String statut = filterStatutCombo != null ? filterStatutCombo.getValue() : "Tous";
        LocalDate debut = filterDateDebut != null ? filterDateDebut.getValue() : null;
        LocalDate fin = filterDateFin != null ? filterDateFin.getValue() : null;

        filteredList.setPredicate(t -> {
            boolean matchSearch = search.isEmpty()
                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(search))
                || (t.getUserNom() != null && t.getUserNom().toLowerCase().contains(search));
            boolean matchType = "Tous".equals(type) || type.equals(t.getType());
            boolean matchStatut = "Tous".equals(statut) || statut.equals(t.getStatut());
            boolean matchDebut = debut == null || (t.getDate() != null && !t.getDate().isBefore(debut));
            boolean matchFin = fin == null || (t.getDate() != null && !t.getDate().isAfter(fin));
            return matchSearch && matchType && matchStatut && matchDebut && matchFin;
        });

        if (statusLabel != null) statusLabel.setText("Résultats: " + filteredList.size() + " transaction(s)");
    }

    private void loadTransactions() {
        setStatus("Chargement...");
        new Thread(() -> {
            try {
                List<Transaction> transactions = isAdmin
                    ? transactionDAO.findAllWithUserInfo()
                    : transactionDAO.findByUserId(currentUser.getId());

                Platform.runLater(() -> {
                    masterList.setAll(transactions);
                    updateStats();
                    applyFilters();
                    setStatus("✅ " + transactions.size() + " transaction(s) chargée(s)");
                    logger.info("Transactions chargées: {}", transactions.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setStatus("❌ Erreur: " + e.getMessage());
                    AlertUtil.showError("Erreur", "Impossible de charger les transactions: " + e.getMessage());
                    logger.error("Erreur chargement transactions", e);
                });
            }
        }).start();
    }

    private void updateStats() {
        try {
            double ventes = isAdmin ? transactionDAO.getTotalVentes() : transactionDAO.getTotalVentes(currentUser.getId());
            double achats = isAdmin ? transactionDAO.getTotalAchats() : transactionDAO.getTotalAchats(currentUser.getId());
            double benefice = ventes - achats;

            if (totalTransactionsLabel != null) totalTransactionsLabel.setText(String.valueOf(masterList.size()));
            if (totalVentesLabel != null) totalVentesLabel.setText(String.format("%.2f", ventes));
            if (totalAchatsLabel != null) totalAchatsLabel.setText(String.format("%.2f", achats));
            if (beneficeLabel != null) {
                beneficeLabel.setText(String.format("%.2f", benefice));
                beneficeLabel.setStyle(benefice >= 0 ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            }
        } catch (Exception e) {
            logger.warn("Erreur calcul stats", e);
        }
    }

    private void updateUserLabels() {
        if (currentUser != null) {
            if (userNameLabel != null) userNameLabel.setText(currentUser.getNom());
            if (userRoleLabel != null) userRoleLabel.setText(currentUser.getRoleName());
        }
    }

    @FXML
    private void handleAjouter() {
        showTransactionDialog(null);
    }

    @FXML
    private void handleModifier() {
        Transaction sel = transactionsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.showWarning("Sélection", "Veuillez sélectionner une transaction"); return; }
        if (!isAdmin && sel.getUserId() != currentUser.getId()) {
            AlertUtil.showError("Accès refusé", "Vous ne pouvez modifier que vos propres transactions");
            return;
        }
        showTransactionDialog(sel);
    }

    @FXML
    private void handleSupprimer() {
        Transaction sel = transactionsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.showWarning("Sélection", "Veuillez sélectionner une transaction"); return; }
        if (!isAdmin && sel.getUserId() != currentUser.getId()) {
            AlertUtil.showError("Accès refusé", "Vous ne pouvez supprimer que vos propres transactions");
            return;
        }
        if (!AlertUtil.showConfirmation("Suppression", "Supprimer cette transaction ?")) return;
        try {
            transactionDAO.delete(sel.getId());
            masterList.remove(sel);
            updateStats();
            setStatus("✅ Transaction supprimée");
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleDetails() {
        Transaction sel = transactionsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la transaction");
        alert.setHeaderText("Transaction #" + sel.getId());
        alert.setContentText(
            "Type: " + sel.getType() + "\n" +
            "Montant: " + String.format("%.2f DT", sel.getMontant()) + "\n" +
            "Date: " + (sel.getDate() != null ? sel.getDate().toString() : "N/A") + "\n" +
            "Statut: " + sel.getStatut() + "\n" +
            "Description: " + (sel.getDescription() != null ? sel.getDescription() : "N/A") + "\n" +
            (isAdmin ? "Utilisateur: " + sel.getUserNom() : "")
        );
        alert.showAndWait();
    }

    @FXML
    private void handleExporter() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les transactions en CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fileChooser.setInitialFileName("transactions.csv");
        File file = fileChooser.showSaveDialog(transactionsTable.getScene().getWindow());
        if (file != null) {
            try {
                transactionService.exportToCSV(sortedList, file.getAbsolutePath());
                AlertUtil.showSuccess("Export", "Transactions exportées: " + file.getName());
                setStatus("✅ Exporté: " + file.getName());
            } catch (Exception e) {
                AlertUtil.showError("Erreur export", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRapport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Générer rapport PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName("rapport_transactions.pdf");
        File file = fileChooser.showSaveDialog(transactionsTable.getScene().getWindow());
        if (file != null) {
            try {
                transactionService.exportToPDF(sortedList, file.getAbsolutePath());
                AlertUtil.showSuccess("PDF", "Rapport généré: " + file.getName());
                setStatus("✅ PDF généré: " + file.getName());
            } catch (Exception e) {
                AlertUtil.showError("Erreur PDF", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRafraichir() {
        if (searchField != null) searchField.clear();
        if (filterTypeCombo != null) filterTypeCombo.setValue("Tous");
        if (filterStatutCombo != null) filterStatutCombo.setValue("Tous");
        if (filterDateDebut != null) filterDateDebut.setValue(null);
        if (filterDateFin != null) filterDateFin.setValue(null);
        loadTransactions();
    }

    @FXML
    private void handleDeconnexion() {
        if (AlertUtil.showConfirmation("Déconnexion", "Voulez-vous vous déconnecter ?")) {
            SessionManager.getInstance().logout();
            redirectToLogin();
        }
    }

    private void showTransactionDialog(Transaction transaction) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(transaction == null ? "Ajouter une transaction" : "Modifier la transaction");
        dialog.setHeaderText(null);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(16));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("VENTE", "ACHAT"));
        TextField montantField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField descField = new TextField();
        ComboBox<String> statutCombo = new ComboBox<>(FXCollections.observableArrayList("EN_ATTENTE", "VALIDEE", "ANNULEE"));

        typeCombo.setValue(transaction != null ? transaction.getType() : "VENTE");
        montantField.setText(transaction != null ? String.valueOf(transaction.getMontant()) : "");
        if (transaction != null && transaction.getDate() != null) datePicker.setValue(transaction.getDate());
        descField.setText(transaction != null && transaction.getDescription() != null ? transaction.getDescription() : "");
        statutCombo.setValue(transaction != null ? transaction.getStatut() : "EN_ATTENTE");

        grid.add(new Label("Type *:"), 0, 0); grid.add(typeCombo, 1, 0);
        grid.add(new Label("Montant (DT) *:"), 0, 1); grid.add(montantField, 1, 1);
        grid.add(new Label("Date *:"), 0, 2); grid.add(datePicker, 1, 2);
        grid.add(new Label("Statut:"), 0, 3); grid.add(statutCombo, 1, 3);
        grid.add(new Label("Description:"), 0, 4); grid.add(descField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double montant = Double.parseDouble(montantField.getText().trim().replace(",", "."));
                if (montant <= 0) { AlertUtil.showError("Erreur", "Le montant doit être positif"); return; }

                if (transaction == null) {
                    Transaction newT = new Transaction();
                    newT.setUserId(currentUser.getId());
                    newT.setType(typeCombo.getValue());
                    newT.setMontant(montant);
                    newT.setDate(datePicker.getValue());
                    newT.setDescription(descField.getText());
                    newT.setStatut(statutCombo.getValue());
                    transactionDAO.create(newT);
                } else {
                    transaction.setType(typeCombo.getValue());
                    transaction.setMontant(montant);
                    transaction.setDate(datePicker.getValue());
                    transaction.setDescription(descField.getText());
                    transaction.setStatut(statutCombo.getValue());
                    transactionDAO.update(transaction);
                }
                loadTransactions();
                setStatus("✅ Transaction " + (transaction == null ? "ajoutée" : "modifiée"));
            } catch (NumberFormatException e) {
                AlertUtil.showError("Erreur", "Montant invalide. Entrez un nombre décimal.");
            } catch (Exception e) {
                AlertUtil.showError("Erreur", e.getMessage());
            }
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (transactionsTable != null && transactionsTable.getScene() != null
                ? transactionsTable.getScene().getWindow() : null);
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("FarmIQ - Connexion");
            }
        } catch (Exception e) {
            logger.error("Erreur redirection login", e);
        }
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}
