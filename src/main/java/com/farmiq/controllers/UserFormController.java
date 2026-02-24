package com.farmiq.controllers;

import com.farmiq.models.Role;
import com.farmiq.models.User;
import com.farmiq.services.RoleService;
import com.farmiq.services.UserService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class UserFormController {
    private static final Logger logger = LogManager.getLogger(UserFormController.class);

    public enum Mode { ADD, EDIT }

    @FXML private Label lblTitle;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldConfirmPassword;
    @FXML private ComboBox<Role> comboRole;
    @FXML private ComboBox<String> comboStatut;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Label lblPasswordSection;

    // Validation error labels
    @FXML private Label errNom;
    @FXML private Label errEmail;
    @FXML private Label errPassword;
    @FXML private Label errConfirm;
    @FXML private Label errRole;

    // Password strength
    @FXML private Label lblPasswordStrength;
    @FXML private ProgressBar pwdStrengthBar;

    private Mode mode = Mode.ADD;
    private User editUser;
    private Runnable onSaveCallback;
    private UserService userService;
    private RoleService roleService;

    @FXML
    public void initialize() {
        userService = new UserService();
        roleService = new RoleService();

        comboStatut.setItems(FXCollections.observableArrayList("ACTIF", "INACTIF"));
        comboStatut.setValue("ACTIF");

        loadRoles();
        setupValidation();
        setupPasswordStrength();
    }

    private void loadRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            comboRole.setItems(FXCollections.observableArrayList(roles));
            if (!roles.isEmpty()) comboRole.setValue(roles.get(0));
        } catch (Exception e) {
            logger.error("Erreur chargement rôles", e);
        }
    }

    private void setupValidation() {
        fieldNom.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateNom();
        });
        fieldEmail.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateEmail();
        });
        fieldPassword.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validatePassword();
        });
        fieldConfirmPassword.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateConfirmPassword();
        });
    }

    private void setupPasswordStrength() {
        if (fieldPassword != null && pwdStrengthBar != null) {
            fieldPassword.textProperty().addListener((obs, old, newVal) -> {
                String strength = com.farmiq.utils.PasswordUtil.getPasswordStrength(newVal);
                updatePasswordStrengthUI(strength);
            });
        }
    }

    private void updatePasswordStrengthUI(String strength) {
        if (pwdStrengthBar == null || lblPasswordStrength == null) return;
        pwdStrengthBar.getStyleClass().removeAll("pwd-weak", "pwd-medium", "pwd-strong");
        switch (strength) {
            case "FAIBLE":
                pwdStrengthBar.setProgress(0.33);
                pwdStrengthBar.getStyleClass().add("pwd-weak");
                lblPasswordStrength.setText("Faible");
                lblPasswordStrength.setStyle("-fx-text-fill: #e74c3c;");
                break;
            case "MOYEN":
                pwdStrengthBar.setProgress(0.66);
                pwdStrengthBar.getStyleClass().add("pwd-medium");
                lblPasswordStrength.setText("Moyen");
                lblPasswordStrength.setStyle("-fx-text-fill: #f39c12;");
                break;
            case "FORT":
                pwdStrengthBar.setProgress(1.0);
                pwdStrengthBar.getStyleClass().add("pwd-strong");
                lblPasswordStrength.setText("Fort");
                lblPasswordStrength.setStyle("-fx-text-fill: #27ae60;");
                break;
            default:
                pwdStrengthBar.setProgress(0);
                lblPasswordStrength.setText("");
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.ADD) {
            if (lblTitle != null) lblTitle.setText("➕ Ajouter un utilisateur");
            if (lblPasswordSection != null) lblPasswordSection.setText("Mot de passe *");
            if (fieldPassword != null) fieldPassword.setVisible(true);
            if (fieldConfirmPassword != null) fieldConfirmPassword.setVisible(true);
        } else {
            if (lblTitle != null) lblTitle.setText("✏️ Modifier l'utilisateur");
            if (lblPasswordSection != null) lblPasswordSection.setText("Nouveau mot de passe (laisser vide pour garder)");
            if (fieldPassword != null) fieldPassword.setPromptText("Laisser vide pour garder l'actuel");
        }
    }

    public void setUser(User user) {
        this.editUser = user;
        if (user != null) {
            fieldNom.setText(user.getNom());
            fieldEmail.setText(user.getEmail());
            comboStatut.setValue(user.getStatut());
            if (user.getRole() != null && comboRole.getItems() != null) {
                comboRole.getItems().stream()
                    .filter(r -> r.getId() == user.getRole().getId())
                    .findFirst()
                    .ifPresent(r -> comboRole.setValue(r));
            }
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSave() {
        if (!validateAll()) return;

        String nom = fieldNom.getText().trim();
        String email = fieldEmail.getText().trim();
        String password = fieldPassword.getText();
        Role role = comboRole.getValue();
        String statut = comboStatut.getValue();

        try {
            if (mode == Mode.ADD) {
                userService.createUser(nom, email, password, role.getId(), statut);
                AlertUtil.showSuccess("Succès", "Utilisateur créé avec succès.");
                logger.info("Utilisateur créé: {}", email);
            } else {
                userService.updateUser(editUser.getId(), nom, email, role.getId(), statut);
                if (password != null && !password.isEmpty()) {
                    userService.updatePassword(editUser.getId(), password);
                }
                AlertUtil.showSuccess("Succès", "Utilisateur mis à jour avec succès.");
                logger.info("Utilisateur mis à jour: {}", email);
            }

            if (onSaveCallback != null) onSaveCallback.run();
            closeDialog();

        } catch (Exception e) {
            AlertUtil.showError("Erreur sauvegarde", e.getMessage());
            logger.error("Erreur sauvegarde utilisateur", e);
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateAll() {
        boolean valid = true;
        valid &= validateNom();
        valid &= validateEmail();
        if (mode == Mode.ADD || (fieldPassword.getText() != null && !fieldPassword.getText().isEmpty())) {
            valid &= validatePassword();
            valid &= validateConfirmPassword();
        }
        if (comboRole.getValue() == null) {
            showError(errRole, "Veuillez sélectionner un rôle");
            valid = false;
        } else {
            clearError(errRole);
        }
        return valid;
    }

    private boolean validateNom() {
        String err = ValidationUtil.validateNom(fieldNom.getText());
        if (err != null) { showError(errNom, err); return false; }
        clearError(errNom);
        return true;
    }

    private boolean validateEmail() {
        String err = ValidationUtil.validateEmail(fieldEmail.getText());
        if (err != null) { showError(errEmail, err); return false; }
        try {
            String email = fieldEmail.getText().trim().toLowerCase();
            boolean exists = (mode == Mode.ADD)
                ? userService.emailExists(email)
                : (editUser != null && !editUser.getEmail().equalsIgnoreCase(email) && userService.emailExists(email));
            if (exists) { showError(errEmail, "Cet email est déjà utilisé"); return false; }
        } catch (Exception ignored) {}
        clearError(errEmail);
        return true;
    }

    private boolean validatePassword() {
        String pwd = fieldPassword.getText();
        if (mode == Mode.EDIT && (pwd == null || pwd.isEmpty())) { clearError(errPassword); return true; }
        String err = ValidationUtil.validatePassword(pwd);
        if (err != null) { showError(errPassword, err); return false; }
        clearError(errPassword);
        return true;
    }

    private boolean validateConfirmPassword() {
        if (mode == Mode.EDIT && (fieldPassword.getText() == null || fieldPassword.getText().isEmpty())) {
            clearError(errConfirm); return true;
        }
        if (!fieldPassword.getText().equals(fieldConfirmPassword.getText())) {
            showError(errConfirm, "Les mots de passe ne correspondent pas");
            return false;
        }
        clearError(errConfirm);
        return true;
    }

    private void showError(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    private void clearError(Label label) {
        if (label != null) {
            label.setText("");
            label.setVisible(false);
            label.setManaged(false);
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
