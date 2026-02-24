package com.farmiq.controllers;

import com.farmiq.models.SellerProfile;
import com.farmiq.models.User;
import com.farmiq.services.SellerService;
import com.farmiq.services.UserService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Controller pour les paramètres et le profil utilisateur
 */
public class SettingsController implements Initializable {
    
    private static final Logger logger = LogManager.getLogger(SettingsController.class);
    
    // Services
    private final UserService userService = new UserService();
    private final SellerService sellerService = new SellerService();
    
    // Tab: Profil
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ImageView imgAvatar;
    @FXML private Button btnUploadPhoto;
    @FXML private Button btnSaveProfile;
    @FXML private Label lblLastLogin;
    
    // Tab: Boutique (for sellers)
    @FXML private TextField txtNomBoutique;
    @FXML private TextArea txtDescriptionBoutique;
    @FXML private ComboBox<String> cmbWilaya;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtTelephone;
    @FXML private ImageView imgLogo;
    @FXML private Button btnUploadLogo;
    @FXML private Button btnSaveBoutique;
    @FXML private Label lblNoteMoyenne;
    @FXML private Label lblTotalVentes;
    
    // Tab: Préférences
    @FXML private ToggleGroup themeGroup;
    @FXML private RadioButton rbLightTheme;
    @FXML private RadioButton rbDarkTheme;
    @FXML private ComboBox<String> cmbLanguage;
    @FXML private TextField txtWeatherCity;
    @FXML private Button btnSavePreferences;
    
    // Tab: Sécurité
    @FXML private Label lblSessionId;
    @FXML private Label lblSessionStart;
    @FXML private Label lblIpAddress;
    @FXML private Button btnLogout;
    @FXML private Button btnLogoutAll;
    
    private User currentUser;
    private SellerProfile sellerProfile;
    private String avatarPath;
    private String logoPath;
    
    // Wilayas
    private static final String[] WILAYAS = {
        "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa", 
        "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", 
        "Mahdia", "Manouba", "Médénine", "Monastir", "Nabeul", 
        "Sfax", "Sidi Bouzid", "Siliana", "Sousse", "Tunis", 
        "Zaghouan"
    };
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du SettingsController");
        
