package com.farmiq.controllers;

import com.farmiq.exceptions.UserException;
import com.farmiq.models.Plante;
import com.farmiq.models.User;
import com.farmiq.services.ExternalApiService;
import com.farmiq.services.PlanteService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static com.farmiq.utils.AlertUtil.*;

/**
 * Contrôleur pour la gestion des plantes
 * @author FarmIQ Team
 * @version 1.0
 */
public class PlantesController {
    private static final Logger logger = LogManager.getLogger(PlantesController.class);

    private final PlanteService planteService;
    private User currentUser;
    private ObservableList<Plante> plantesData;
    private FilteredList<Plante> filteredData;

    // ===== Form Fields =====
    @FXML private TextField txtNom;
    @FXML private TextField txtType;
    @FXML private TextField txtQuantite;
    @FXML private DatePicker dpPlantation;
    @FXML private DatePicker dpRecolte;
    @FXML private ComboBox<String> cbEtat;

    // ===== Table =====
    @FXML private TableView<Plante> tablePlantes;
    @FXML private TableColumn<Plante, Integer> colId;
    @FXML private TableColumn<Plante, String> colNom;
    @FXML private TableColumn<Plante, String> colType;
    @FXML private TableColumn<Plante, Integer> colQuantite;
    @FXML private TableColumn<Plante, LocalDate> colPlantation;
    @FXML private TableColumn<Plante, LocalDate> colRecolte;
    @FXML private TableColumn<Plante, String> colEtat;
    @FXML private TableColumn<Plante, Long> colJours;

    // ===== Search & Stats =====
    @FXML private TextField txtSearch;
    @FXML private Label lblCount;
    @FXML private Label lblSemees;
    @FXML private Label lblCroissance;
    @FXML private Label lblPretes;
    @FXML private Label lblRecoltees;

    // ===== Buttons =====
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnVider;
    @FXML private Button btnRefresh;
    @FXML private Button btnParcelles;
    @FXML private Button btnEnhancePrompt;

    public PlantesController() {
        this.planteService = new PlanteService();
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialiser le ComboBox des états
        cbEtat.setItems(FXCollections.observableArrayList(
                "semée", "en croissance", "prête à récolter", "récoltée"
        ));
        cbEtat.getSelectionModel().selectFirst();

        // Initialiser les dates par défaut
        dpPlantation.setValue(LocalDate.now());
        dpRecolte.setValue(LocalDate.now().plusDays(30));

        // Configuration des colonnes de la table
        colId.setCellValueFactory(new PropertyValueFactory<>("idPlante"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPlantation.setCellValueFactory(new PropertyValueFactory<>("datePlantation"));
        colRecolte.setCellValueFactory(new PropertyValueFactory<>("dateRecoltePrevue"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));

        // Colonne personnalisée pour les jours avant récolte
        colJours.setCellValueFactory(cellData -> {
            Plante plante = cellData.getValue();
            long jours = ChronoUnit.DAYS.between(LocalDate.now(), plante.getDateRecoltePrevue());
            return new javafx.beans.property.SimpleLongProperty(jours).asObject();
        });

        // Style personnalisé pour l'état
        colEtat.setCellFactory(column -> new TableCell<Plante, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Plante plante = getTableView().getItems().get(getIndex());
                    setText(plante.getEtatEmoji() + " " + etat);

                    // Couleur selon l'état
                    switch (etat.toLowerCase()) {
                        case "semée":
                            setStyle("-fx-text-fill: #8bc34a; -fx-font-weight: bold;");
                            break;
                        case "en croissance":
                            setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                            break;
                        case "prête à récolter":
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                            break;
                        case "récoltée":
                            setStyle("-fx-text-fill: #2196f3; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Style pour la colonne jours
        colJours.setCellFactory(column -> new TableCell<Plante, Long>() {
            @Override
            protected void updateItem(Long jours, boolean empty) {
                super.updateItem(jours, empty);
                if (empty || jours == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (jours < 0) {
                        setText("Dépassé");
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    } else if (jours == 0) {
                        setText("Aujourd'hui");
                        setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                    } else {
                        setText(jours + " j");
                        setStyle("-fx-text-fill: #4caf50;");
                    }
                }
            }
        });

        // Listener pour la sélection d'une ligne
        tablePlantes.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        remplirFormulaire(newSelection);
                    }
                }
        );

        // Validation quantité (seulement chiffres)
        txtQuantite.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtQuantite.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Recherche en temps réel
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> {
            if (filteredData != null) {
                filteredData.setPredicate(plante -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return plante.getNom().toLowerCase().contains(lowerCaseFilter) ||
                            plante.getType().toLowerCase().contains(lowerCaseFilter);
                });
                updateStats();
            }
        });

        // Charger les données
        chargerPlantes();
    }

