package com.farmiq.controllers;

import com.farmiq.dao.RoleDAO;
import com.farmiq.models.Role;
import com.farmiq.services.UserService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.ValidationUtil;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegisterController {
    private static final Logger logger = LogManager.getLogger(RegisterController.class);

    @FXML private TextField fieldNom;
    @FXML private TextField fieldEmail;

    // Password toggle pair
    @FXML private PasswordField fieldPassword;
    @FXML private TextField fieldPasswordVisible;
    @FXML private Button btnTogglePassword;

    // Confirm toggle pair
    @FXML private PasswordField fieldConfirm;
    @FXML private TextField fieldConfirmVisible;
    @FXML private Button btnToggleConfirm;

    @FXML private Button btnRegister;
    @FXML private Label errNom;
    @FXML private Label errEmail;
    @FXML private Label errPassword;
    @FXML private Label errConfirm;
    @FXML private Label statusLabel;

    private boolean passwordShown = false;
    private boolean confirmShown  = false;
    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService();
        setupSync();
        setupValidation();
    }

    // ─── Sync each PasswordField ↔ TextField pair ────────────────────────────

    private void setupSync() {
        if (fieldPassword != null && fieldPasswordVisible != null) {
            fieldPassword.textProperty().addListener((obs, o, n) -> {
                if (!passwordShown) fieldPasswordVisible.setText(n);
            });
            fieldPasswordVisible.textProperty().addListener((obs, o, n) -> {
                if (passwordShown) fieldPassword.setText(n);
            });
        }
        if (fieldConfirm != null && fieldConfirmVisible != null) {
            fieldConfirm.textProperty().addListener((obs, o, n) -> {
                if (!confirmShown) fieldConfirmVisible.setText(n);
            });
            fieldConfirmVisible.textProperty().addListener((obs, o, n) -> {
                if (confirmShown) fieldConfirm.setText(n);
            });
        }
    }

    // ─── Inline validation on focus-out ──────────────────────────────────────

    private void setupValidation() {
        if (fieldNom   != null) fieldNom.focusedProperty().addListener((obs, o, f)   -> { if (!f) validateNom();     });
        if (fieldEmail != null) fieldEmail.focusedProperty().addListener((obs, o, f) -> { if (!f) validateEmail();   });

        ChangeListener<Boolean> pwdFocus = (obs, o, f) -> { if (!f) validatePwd();     };
        if (fieldPassword        != null) fieldPassword.focusedProperty().addListener(pwdFocus);
        if (fieldPasswordVisible != null) fieldPasswordVisible.focusedProperty().addListener(pwdFocus);

        ChangeListener<Boolean> cfmFocus = (obs, o, f) -> { if (!f) validateConfirm(); };
        if (fieldConfirm        != null) fieldConfirm.focusedProperty().addListener(cfmFocus);
        if (fieldConfirmVisible != null) fieldConfirmVisible.focusedProperty().addListener(cfmFocus);
    }

    // ─── Eye toggle handlers ─────────────────────────────────────────────────

    @FXML
    private void handleTogglePassword() {
        passwordShown = !passwordShown;
        togglePair(fieldPassword, fieldPasswordVisible, btnTogglePassword, passwordShown);
    }

    @FXML
    private void handleToggleConfirm() {
        confirmShown = !confirmShown;
        togglePair(fieldConfirm, fieldConfirmVisible, btnToggleConfirm, confirmShown);
    }

    /** Show plain TextField or PasswordField depending on `show` flag. */
    private void togglePair(PasswordField hidden, TextField visible, Button eye, boolean show) {
        if (show) {
            visible.setText(hidden.getText());
            visible.setVisible(true);
            visible.setManaged(true);
            hidden.setVisible(false);
            hidden.setManaged(false);
            eye.setText("🙈");
            visible.requestFocus();
            visible.positionCaret(visible.getText().length());
        } else {
            hidden.setText(visible.getText());
            hidden.setVisible(true);
            hidden.setManaged(true);
            visible.setVisible(false);
            visible.setManaged(false);
            eye.setText("👁");
            hidden.requestFocus();
            hidden.positionCaret(hidden.getText().length());
        }
    }

    // ─── Read helpers — always returns the active field's value ──────────────

    private String getPassword() {
        return passwordShown
                ? (fieldPasswordVisible != null ? fieldPasswordVisible.getText() : "")
                : (fieldPassword        != null ? fieldPassword.getText()        : "");
    }

    private String getConfirm() {
        return confirmShown
                ? (fieldConfirmVisible != null ? fieldConfirmVisible.getText() : "")
                : (fieldConfirm        != null ? fieldConfirm.getText()        : "");
    }

    // ─── Register ────────────────────────────────────────────────────────────

    @FXML
    private void handleRegister() {
        if (!validateNom() || !validateEmail() || !validatePwd() || !validateConfirm()) return;

        btnRegister.setDisable(true);
        String nom      = fieldNom.getText().trim();
        String email    = fieldEmail.getText().trim().toLowerCase();
        String password = getPassword();

        new Thread(() -> {
            try {
                RoleDAO roleDAO = new RoleDAO();
                Role agriculteurRole = roleDAO.findByName("AGRICULTEUR");
                int roleId = agriculteurRole != null ? agriculteurRole.getId() : 2;

                userService.createUser(nom, email, password, roleId, "ACTIF");

                javafx.application.Platform.runLater(() -> {
                    AlertUtil.showSuccess("Inscription réussie",
                            "Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
                    goToLogin();
                    logger.info("Nouvelle inscription: {}", email);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    btnRegister.setDisable(false);
                    if (statusLabel != null) statusLabel.setText("❌ " + e.getMessage());
                    logger.warn("Échec inscription: {}", e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleGoToLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FarmIQ - Connexion");
        } catch (Exception e) {
            logger.error("Erreur navigation login", e);
        }
    }

    // ─── Validators ──────────────────────────────────────────────────────────

    private boolean validateNom() {
        String err = ValidationUtil.validateNom(fieldNom != null ? fieldNom.getText() : "");
        if (err != null) { showErr(errNom, err); return false; }
        clearErr(errNom); return true;
    }

    private boolean validateEmail() {
        String err = ValidationUtil.validateEmail(fieldEmail != null ? fieldEmail.getText() : "");
        if (err != null) { showErr(errEmail, err); return false; }
        clearErr(errEmail); return true;
    }

    private boolean validatePwd() {
        String err = ValidationUtil.validatePassword(getPassword());
        if (err != null) { showErr(errPassword, err); return false; }
        clearErr(errPassword); return true;
    }

    private boolean validateConfirm() {
        if (!getPassword().equals(getConfirm())) {
            showErr(errConfirm, "Les mots de passe ne correspondent pas");
            return false;
        }
        clearErr(errConfirm); return true;
    }

    private void showErr(Label lbl, String msg) {
        if (lbl != null) { lbl.setText("⚠  " + msg); lbl.setVisible(true); lbl.setManaged(true); }
    }

    private void clearErr(Label lbl) {
        if (lbl != null) { lbl.setText(""); lbl.setVisible(false); lbl.setManaged(false); }
    }
}
