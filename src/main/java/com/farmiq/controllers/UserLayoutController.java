package com.farmiq.controllers;

import com.farmiq.models.User;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class UserLayoutController {
    private static final Logger logger = LogManager.getLogger(UserLayoutController.class);

    @FXML private StackPane contentArea;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblPageTitle;

    // Nav buttons
    @FXML private Button btnNavTransactions;
    @FXML private Button btnNavPlantes;
    @FXML private Button btnNavProfil;
    @FXML private Button btnNavMarketplace;
    @FXML private Button btnNavCalendar;
    // Future modules — uncomment when ready:
    // @FXML private Button btnNavProduits;
    // @FXML private Button btnNavFournisseurs;
    // @FXML private Button btnNavRapports;
    // @FXML private Button btnNavStock;

    private enum Section { TRANSACTIONS, PLANTES, PROFIL, MARKETPLACE, CALENDAR, PRODUITS, FOURNISSEURS, RAPPORTS, STOCK }
    private Section currentSection = Section.TRANSACTIONS;

    @FXML
    public void initialize() {
        updateUserInfo();
        showTransactions(); // default landing page
    }

    // ─── User info ───────────────────────────────────────────────────────────

    private void updateUserInfo() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            if (lblUserName != null) lblUserName.setText(user.getNom());
            if (lblUserRole != null) lblUserRole.setText(user.getRoleName());
        }
    }

    // ─── Navigation handlers ─────────────────────────────────────────────────

    @FXML
    public void showTransactions() {
        loadSection("/views/fxml/Transactions.fxml", "💰 Transactions", Section.TRANSACTIONS);
    }

    @FXML
    public void showPlantes() {
        loadSection("/views/fxml/Plantes.fxml", "🌱 Mes Plantes", Section.PLANTES);
    }

    @FXML
    public void showProfil() {
        loadSection("/views/fxml/Profil.fxml", "⚙️ Mon Profil", Section.PROFIL);
    }
    
    @FXML
    public void showMarketplace() {
        loadSection("/views/fxml/Marketplace.fxml", "🛒 Marketplace", Section.MARKETPLACE);
    }
    
    @FXML
    public void showCalendar() {
        loadSection("/views/fxml/Calendar.fxml", "📅 Calendrier", Section.CALENDAR);
    }

    // ── Futurs modules — décommenter + créer le FXML correspondant ───────────

    // @FXML
    // public void showProduits() {
    //     loadSection("/views/fxml/Produits.fxml", "🌱 Produits", Section.PRODUITS);
    // }

    // @FXML
    // public void showFournisseurs() {
    //     loadSection("/views/fxml/Fournisseurs.fxml", "🏭 Fournisseurs", Section.FOURNISSEURS);
    // }

    // @FXML
    // public void showRapports() {
    //     loadSection("/views/fxml/Rapports.fxml", "📊 Rapports", Section.RAPPORTS);
    // }

    // @FXML
    // public void showStock() {
    //     loadSection("/views/fxml/Stock.fxml", "📦 Stock", Section.STOCK);
    // }

    // ─── Core loader ─────────────────────────────────────────────────────────

    /**
     * Charge un FXML dans la zone de contenu, met à jour le titre
     * et surligne le bouton actif dans la sidebar.
     */
    private void loadSection(String fxmlPath, String title, Section section) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().setAll(content);
            currentSection = section;
            if (lblPageTitle != null) lblPageTitle.setText(title);
            updateNavHighlight();
            logger.debug("Section chargée: {}", fxmlPath);
        } catch (IOException e) {
            logger.error("Erreur chargement section: {}", fxmlPath, e);
            AlertUtil.showError("Erreur navigation", "Impossible de charger: " + e.getMessage());
        }
    }

    /**
     * Surligne le bouton de la section active, retire la classe des autres.
     * Ajouter ici chaque nouveau bouton de module.
     */
    private void updateNavHighlight() {
        Button[] allBtns = {
                btnNavTransactions,
                btnNavPlantes,
                btnNavProfil,
                btnNavMarketplace,
                btnNavCalendar,
                // btnNavProduits,
                // btnNavFournisseurs,
                // btnNavRapports,
                // btnNavStock,
        };
        Section[] sections = {
                Section.TRANSACTIONS,
                Section.PLANTES,
                Section.PROFIL,
                Section.MARKETPLACE,
                Section.CALENDAR,
                // Section.PRODUITS,
                // Section.FOURNISSEURS,
                // Section.RAPPORTS,
                // Section.STOCK,
        };

        for (int i = 0; i < allBtns.length; i++) {
            if (allBtns[i] != null) {
                allBtns[i].getStyleClass().remove("nav-active");
                if (sections[i] == currentSection) {
                    allBtns[i].getStyleClass().add("nav-active");
                }
            }
        }
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    @FXML
    private void handleDeconnexion() {
        if (AlertUtil.showConfirmation("Déconnexion", "Voulez-vous vous déconnecter ?")) {
            SessionManager.getInstance().logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) contentArea.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(
                        getClass().getResource("/views/css/admin.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("FarmIQ - Connexion");
                stage.setResizable(false);
                stage.centerOnScreen();
                logger.info("Déconnexion utilisateur");
            } catch (IOException e) {
                logger.error("Erreur redirection login", e);
            }
        }
    }
}