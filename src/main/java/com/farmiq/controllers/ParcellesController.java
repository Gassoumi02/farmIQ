package com.farmiq.controllers;

import com.farmiq.exceptions.UserException;
import com.farmiq.models.Parcelle;
import com.farmiq.models.Plante;
import com.farmiq.models.User;
import com.farmiq.services.ParcelleService;
import com.farmiq.services.PlanteService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur pour la gestion des parcelles
 */
public class ParcellesController {
    private static final Logger logger = LogManager.getLogger(ParcellesController.class);

    private final ParcelleService parcelleService;
    private final PlanteService planteService;
    private User currentUser;
    private ObservableList<Parcelle> parcellesData;

    // ===== FORM FIELDS =====
    @FXML private TextField txtNomParcelle;
    @FXML private TextField txtSurface;
    @FXML private TextField txtLocalisation;
    @FXML private TextField txtTypeSol;
    @FXML private ComboBox<String> cbEtatParcelle;
    @FXML private DatePicker dpDerniereCulture;
    @FXML private CheckBox chkIrrigation;
    @FXML private ComboBox<Plante> cbPlante;
    @FXML private TextArea txtRemarques;

    // ===== TABLE =====
    @FXML private TableView<Parcelle> tableParcelles;
    @FXML private TableColumn<Parcelle, Integer> colId;
    @FXML private TableColumn<Parcelle, String> colNom;
    @FXML private TableColumn<Parcelle, Double> colSurface;
    @FXML private TableColumn<Parcelle, String> colLocalisation;
    @FXML private TableColumn<Parcelle, String> colTypeSol;
    @FXML private TableColumn<Parcelle, String> colEtat;
    @FXML private TableColumn<Parcelle, LocalDate> colDateCulture;
    @FXML private TableColumn<Parcelle, Boolean> colIrrigation;
    @FXML private TableColumn<Parcelle, Integer> colPlante;

    // ===== STATS =====
    @FXML private Label lblCount;
    @FXML private Label lblSurfaceTotale;
    @FXML private Label lblDisponibles;
    @FXML private Label lblEnCulture;
    @FXML private Label lblAvecIrrigation;

    // ===== BUTTONS =====
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnVider;
    @FXML private Button btnRefresh;
    @FXML private Button btnRetourPlantes;