    // ===============================================================
    // DATA MANAGEMENT
    // ===============================================================

    /**
     * Charge la liste des plantes
     */
    private void chargerPlantes() {
        try {
            plantesData = FXCollections.observableArrayList(
                    currentUser.isAdmin() ?
                            planteService.getAllPlantes() :
                            planteService.getPlantesByUser(currentUser.getId())
            );

            filteredData = new FilteredList<>(plantesData, p -> true);
            tablePlantes.setItems(filteredData);

            updateStats();
            logger.debug("Plantes chargées: {}", plantesData.size());
        } catch (UserException e) {
            logger.error("Erreur chargement plantes", e);
            showError("Erreur", "Impossible de charger les plantes: " + e.getMessage());
        }
    }

    /**
     * Remplit le formulaire avec les données d'une plante
     */
    private void remplirFormulaire(Plante plante) {
        txtNom.setText(plante.getNom());
        txtType.setText(plante.getType());
        txtQuantite.setText(String.valueOf(plante.getQuantite()));
        dpPlantation.setValue(plante.getDatePlantation());
        dpRecolte.setValue(plante.getDateRecoltePrevue());
        cbEtat.setValue(plante.getEtat());
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStats() {
        if (filteredData == null) return;

        int total = filteredData.size();
        lblCount.setText(total + " plante(s)");

        long semees = filteredData.stream()
                .filter(p -> "semée".equalsIgnoreCase(p.getEtat()))
                .count();
        long croissance = filteredData.stream()
                .filter(p -> "en croissance".equalsIgnoreCase(p.getEtat()))
                .count();
        long pretes = filteredData.stream()
                .filter(p -> "prête à récolter".equalsIgnoreCase(p.getEtat()))
                .count();
        long recoltees = filteredData.stream()
                .filter(p -> "récoltée".equalsIgnoreCase(p.getEtat()))
                .count();

        lblSemees.setText(String.valueOf(semees));
        lblCroissance.setText(String.valueOf(croissance));
        lblPretes.setText(String.valueOf(pretes));
        lblRecoltees.setText(String.valueOf(recoltees));
    }

    // ===============================================================
    // ACTION HANDLERS
    // ===============================================================

    /**
     * Ajoute une nouvelle plante
     */
    @FXML
    private void handleAjouter() {
        String nom = txtNom.getText();
        String type = txtType.getText();
        String quantiteStr = txtQuantite.getText();
        LocalDate plantation = dpPlantation.getValue();
        LocalDate recolte = dpRecolte.getValue();
        String etat = cbEtat.getValue();

        // Validation basique
        if (nom == null || nom.trim().isEmpty()) {
            showWarning("Validation", "Le nom est obligatoire");
            txtNom.requestFocus();
            return;
        }

        if (type == null || type.trim().isEmpty()) {
            showWarning("Validation", "Le type est obligatoire");
            txtType.requestFocus();
            return;
        }

        if (quantiteStr == null || quantiteStr.trim().isEmpty()) {
            showWarning("Validation", "La quantité est obligatoire");
            txtQuantite.requestFocus();
            return;
        }

        int quantite;
        try {
            quantite = Integer.parseInt(quantiteStr);
        } catch (NumberFormatException e) {
            showWarning("Validation", "La quantité doit être un nombre");
            txtQuantite.requestFocus();
            return;
        }

        if (plantation == null || recolte == null) {
            showWarning("Validation", "Les dates sont obligatoires");
            return;
        }

        if (etat == null) {
            showWarning("Validation", "L'état est obligatoire");
            return;
        }

        // Ajouter la plante
        try {
            Integer userId = currentUser.isAdmin() ? null : currentUser.getId();
            planteService.createPlante(nom, type, plantation, recolte, quantite, etat, userId);

            showSuccess("Succès", "Plante ajoutée avec succès !");
            viderFormulaire();
            chargerPlantes();
        } catch (UserException e) {
            logger.error("Erreur ajout plante", e);
            showError("Erreur", e.getMessage());
        }
    }

    /**
     * Modifie la plante sélectionnée
     */
    @FXML
    private void handleModifier() {
        Plante selected = tablePlantes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection", "Veuillez sélectionner une plante à modifier");
            return;
        }

        String nom = txtNom.getText();
        String type = txtType.getText();
        String quantiteStr = txtQuantite.getText();
        LocalDate plantation = dpPlantation.getValue();
        LocalDate recolte = dpRecolte.getValue();
        String etat = cbEtat.getValue();

        if (nom == null || nom.trim().isEmpty() || type == null || type.trim().isEmpty() ||
                quantiteStr == null || quantiteStr.trim().isEmpty() ||
                plantation == null || recolte == null || etat == null) {
            showWarning("Validation", "Tous les champs sont obligatoires");
            return;
        }

        int quantite;
        try {
            quantite = Integer.parseInt(quantiteStr);
        } catch (NumberFormatException e) {
            showWarning("Validation", "La quantité doit être un nombre");
            return;
        }

        try {
            planteService.updatePlante(
                    selected.getIdPlante(), nom, type, plantation, recolte, quantite, etat
            );

            showSuccess("Succès", "Plante modifiée avec succès !");
            viderFormulaire();
            chargerPlantes();
        } catch (UserException e) {
            logger.error("Erreur modification plante", e);
            showError("Erreur", e.getMessage());
        }
    }

