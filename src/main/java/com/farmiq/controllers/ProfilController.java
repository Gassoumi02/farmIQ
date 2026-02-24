package com.farmiq.controllers;

import com.farmiq.exceptions.AuthException;
import com.farmiq.exceptions.UserException;
import com.farmiq.models.User;
import com.farmiq.services.AuthService;
import com.farmiq.services.UserService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.SessionManager;
import com.farmiq.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Contrôleur pour la section Profil Utilisateur
 * Permet de visualiser et modifier les informations du profil
 * et de changer le mot de passe
 */
public class ProfilController {
    private static final Logger logger = LogManager.getLogger(ProfilController.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);

    private final UserService userService;
    private final AuthService authService;
    private User currentUser;

    // ===== Profile Display Fields =====
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblUserId;
    @FXML private Label lblEmail;
    @FXML private Label lblStatut;
    @FXML private Label lblMemberSince;

    // ===== Edit Profile Form =====
    @FXML private GridPane editForm;
    @FXML private VBox viewInfo;
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private Button btnEdit;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ===== Password Change Form =====
    @FXML private GridPane passwordForm;
    @FXML private VBox passwordViewInfo;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnChangePassword;
    @FXML private Button btnSavePassword;
    @FXML private Button btnCancelPassword;
    @FXML private Label lblLastPasswordChange;

    public ProfilController() {
        this.userService = new UserService();
        this.authService = new AuthService();
    }

    @FXML
    public void initialize() {
        loadUserProfile();
    }

