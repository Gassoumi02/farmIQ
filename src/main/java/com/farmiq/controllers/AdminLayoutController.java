package com.farmiq.controllers;

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

public class AdminLayoutController {
    private static final Logger logger = LogManager.getLogger(AdminLayoutController.class);

    @FXML private StackPane contentArea;
    @FXML private Label lblCurrentUser;
    @FXML private Button btnNavUsers;
    @FXML private Button btnNavTransactions;
    @FXML private Label lblPageTitle;

    private enum ActiveSection { USERS, TRANSACTIONS }
    private ActiveSection currentSection = ActiveSection.USERS;

    @FXML
    public void initialize() {
        updateUserInfo();
        showUsers();
    }

    private void updateUserInfo() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null && lblCurrentUser != null) {
            lblCurrentUser.setText(user.getNom() + " | " + user.getRoleName());
        }
    }

    @FXML
    public void showUsers() {
        loadSection("/views/fxml/AdminUsers.fxml");
        currentSection = ActiveSection.USERS;
        updateNavHighlight();
        if (lblPageTitle != null) lblPageTitle.setText("Gestion des Utilisateurs");
    }

    @FXML
    public void showTransactions() {
        loadSection("/views/fxml/AdminTransactions.fxml");
        currentSection = ActiveSection.TRANSACTIONS;
        updateNavHighlight();
        if (lblPageTitle != null) lblPageTitle.setText("Gestion des Transactions");
    }

    private void loadSection(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            logger.error("Erreur chargement section: {}", fxmlPath, e);
            AlertUtil.showError("Erreur navigation", "Impossible de charger la section: " + e.getMessage());
        }
    }

    private void updateNavHighlight() {
        if (btnNavUsers != null) {
            btnNavUsers.getStyleClass().removeAll("nav-active");
            if (currentSection == ActiveSection.USERS) btnNavUsers.getStyleClass().add("nav-active");
        }
        if (btnNavTransactions != null) {
            btnNavTransactions.getStyleClass().removeAll("nav-active");
            if (currentSection == ActiveSection.TRANSACTIONS) btnNavTransactions.getStyleClass().add("nav-active");
        }
    }

    @FXML
    private void handleDeconnexion() {
        if (AlertUtil.showConfirmation("Déconnexion", "Voulez-vous vous déconnecter ?")) {
            SessionManager.getInstance().logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("FarmIQ - Connexion");
            } catch (IOException e) {
                logger.error("Erreur redirection login", e);
            }
        }
    }
}