    /**
     * Supprime la plante sélectionnée
     */
    @FXML
    private void handleSupprimer() {
        Plante selected = tablePlantes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection", "Veuillez sélectionner une plante à supprimer");
            return;
        }

        if (!showConfirmation("Confirmation",
                "Voulez-vous vraiment supprimer la plante \"" + selected.getNom() + "\" ?")) {
            return;
        }

        try {
            planteService.deletePlante(selected.getIdPlante());
            showSuccess("Succès", "Plante supprimée avec succès !");
            viderFormulaire();
            chargerPlantes();
        } catch (UserException e) {
            logger.error("Erreur suppression plante", e);
            showError("Erreur", e.getMessage());
        }
    }

    /**
     * Vide le formulaire
     */
    @FXML
    private void handleVider() {
        viderFormulaire();
    }

    /**
     * Actualise la liste
     */
    @FXML
    private void handleRefresh() {
        chargerPlantes();
        showSuccess("Actualisation", "Liste actualisée !");
    }

    /**
     * Naviguer vers les Parcelles
     * Fixed version: Properly navigates through the BorderPane scene graph
     */
    @FXML
    private void handleParcelles() {
        try {
            // Load the Parcelles FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/fxml/Parcelles_farmiq.fxml")
            );
            Parent parcellesView = loader.load();

            // Navigate through the scene graph properly
            Scene scene = btnParcelles.getScene();
            if (scene != null && scene.getRoot() instanceof BorderPane) {
                BorderPane borderPane = (BorderPane) scene.getRoot();

                // Get the center VBox which contains header + contentArea
                Node centerNode = borderPane.getCenter();
                if (centerNode instanceof VBox) {
                    VBox centerVBox = (VBox) centerNode;

                    // The second child should be the StackPane contentArea
                    if (centerVBox.getChildren().size() >= 2) {
                        Node contentNode = centerVBox.getChildren().get(1);
                        if (contentNode instanceof StackPane) {
                            StackPane contentArea = (StackPane) contentNode;
                            contentArea.getChildren().setAll(parcellesView);

                            logger.info("Navigation vers Parcelles réussie");
                            return;
                        }
                    }
                }
            }

            // Fallback if scene structure is unexpected
            showWarning("Navigation",
                    "Veuillez utiliser le menu 'Mes Parcelles' dans la barre latérale pour une meilleure expérience.");

        } catch (Exception e) {
            logger.error("Erreur navigation vers Parcelles", e);
            showError("Erreur", "Impossible de charger la page Parcelles: " + e.getMessage());
        }
    }

    /**
     * Vide tous les champs du formulaire
     */
    private void viderFormulaire() {
        txtNom.clear();
        txtType.clear();
        txtQuantite.clear();
        dpPlantation.setValue(LocalDate.now());
        dpRecolte.setValue(LocalDate.now().plusDays(30));
        cbEtat.getSelectionModel().selectFirst();
        tablePlantes.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleEnhancePrompt() {
        // Get the prompt from the search bar (which is likely where users type their queries)
        String prompt = txtSearch.getText();

        // Fallback to form field if search is empty
        if (prompt == null || prompt.trim().isEmpty()) {
            prompt = txtNom.getText();
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            showWarning("Champ vide", "Veuillez entrer un terme à améliorer (dans la barre de recherche ou le formulaire).");
            return;
        }

        try {
            logger.info("Enhancing prompt: {}", prompt);
            ExternalApiService apiService = new ExternalApiService();
            String enhancedPrompt = apiService.enhancePrompt(prompt);

            AlertUtil.showInfo("Prompt Amélioré", "Voici une suggestion améliorée:\n\n" + enhancedPrompt);

            // Fill the search bar with the result to make it easy to use
            txtSearch.setText(enhancedPrompt);

        } catch (Exception e) {
            logger.error("Erreur lors de l'amélioration du prompt", e);
            showError("Erreur", "Impossible d'améliorer le prompt.");
        }
    }
}