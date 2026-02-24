package com.farmiq.controllers;

import com.farmiq.exceptions.AuthException;
import com.farmiq.models.User;
import com.farmiq.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @FXML private TextField emailField;

    // Password toggle pair
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button btnTogglePassword;

    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;

    private boolean passwordShown = false;
    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        if (passwordField != null && passwordVisible != null) {
            passwordField.textProperty().addListener((obs, o, n) -> {
                if (!passwordShown) passwordVisible.setText(n);
            });
            passwordVisible.textProperty().addListener((obs, o, n) -> {
                if (passwordShown) passwordField.setText(n);
            });
        }
    }

    @FXML
    private void handleTogglePassword() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            btnTogglePassword.setText("🙈");
            passwordVisible.requestFocus();
            passwordVisible.positionCaret(passwordVisible.getText().length());
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            btnTogglePassword.setText("👁");
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordShown ? passwordVisible.getText() : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        loginButton.setDisable(true);
        if (statusLabel != null) statusLabel.setText("Connexion en cours...");
        hideError();

        new Thread(() -> {
            try {
                User user = authService.login(email, password);
                javafx.application.Platform.runLater(() -> {
                    logger.info("Connexion réussie: {} ({})", user.getNom(), user.getRoleName());
                    navigateAfterLogin(user);
                });
            } catch (AuthException e) {
                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    if (statusLabel != null) statusLabel.setText("");
                    showError(e.getMessage());
                    logger.warn("Échec connexion: {}", e.getMessage());
                });
            }
        }).start();
    }

    private void navigateAfterLogin(User user) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            String css = getClass().getResource("/views/css/admin.css").toExternalForm();

            if (user.isAdmin()) {
                // ── ADMIN → Back-Office ──────────────────────────────────
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/AdminLayout.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1280, 800);
                scene.getStylesheets().add(css);
                stage.setScene(scene);
                stage.setTitle("FarmIQ - Back-Office Admin");
                stage.setResizable(true);
                stage.setMaximized(true);

            } else {
                // ── AGRICULTEUR / TECHNICIEN → UserLayout avec navbar ────
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Userlayout.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1200, 750);
                scene.getStylesheets().add(css);
                stage.setScene(scene);
                stage.setTitle("FarmIQ - Espace " + user.getRoleName());
                stage.setResizable(true);
            }

            stage.centerOnScreen();

        } catch (Exception e) {
            showError("Erreur navigation: " + e.getMessage());
            loginButton.setDisable(false);
            logger.error("Erreur navigation après login", e);
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FarmIQ - Inscription");
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
            logger.error("Erreur navigation vers inscription", e);
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText("⚠  " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}