    public ParcellesController() {
        this.parcelleService = new ParcelleService();
        this.planteService = new PlanteService();
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialiser ComboBox États
        cbEtatParcelle.setItems(FXCollections.observableArrayList(
                "disponible", "en culture", "en repos", "en préparation"
        ));
        cbEtatParcelle.getSelectionModel().selectFirst();

        // Date par défaut
        dpDerniereCulture.setValue(LocalDate.now());

        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("idParcelle"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomParcelle"));
        colSurface.setCellValueFactory(new PropertyValueFactory<>("surface"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colTypeSol.setCellValueFactory(new PropertyValueFactory<>("typeSol"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatParcelle"));
        colDateCulture.setCellValueFactory(new PropertyValueFactory<>("dateDerniereCulture"));
        colIrrigation.setCellValueFactory(new PropertyValueFactory<>("irrigation"));
        colPlante.setCellValueFactory(new PropertyValueFactory<>("idPlante"));

        // Style pour état
        colEtat.setCellFactory(column -> new TableCell<Parcelle, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Parcelle parcelle = getTableView().getItems().get(getIndex());
                    setText(parcelle.getEtatEmoji() + " " + etat);

                    switch (etat.toLowerCase()) {
                        case "disponible":
                            setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                            break;
                        case "en culture":
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                            break;
                        case "en repos":
                            setStyle("-fx-text-fill: #9e9e9e; -fx-font-weight: bold;");
                            break;
                        case "en préparation":
                            setStyle("-fx-text-fill: #2196f3; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // Style irrigation
        colIrrigation.setCellFactory(column -> new TableCell<Parcelle, Boolean>() {
            @Override
            protected void updateItem(Boolean irrigation, boolean empty) {
                super.updateItem(irrigation, empty);
                if (empty || irrigation == null) {
                    setText(null);
                } else {
                    setText(irrigation ? "💧 Oui" : "🚫 Non");
                    setStyle(irrigation ? "-fx-text-fill: #2196f3;" : "-fx-text-fill: #9e9e9e;");
                }
            }
        });

        // Listener sélection
        tableParcelles.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        remplirFormulaire(newSelection);
                    }
                }
        );

        // Validation surface (double)
        txtSurface.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtSurface.setText(oldValue);
            }
        });

        // Charger données
        chargerParcelles();
        chargerPlantes();
    }

    // ===============================================================
    // DATA MANAGEMENT
    // ===============================================================

    private void chargerParcelles() {
        try {
            parcellesData = FXCollections.observableArrayList(
                    currentUser.isAdmin() ?
                            parcelleService.getAllParcelles() :
                            parcelleService.getParcellesByUser(currentUser.getId())
            );

            tableParcelles.setItems(parcellesData);
            updateStats();
            logger.debug("Parcelles chargées: {}", parcellesData.size());
        } catch (UserException e) {
            logger.error("Erreur chargement parcelles", e);
            AlertUtil.showError("Erreur", "Impossible de charger les parcelles: " + e.getMessage());
        }
    }

    private void chargerPlantes() {
        try {
            List<Plante> plantes = currentUser.isAdmin() ?
                    planteService.getAllPlantes() :
                    planteService.getPlantesByUser(currentUser.getId());

            cbPlante.setItems(FXCollections.observableArrayList(plantes));
            cbPlante.setConverter(new javafx.util.StringConverter<Plante>() {
                @Override
                public String toString(Plante plante) {
                    return plante != null ? plante.getNom() : "";
                }

                @Override
                public Plante fromString(String string) {
                    return null;
                }
            });
        } catch (UserException e) {
            logger.error("Erreur chargement plantes", e);
        }
    }

    private void remplirFormulaire(Parcelle parcelle) {
        txtNomParcelle.setText(parcelle.getNomParcelle());
        txtSurface.setText(String.valueOf(parcelle.getSurface()));
        txtLocalisation.setText(parcelle.getLocalisation());
        txtTypeSol.setText(parcelle.getTypeSol());
        cbEtatParcelle.setValue(parcelle.getEtatParcelle());
        dpDerniereCulture.setValue(parcelle.getDateDerniereCulture());
        chkIrrigation.setSelected(parcelle.isIrrigation());
        txtRemarques.setText(parcelle.getRemarques());

        // Sélectionner la plante
        if (parcelle.getIdPlante() != null) {
            cbPlante.getItems().stream()
                    .filter(p -> p.getIdPlante() == parcelle.getIdPlante())
                    .findFirst()
                    .ifPresent(p -> cbPlante.getSelectionModel().select(p));
        } else {
            cbPlante.getSelectionModel().clearSelection();
        }
    }

    private void updateStats() {
        if (parcellesData == null) return;

        int total = parcellesData.size();
        lblCount.setText(total + " parcelle(s)");

        double surfaceTotale = parcellesData.stream()
                .mapToDouble(Parcelle::getSurface)
                .sum();
        lblSurfaceTotale.setText(String.format("%.0f m²", surfaceTotale));

        long disponibles = parcellesData.stream()
                .filter(p -> "disponible".equalsIgnoreCase(p.getEtatParcelle()))
                .count();
        lblDisponibles.setText(String.valueOf(disponibles));

        long enCulture = parcellesData.stream()
                .filter(p -> "en culture".equalsIgnoreCase(p.getEtatParcelle()))
                .count();
        lblEnCulture.setText(String.valueOf(enCulture));

        long avecIrrigation = parcellesData.stream()
                .filter(Parcelle::isIrrigation)
                .count();
        lblAvecIrrigation.setText(String.valueOf(avecIrrigation));
    }

    // ===============================================================
    // ACTION HANDLERS
    // ===============================================================

    @FXML
    private void handleAjouter() {
        String nom = txtNomParcelle.getText();
        String surfaceStr = txtSurface.getText();
        String localisation = txtLocalisation.getText();
        String typeSol = txtTypeSol.getText();
        String etat = cbEtatParcelle.getValue();
        LocalDate dateCulture = dpDerniereCulture.getValue();
        boolean irrigation = chkIrrigation.isSelected();
        String remarques = txtRemarques.getText();
        Plante planteSelectionnee = cbPlante.getValue();

        // Validation
        if (nom == null || nom.trim().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le nom est obligatoire");
            return;
        }

        if (surfaceStr == null || surfaceStr.trim().isEmpty()) {
            AlertUtil.showWarning("Validation", "La surface est obligatoire");
            return;
        }

        double surface;
        try {
            surface = Double.parseDouble(surfaceStr);
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation", "La surface doit être un nombre");
            return;
        }

        try {
            Integer userId = currentUser.isAdmin() ? null : currentUser.getId();
            Integer idPlante = planteSelectionnee != null ? planteSelectionnee.getIdPlante() : null;

            parcelleService.createParcelle(nom, surface, localisation, typeSol, etat,
                    dateCulture, irrigation, remarques, idPlante, userId);

            AlertUtil.showSuccess("Succès", "Parcelle ajoutée avec succès !");
            viderFormulaire();
            chargerParcelles();
        } catch (UserException e) {
            logger.error("Erreur ajout parcelle", e);
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Parcelle selected = tableParcelles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner une parcelle à modifier");
            return;
        }

        String nom = txtNomParcelle.getText();
        String surfaceStr = txtSurface.getText();

        if (nom == null || nom.trim().isEmpty() || surfaceStr == null || surfaceStr.trim().isEmpty()) {
            AlertUtil.showWarning("Validation", "Nom et surface sont obligatoires");
            return;
        }

        double surface;
        try {
            surface = Double.parseDouble(surfaceStr);
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation", "La surface doit être un nombre");
            return;
        }

        try {
            Plante planteSelectionnee = cbPlante.getValue();
            Integer idPlante = planteSelectionnee != null ? planteSelectionnee.getIdPlante() : null;

            parcelleService.updateParcelle(
                    selected.getIdParcelle(), nom, surface, txtLocalisation.getText(),
                    txtTypeSol.getText(), cbEtatParcelle.getValue(), dpDerniereCulture.getValue(),
                    chkIrrigation.isSelected(), txtRemarques.getText(), idPlante
            );

            AlertUtil.showSuccess("Succès", "Parcelle modifiée avec succès !");
            viderFormulaire();
            chargerParcelles();
        } catch (UserException e) {
            logger.error("Erreur modification parcelle", e);
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Parcelle selected = tableParcelles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner une parcelle à supprimer");
            return;
        }

        if (!AlertUtil.showConfirmation("Confirmation",
                "Voulez-vous vraiment supprimer la parcelle \"" + selected.getNomParcelle() + "\" ?")) {
            return;
        }

        try {
            parcelleService.deleteParcelle(selected.getIdParcelle());
            AlertUtil.showSuccess("Succès", "Parcelle supprimée avec succès !");
            viderFormulaire();
            chargerParcelles();
        } catch (UserException e) {
            logger.error("Erreur suppression parcelle", e);
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleVider() {
        viderFormulaire();
    }

    @FXML
    private void handleRefresh() {
        chargerParcelles();
        chargerPlantes();
        AlertUtil.showSuccess("Actualisation", "Liste actualisée !");
    }

    @FXML
    private void handleRetourPlantes() {
        try {
            // Charger l'interface Plantes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Plantes.fxml"));
            VBox plantesView = loader.load();

            // Remplacer le contenu actuel
            VBox parent = (VBox) btnRetourPlantes.getScene().getRoot();
            parent.getChildren().setAll(plantesView.getChildren());

        } catch (IOException e) {
            logger.error("Erreur navigation vers Plantes", e);
            AlertUtil.showError("Erreur", "Impossible de charger la page Plantes");
        }
    }

    private void viderFormulaire() {
        txtNomParcelle.clear();
        txtSurface.clear();
        txtLocalisation.clear();
        txtTypeSol.clear();
        cbEtatParcelle.getSelectionModel().selectFirst();
        dpDerniereCulture.setValue(LocalDate.now());
        chkIrrigation.setSelected(false);
        txtRemarques.clear();
        cbPlante.getSelectionModel().clearSelection();
        tableParcelles.getSelectionModel().clearSelection();
    }
}