    /**
     * Charge et affiche les informations du profil utilisateur
     */
    private void loadUserProfile() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertUtil.showError("Erreur", "Aucun utilisateur connecté");
            return;
        }

        // Recharger l'utilisateur depuis la base pour avoir les données à jour
        try {
            currentUser = userService.getUserById(currentUser.getId());
            SessionManager.getInstance().refreshUser(currentUser);
            displayUserInfo();
        } catch (UserException e) {
            logger.error("Erreur chargement profil", e);
            AlertUtil.showError("Erreur", "Impossible de charger le profil: " + e.getMessage());
        }
    }

    /**
     * Affiche les informations de l'utilisateur dans l'interface
     */
    private void displayUserInfo() {
        if (currentUser == null) return;

        lblUserName.setText(currentUser.getNom());
        lblUserRole.setText(currentUser.getRoleName());
        lblUserId.setText("#" + currentUser.getId());
        lblEmail.setText(currentUser.getEmail());

        // Statut avec couleur
        String statut = currentUser.getStatut();
        if ("ACTIF".equals(statut)) {
            lblStatut.setText("🟢 ACTIF");
            lblStatut.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
        } else {
            lblStatut.setText("🔴 " + statut);
            lblStatut.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
        }

        // Date de création
        if (currentUser.getCreatedAt() != null) {
            lblMemberSince.setText(currentUser.getCreatedAt().format(DATE_FORMATTER));
        } else {
            lblMemberSince.setText("Date inconnue");
        }

        // Dernière modification du mot de passe
        if (currentUser.getUpdatedAt() != null) {
            lblLastPasswordChange.setText("Dernière modification : " +
                    currentUser.getUpdatedAt().format(DATE_FORMATTER));
        }
    }

    // ===============================================================
    // PROFILE EDIT HANDLERS
    // ===============================================================

    /**
     * Active le mode édition du profil
     */
    @FXML
    private void handleEdit() {
        // Remplir les champs avec les valeurs actuelles
        txtNom.setText(currentUser.getNom());
        txtEmail.setText(currentUser.getEmail());

        // Basculer en mode édition
        editForm.setVisible(true);
        editForm.setManaged(true);
        viewInfo.setVisible(false);
        viewInfo.setManaged(false);
        btnEdit.setDisable(true);

        logger.debug("Mode édition activé");
    }

    /**
     * Annule l'édition du profil
     */
    @FXML
    private void handleCancel() {
        // Retour au mode visualisation
        editForm.setVisible(false);
        editForm.setManaged(false);
        viewInfo.setVisible(true);
        viewInfo.setManaged(true);
        btnEdit.setDisable(false);

        // Nettoyer les champs
        txtNom.clear();
        txtEmail.clear();

        logger.debug("Édition annulée");
    }

    /**
     * Enregistre les modifications du profil
     */
    @FXML
    private void handleSave() {
        String nom = txtNom.getText().trim();
        String email = txtEmail.getText().trim();

        // Validation
        String nomError = ValidationUtil.validateNom(nom);
        if (nomError != null) {
            AlertUtil.showError("Erreur de validation", nomError);
            return;
        }

        String emailError = ValidationUtil.validateEmail(email);
        if (emailError != null) {
            AlertUtil.showError("Erreur de validation", emailError);
            return;
        }

        // Vérifier si des changements ont été effectués
        if (nom.equals(currentUser.getNom()) && email.equals(currentUser.getEmail())) {
            AlertUtil.showWarning("Information", "Aucune modification détectée.");
            handleCancel();
            return;
        }

        // Confirmation
        if (!AlertUtil.showConfirmation("Confirmer les modifications",
                "Voulez-vous enregistrer ces modifications ?")) {
            return;
        }

        // Enregistrer les modifications
        try {
            userService.updateUser(
                    currentUser.getId(),
                    nom,
                    email,
                    currentUser.getRole().getId(),
                    currentUser.getStatut()
            );

            // Recharger le profil
            loadUserProfile();

            // Retour au mode visualisation
            handleCancel();

            AlertUtil.showSuccess("Succès", "Profil mis à jour avec succès !");
            logger.info("Profil utilisateur mis à jour: {}", currentUser.getEmail());

        } catch (UserException e) {
            logger.error("Erreur mise à jour profil", e);
            AlertUtil.showError("Erreur", "Impossible de mettre à jour le profil: " + e.getMessage());
        }
    }

    // ===============================================================
    // PASSWORD CHANGE HANDLERS
    // ===============================================================

    /**
     * Affiche le formulaire de changement de mot de passe
     */
    @FXML
    private void handleShowPasswordChange() {
        passwordForm.setVisible(true);
        passwordForm.setManaged(true);
        passwordViewInfo.setVisible(false);
        passwordViewInfo.setManaged(false);
        btnChangePassword.setDisable(true);

        logger.debug("Formulaire changement mot de passe affiché");
    }

    /**
     * Annule le changement de mot de passe
     */
    @FXML
    private void handleCancelPassword() {
        passwordForm.setVisible(false);
        passwordForm.setManaged(false);
        passwordViewInfo.setVisible(true);
        passwordViewInfo.setManaged(true);
        btnChangePassword.setDisable(false);

        // Nettoyer les champs
        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();

        logger.debug("Changement mot de passe annulé");
    }

    /**
     * Enregistre le nouveau mot de passe
     */
    @FXML
    private void handleSavePassword() {
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // Validation des champs vides
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showError("Erreur", "Tous les champs sont obligatoires.");
            return;
        }

        // Vérifier que les nouveaux mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            AlertUtil.showError("Erreur", "Les nouveaux mots de passe ne correspondent pas.");
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            return;
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (currentPassword.equals(newPassword)) {
            AlertUtil.showError("Erreur", "Le nouveau mot de passe doit être différent de l'ancien.");
            return;
        }

        // Validation du nouveau mot de passe
        String passwordError = ValidationUtil.validatePassword(newPassword);
        if (passwordError != null) {
            AlertUtil.showError("Erreur de validation", passwordError);
            return;
        }

        // Vérifier le mot de passe actuel
        try {
            authService.login(currentUser.getEmail(), currentPassword);
        } catch (AuthException e) {
            AlertUtil.showError("Erreur", "Mot de passe actuel incorrect.");
            txtCurrentPassword.clear();
            txtCurrentPassword.requestFocus();
            return;
        }

        // Confirmation
        if (!AlertUtil.showConfirmation("Confirmer le changement",
                "Voulez-vous changer votre mot de passe ?")) {
            return;
        }

        // Changer le mot de passe
        try {
            userService.updatePassword(currentUser.getId(), newPassword);

            // Fermer le formulaire
            handleCancelPassword();

            AlertUtil.showSuccess("Succès", "Mot de passe changé avec succès !\n" +
                    "Utilisez ce nouveau mot de passe lors de votre prochaine connexion.");
            logger.info("Mot de passe changé pour utilisateur: {}", currentUser.getEmail());

            // Recharger pour mettre à jour la date de modification
            loadUserProfile();

        } catch (UserException e) {
            logger.error("Erreur changement mot de passe", e);
            AlertUtil.showError("Erreur", "Impossible de changer le mot de passe: " + e.getMessage());
        }
    }
}