        // Verify session
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté");
            return;
        }
        
        // Initialize UI
        initializeWilayas();
        initializeTheme();
        initializeLanguage();
        
        // Load user data
        loadUserProfile();
        loadSellerProfile();
        loadSecurityInfo();
    }
    
    private void initializeWilayas() {
        cmbWilaya.getItems().addAll(WILAYAS);
    }
    
    private void initializeTheme() {
        // Check saved theme preference (would use Preferences in production)
        rbLightTheme.setSelected(true);
    }
    
    private void initializeLanguage() {
        cmbLanguage.getItems().addAll("Français", "العربية", "English");
        cmbLanguage.getSelectionModel().selectFirst();
    }
    
    private void loadUserProfile() {
        txtNom.setText(currentUser.getNom());
        txtEmail.setText(currentUser.getEmail());
        
        // Last login info
        if (currentUser.getDerniereConnexion() != null) {
            lblLastLogin.setText("Dernière connexion: " + currentUser.getDerniereConnexion().toString());
        } else {
            lblLastLogin.setText("Première connexion");
        }
        
        // Load avatar
        if (currentUser.getPhotoUrl() != null) {
            File avatarFile = new File(currentUser.getPhotoUrl());
            if (avatarFile.exists()) {
                imgAvatar.setImage(new Image(avatarFile.toURI().toString()));
            }
        }
    }
    
    private void loadSellerProfile() {
        try {
            sellerProfile = sellerService.getSellerProfileByUserId(currentUser.getId());
            
            if (sellerProfile != null) {
                // Enable boutique tab fields
                txtNomBoutique.setText(sellerProfile.getNomBoutique());
                txtDescriptionBoutique.setText(sellerProfile.getDescription());
                cmbWilaya.setValue(sellerProfile.getWilaya());
                txtAdresse.setText(sellerProfile.getAdresse());
                txtTelephone.setText(sellerProfile.getTelephone());
                
                // Stats
                double note = sellerProfile.getNoteMoyenne();
                if (note > 0) {
                    lblNoteMoyenne.setText(String.format("%.1f ⭐", note));
                } else {
                    lblNoteMoyenne.setText("Pas encore noté");
                }
                lblTotalVentes.setText(String.valueOf(sellerProfile.getTotalVentes()));
                
                // Logo
                if (sellerProfile.getLogoUrl() != null) {
                    File logoFile = new File(sellerProfile.getLogoUrl());
                    if (logoFile.exists()) {
                        imgLogo.setImage(new Image(logoFile.toURI().toString()));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du profil vendeur", e);
        }
    }
    
    private void loadSecurityInfo() {
        // Session info
        lblSessionId.setText("ID: " + SessionManager.getInstance().getSessionId());
        lblSessionStart.setText("Début: " + java.time.LocalDateTime.now());
        lblIpAddress.setText("IP: 127.0.0.1 (local)");
    }
    
    @FXML
    private void onUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            avatarPath = file.getAbsolutePath();
            imgAvatar.setImage(new Image(file.toURI().toString()));
        }
    }
    
    @FXML
    private void onUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un logo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            logoPath = file.getAbsolutePath();
            imgLogo.setImage(new Image(file.toURI().toString()));
        }
    }
    
    @FXML
    private void onSaveProfile() {
        try {
            // Validate
            String nom = txtNom.getText().trim();
            String email = txtEmail.getText().trim();
            
            if (nom.isEmpty() || email.isEmpty()) {
                AlertUtil.showWarning("Champs obligatoires", "Veuillez remplir tous les champs obligatoires");
                return;
            }
            
            // Check password change
            String currentPassword = txtCurrentPassword.getText();
            String newPassword = txtNewPassword.getText();
            String confirmPassword = txtConfirmPassword.getText();
            
            if (!currentPassword.isEmpty()) {
                // Verify current password
                if (!userService.verifyPassword(currentUser.getEmail(), currentPassword)) {
                    AlertUtil.showError("Mot de passe incorrect", "Le mot de passe actuel est incorrect");
                    return;
                }
                
                if (newPassword.isEmpty()) {
                    AlertUtil.showWarning("Nouveau mot de passe", "Veuillez entrer un nouveau mot de passe");
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    AlertUtil.showError("Erreur", "Les mots de passe ne correspondent pas");
                    return;
                }
                
                if (newPassword.length() < 6) {
                    AlertUtil.showWarning("Mot de passe trop court", "Le mot de passe doit contenir au moins 6 caractères");
                    return;
                }
                
                // Update password
                userService.updatePassword(currentUser.getId(), newPassword);
            }
            
            // Update profile
            currentUser.setNom(nom);
            currentUser.setEmail(email);
            currentUser.setPhotoUrl(avatarPath);
            
            userService.updateUser(currentUser);
            
            AlertUtil.showInfo("Succès", "Profil mis à jour avec succès!");
            
            // Clear password fields
            txtCurrentPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du profil", e);
            AlertUtil.showError("Erreur", "Impossible de mettre à jour le profil: " + e.getMessage());
        }
    }
    
    @FXML
    private void onSaveBoutique() {
        try {
            if (sellerProfile == null) {
                // Create new seller profile
                sellerProfile = new SellerProfile();
                sellerProfile.setUserId(currentUser.getId());
            }
            
            // Validate
            String nomBoutique = txtNomBoutique.getText().trim();
            if (nomBoutique.isEmpty()) {
                AlertUtil.showWarning("Nom de boutique", "Veuillez entrer un nom de boutique");
                return;
            }
            
            // Update profile
            sellerProfile.setNomBoutique(nomBoutique);
            sellerProfile.setDescription(txtDescriptionBoutique.getText());
            sellerProfile.setWilaya(cmbWilaya.getValue());
            sellerProfile.setAdresse(txtAdresse.getText());
            sellerProfile.setTelephone(txtTelephone.getText());
            sellerProfile.setLogoUrl(logoPath);
            
            if (sellerProfile.getId() == 0) {
                sellerService.createSellerProfile(sellerProfile);
                AlertUtil.showInfo("Succès", "Boutique créée avec succès!");
            } else {
                sellerService.updateSellerProfile(sellerProfile);
                AlertUtil.showInfo("Succès", "Boutique mise à jour avec succès!");
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de la boutique", e);
            AlertUtil.showError("Erreur", "Impossible de mettre à jour la boutique: " + e.getMessage());
        }
    }
    
    @FXML
    private void onSavePreferences() {
        try {
            // Save preferences
            String theme = rbLightTheme.isSelected() ? "light" : "dark";
            String language = cmbLanguage.getValue();
            String weatherCity = txtWeatherCity.getText();
            
            // In production, save to java.util.prefs.Preferences
            AlertUtil.showInfo("Succès", "Préférences enregistrées!");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement des préférences", e);
            AlertUtil.showError("Erreur", "Impossible de enregistrer les préférences");
        }
    }
    
    @FXML
    private void onLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vous déconnecter?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            // Navigate to login
            // NavigationManager.getInstance().navigateToLogin();
        }
    }
    
    @FXML
    private void onLogoutAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion globale");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vous déconnecter de tous les appareils?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            AlertUtil.showInfo("Déconnexion", "Fonctionnalité à implémenter: invalidate all sessions");
        }
    }
}
