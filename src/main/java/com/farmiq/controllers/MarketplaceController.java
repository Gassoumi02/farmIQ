package com.farmiq.controllers;

import com.farmiq.models.Listing;
import com.farmiq.models.ListingImage;
import com.farmiq.models.SellerProfile;
import com.farmiq.models.User;
import com.farmiq.models.enums.ListingStatus;
import com.farmiq.services.CartService;
import com.farmiq.services.ListingService;
import com.farmiq.services.SellerService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.CurrencyUtils;
import com.farmiq.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

/**
 * Controller for the marketplace – browse listings + full CRUD for seller's own listings.
 */
public class MarketplaceController implements Initializable {

    private static final Logger logger = LogManager.getLogger(MarketplaceController.class);

    // ── Services ──────────────────────────────────────────────────────────────
    private final ListingService listingService = new ListingService();
    private final SellerService  sellerService  = new SellerService();
    private final CartService    cartService    = new CartService();

    // ── BROWSE tab UI ─────────────────────────────────────────────────────────
    @FXML private TabPane tabPane;
    @FXML private Tab     tabParcourir;
    @FXML private Tab     tabMesAnnonces;

    @FXML private TextField    txtSearch;
    @FXML private ComboBox<String> cmbCategorie;
    @FXML private ComboBox<String> cmbWilaya;
    @FXML private ComboBox<String> cmbSortBy;
    @FXML private Slider       sliderPrixMin;
    @FXML private Slider       sliderPrixMax;
    @FXML private Label        lblPrixMin;
    @FXML private Label        lblPrixMax;
    @FXML private FlowPane     flowListings;
    @FXML private Label        lblPageInfo;
    @FXML private Button       btnPrev;
    @FXML private Button       btnNext;
    @FXML private Label        lblTotalResults;

    // ── MY LISTINGS tab UI ────────────────────────────────────────────────────
    @FXML private HBox   hboxSellerSetup;
    @FXML private Button btnNewListing;
    @FXML private TableView<Listing>              tableMyListings;
    @FXML private TableColumn<Listing, String>    colMyTitre;
    @FXML private TableColumn<Listing, String>    colMyCategorie;
    @FXML private TableColumn<Listing, String>    colMyPrix;
    @FXML private TableColumn<Listing, String>    colMyUnite;
    @FXML private TableColumn<Listing, String>    colMyQte;
    @FXML private TableColumn<Listing, String>    colMyWilaya;
    @FXML private TableColumn<Listing, String>    colMyStatut;
    @FXML private TableColumn<Listing, String>    colMyVues;
    @FXML private TableColumn<Listing, String>    colMyActions;

    // ── State – browse ────────────────────────────────────────────────────────
    private final ObservableList<Listing> listings = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int totalPages  = 1;
    private int pageSize    = 20;
    private int totalResults = 0;

    private String     currentSearch   = "";
    private String     currentCategorie = null;
    private String     currentWilaya    = null;
    private BigDecimal currentPrixMin   = null;
    private BigDecimal currentPrixMax   = null;
    private String     currentSortBy   = "recent";

    // ── State – my listings ───────────────────────────────────────────────────
    private final ObservableList<Listing> myListings = FXCollections.observableArrayList();
    private SellerProfile currentSeller = null;

    // ── Static data ───────────────────────────────────────────────────────────
    private static final String[] WILAYAS = {
        "Ariana","Béja","Ben Arous","Bizerte","Gabès","Gafsa",
        "Jendouba","Kairouan","Kasserine","Kébili","Kef",
        "Mahdia","Manouba","Médénine","Monastir","Nabeul",
        "Sfax","Sidi Bouzid","Siliana","Sousse","Tataouine",
        "Tozeur","Tunis","Zaghouan"
    };
    private static final String[] CATEGORIES = {
        "SEMENCE","ENGRAIS","PESTICIDE","RECOLTE","MATERIEL","BETAIL","AUTRE"
    };
    private static final String[] UNITES = {
        "kg","quintal","tonne","litre","sac","boite","unité","lot","paquet"
    };

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du MarketplaceController");
        if (!SessionManager.getInstance().isLoggedIn()) return;

        initComboBoxes();
        initSliderListeners();
        setupMyListingsTable();
        loadListings();
    }

    // ── Browse tab setup ──────────────────────────────────────────────────────

    private void initComboBoxes() {
        cmbCategorie.getItems().add("Toutes les catégories");
        cmbCategorie.getItems().addAll(CATEGORIES);
        cmbCategorie.getSelectionModel().selectFirst();

        cmbWilaya.getItems().add("Toutes les wilayas");
        cmbWilaya.getItems().addAll(WILAYAS);
        cmbWilaya.getSelectionModel().selectFirst();

        cmbSortBy.getItems().addAll("Plus récent","Prix croissant","Prix décroissant","Mieux noté");
        cmbSortBy.getSelectionModel().selectFirst();

        cmbCategorie.setOnAction(e -> onFilterChanged());
        cmbWilaya.setOnAction(e -> onFilterChanged());
        cmbSortBy.setOnAction(e -> onFilterChanged());
    }

    private void initSliderListeners() {
        sliderPrixMin.valueProperty().addListener((obs, o, n) -> {
            lblPrixMin.setText(String.format("%.0f TND", n.doubleValue()));
            currentPrixMin = BigDecimal.valueOf(n.doubleValue());
        });
        sliderPrixMax.valueProperty().addListener((obs, o, n) -> {
            lblPrixMax.setText(String.format("%.0f TND", n.doubleValue()));
            currentPrixMax = BigDecimal.valueOf(n.doubleValue());
        });
        lblPrixMin.setText("0 TND");
        lblPrixMax.setText("1000 TND");
    }

    @FXML private void onSearch() {
        currentSearch = txtSearch.getText();
        currentPage = 1;
        loadListings();
    }

    private void onFilterChanged() {
        int catIdx = cmbCategorie.getSelectionModel().getSelectedIndex();
        currentCategorie = catIdx > 0 ? CATEGORIES[catIdx - 1] : null;

        int wIdx = cmbWilaya.getSelectionModel().getSelectedIndex();
        currentWilaya = wIdx > 0 ? WILAYAS[wIdx - 1] : null;

        currentSortBy = switch (cmbSortBy.getSelectionModel().getSelectedIndex()) {
            case 1 -> "price_asc";
            case 2 -> "price_desc";
            case 3 -> "rating";
            default -> "recent";
        };
        currentPage = 1;
        loadListings();
    }

    private void loadListings() {
        try {
            List<Listing> results = listingService.searchListings(
                currentSearch.isEmpty() ? null : currentSearch,
                currentCategorie, currentWilaya,
                currentPrixMin, currentPrixMax,
                null, currentPage, pageSize
            );
            totalResults = listingService.countListings(
                currentSearch.isEmpty() ? null : currentSearch,
                currentCategorie, currentWilaya,
                currentPrixMin  != null ? currentPrixMin.doubleValue()  : null,
                currentPrixMax  != null ? currentPrixMax.doubleValue()  : null,
                null
            );
            totalPages = (int) Math.ceil((double) totalResults / pageSize);
            if (totalPages == 0) totalPages = 1;

            listings.setAll(results);
            Platform.runLater(() -> { displayListings(); updatePagination(); });
        } catch (Exception e) {
            logger.error("Erreur chargement annonces", e);
        }
    }

    private void displayListings() {
        flowListings.getChildren().clear();
        for (Listing l : listings) flowListings.getChildren().add(createListingCard(l));
        lblTotalResults.setText(String.format("%d résultat(s) trouvé(s)", totalResults));
    }

    private VBox createListingCard(Listing listing) {
        VBox card = new VBox(8);
        card.setPrefWidth(255);
        card.setPrefHeight(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPadding(new Insets(12));

        try {
            ImageView imgView = new ImageView();
            imgView.setFitWidth(231); imgView.setFitHeight(120);
            imgView.setPreserveRatio(true); imgView.setSmooth(true);
            try {
                List<ListingImage> images = listingService.getImagesByListing(listing.getId());
                if (!images.isEmpty()) {
                    File f = new File(images.get(0).getImageUrl());
                    if (f.exists()) imgView.setImage(new Image(f.toURI().toString()));
                }
            } catch (Exception ignored) {}

            Label lblTitre = new Label(listing.getTitre());
            lblTitre.setFont(Font.font("System", FontWeight.BOLD, 13));
            lblTitre.setWrapText(true); lblTitre.setMaxWidth(231);

            Label lblPrix = new Label(CurrencyUtils.format(listing.getPrix()) + " / " + listing.getUnite());
            lblPrix.setFont(Font.font("System", FontWeight.BOLD, 15));
            lblPrix.setStyle("-fx-text-fill: #2E7D32;");

            Label lblVendeur = new Label(getSellerInfo(listing.getSellerId()));
            lblVendeur.setFont(Font.font("System", 10));
            lblVendeur.setStyle("-fx-text-fill: #757575;");

            Label lblWilaya = new Label(listing.getWilaya() != null ? listing.getWilaya() : "");
            lblWilaya.setFont(Font.font("System", 10));
            lblWilaya.setStyle("-fx-text-fill: #757575;");

            HBox btns = new HBox(8);
            btns.setAlignment(Pos.CENTER_RIGHT);
            Button btnVoir = new Button("Voir détails");
            btnVoir.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 4;");
            btnVoir.setOnAction(e -> onViewListing(listing));
            Button btnCart = new Button("🛒");
            btnCart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4;");
            btnCart.setOnAction(e -> onAddToCart(listing));
            btns.getChildren().addAll(btnVoir, btnCart);

            card.getChildren().addAll(imgView, lblTitre, lblPrix, lblVendeur, lblWilaya, btns);

            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"));
            card.setOnMouseExited(e  -> card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
        } catch (Exception e) {
            logger.error("Erreur carte annonce {}", listing.getId(), e);
        }
        return card;
    }

    private String getSellerInfo(int sellerId) {
        try {
            SellerProfile sp = sellerService.getSellerProfileById(sellerId);
            if (sp != null) {
                String r = sp.getNoteMoyenne() > 0 ? String.format("%.1f", sp.getNoteMoyenne()) : "N/A";
                return sp.getNomBoutique() + " ⭐ " + r;
            }
        } catch (Exception ignored) {}
        return "Vendeur";
    }

    private void updatePagination() {
        lblPageInfo.setText(String.format("Page %d / %d", currentPage, totalPages));
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    @FXML private void onPreviousPage() { if (currentPage > 1) { currentPage--; loadListings(); } }
    @FXML private void onNextPage()     { if (currentPage < totalPages) { currentPage++; loadListings(); } }
    @FXML private void onRefresh()      { loadListings(); }

    /** Show listing detail in a dialog. */
    @FXML
    private void onViewListing(Listing listing) {
        try {
            listingService.incrementVues(listing.getId());
        } catch (Exception ignored) {}

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Détail de l'annonce");
        dialog.setHeaderText(listing.getTitre());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int row = 0;
        addDetailRow(grid, row++, "Catégorie:", listing.getCategorie());
        addDetailRow(grid, row++, "Prix:",      CurrencyUtils.format(listing.getPrix()) + " / " + listing.getUnite());
        addDetailRow(grid, row++, "Quantité disponible:", String.valueOf(listing.getQuantiteDisponible()) + " " + listing.getUnite());
        addDetailRow(grid, row++, "Wilaya:",    listing.getWilaya() != null ? listing.getWilaya() : "—");
        addDetailRow(grid, row++, "Statut:",    listing.getStatut() != null ? listing.getStatut().name() : "—");
        addDetailRow(grid, row++, "Vues:",      String.valueOf(listing.getVues()));

        if (listing.getDescription() != null && !listing.getDescription().isEmpty()) {
            Label lblDescTitle = new Label("Description:");
            lblDescTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            TextArea ta = new TextArea(listing.getDescription());
            ta.setEditable(false); ta.setWrapText(true);
            ta.setPrefRowCount(3); ta.setPrefWidth(350);
            grid.add(lblDescTitle, 0, row);
            grid.add(ta, 1, row++);
        }

        dialog.getDialogPane().setContent(grid);
        ButtonType btnCart = new ButtonType("Ajouter au panier 🛒", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCart, ButtonType.CLOSE);
        dialog.setResultConverter(bt -> bt);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != null && bt.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                onAddToCart(listing);
            }
        });
    }

    private void addDetailRow(GridPane grid, int row, String labelText, String value) {
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label val = new Label(value != null ? value : "—");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    @FXML
    private void onAddToCart(Listing listing) {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) { AlertUtil.showError("Erreur", "Veuillez vous connecter."); return; }
        try {
            cartService.addToCart(u.getId(), listing.getId(), 1.0);
            AlertUtil.showInfo("Panier", "Article ajouté au panier!");
        } catch (Exception e) {
            logger.error("Erreur ajout panier", e);
            AlertUtil.showError("Erreur", "Impossible d'ajouter au panier: " + e.getMessage());
        }
    }

    // ── Mes Annonces tab ──────────────────────────────────────────────────────

    @FXML
    private void onMesAnnoncesTabSelected() {
        if (tabMesAnnonces.isSelected()) {
            loadMyListings();
        }
    }

    @FXML private void onRefreshMyListings() { loadMyListings(); }

    private void loadMyListings() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;

        currentSeller = sellerService.getSellerProfileByUserId(u.getId());

        if (currentSeller == null) {
            hboxSellerSetup.setVisible(true);
            hboxSellerSetup.setManaged(true);
            btnNewListing.setDisable(true);
            myListings.clear();
            tableMyListings.setItems(myListings);
            return;
        }

        hboxSellerSetup.setVisible(false);
        hboxSellerSetup.setManaged(false);
        btnNewListing.setDisable(false);

        try {
            List<Listing> list = listingService.getListingsBySeller(currentSeller.getId());
            myListings.setAll(list);
            tableMyListings.setItems(myListings);
        } catch (Exception e) {
            logger.error("Erreur chargement mes annonces", e);
            AlertUtil.showError("Erreur", "Impossible de charger vos annonces: " + e.getMessage());
        }
    }

    private void setupMyListingsTable() {
        colMyTitre    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        colMyCategorie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategorie()));
        colMyPrix     .setCellValueFactory(c -> new SimpleStringProperty(
                CurrencyUtils.format(c.getValue().getPrix())));
        colMyUnite    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnite()));
        colMyQte      .setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getQuantiteDisponible())));
        colMyWilaya   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWilaya()));
        colMyStatut   .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatut() != null ? c.getValue().getStatut().name() : ""));
        colMyVues     .setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getVues())));

        // Actions column with Edit / Delete buttons
        colMyActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏️ Modifier");
            private final Button btnDelete = new Button("🗑️ Supprimer");
            private final HBox   box       = new HBox(6, btnEdit, btnDelete);
            {
                btnEdit.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                btnDelete.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                btnEdit.setOnAction(e -> {
                    Listing l = getTableView().getItems().get(getIndex());
                    onEditListing(l);
                });
                btnDelete.setOnAction(e -> {
                    Listing l = getTableView().getItems().get(getIndex());
                    onDeleteListing(l);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableMyListings.setItems(myListings);
    }

    // ── CREATE listing ────────────────────────────────────────────────────────

    @FXML
    private void onNewListing() {
        if (currentSeller == null) {
            AlertUtil.showWarning("Profil vendeur manquant", "Créez d'abord votre profil vendeur.");
            return;
        }
        showListingForm(null);
    }

    // ── EDIT listing ──────────────────────────────────────────────────────────

    private void onEditListing(Listing listing) {
        showListingForm(listing);
    }

    // ── DELETE listing ────────────────────────────────────────────────────────

    private void onDeleteListing(Listing listing) {
        if (!AlertUtil.showConfirmation("Supprimer l'annonce",
                "Voulez-vous vraiment supprimer \"" + listing.getTitre() + "\" ?")) return;
        try {
            boolean ok = listingService.deleteListing(listing.getId(), currentSeller.getId());
            if (ok) {
                AlertUtil.showInfo("Suppression", "Annonce supprimée avec succès.");
                loadMyListings();
            } else {
                AlertUtil.showError("Erreur", "Impossible de supprimer l'annonce.");
            }
        } catch (Exception e) {
            logger.error("Erreur suppression annonce {}", listing.getId(), e);
            AlertUtil.showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    // ── FORM dialog (create & edit) ───────────────────────────────────────────

    private void showListingForm(Listing existing) {
        boolean isEdit = (existing != null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier l'annonce" : "Nouvelle Annonce");
        dialog.setHeaderText(isEdit ? "Modifier : " + existing.getTitre() : "Créer une nouvelle annonce");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField   fTitre   = new TextField(isEdit ? existing.getTitre() : "");
        fTitre.setPromptText("Titre de l'annonce"); fTitre.setPrefWidth(280);

        TextArea    fDesc    = new TextArea(isEdit ? existing.getDescription() : "");
        fDesc.setPromptText("Description"); fDesc.setPrefRowCount(3); fDesc.setPrefWidth(280);

        ComboBox<String> fCat = new ComboBox<>();
        fCat.getItems().addAll(CATEGORIES);
        if (isEdit && existing.getCategorie() != null) fCat.setValue(existing.getCategorie());
        else fCat.getSelectionModel().selectFirst();

        TextField fPrix = new TextField(isEdit ? String.valueOf(existing.getPrix()) : "");
        fPrix.setPromptText("Prix (ex: 85.50)");

        ComboBox<String> fUnite = new ComboBox<>();
        fUnite.getItems().addAll(UNITES);
        if (isEdit && existing.getUnite() != null) fUnite.setValue(existing.getUnite());
        else fUnite.getSelectionModel().selectFirst();

        TextField fQte = new TextField(isEdit ? String.valueOf(existing.getQuantiteDisponible()) : "");
        fQte.setPromptText("Quantité disponible");

        ComboBox<String> fWilaya = new ComboBox<>();
        fWilaya.getItems().addAll(WILAYAS);
        if (isEdit && existing.getWilaya() != null) fWilaya.setValue(existing.getWilaya());
        else fWilaya.getSelectionModel().selectFirst();

        ComboBox<String> fStatut = new ComboBox<>();
        fStatut.getItems().addAll("BROUILLON", "ACTIF", "PAUSE");
        if (isEdit && existing.getStatut() != null) fStatut.setValue(existing.getStatut().name());
        else fStatut.setValue("ACTIF");

        int r = 0;
        addFormRow(grid, r++, "Titre *",       fTitre);
        addFormRow(grid, r++, "Description",   fDesc);
        addFormRow(grid, r++, "Catégorie *",   fCat);
        addFormRow(grid, r++, "Prix (TND) *",  fPrix);
        addFormRow(grid, r++, "Unité *",       fUnite);
        addFormRow(grid, r++, "Quantité *",    fQte);
        addFormRow(grid, r++, "Wilaya",        fWilaya);
        addFormRow(grid, r++, "Statut",        fStatut);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnSave = new ButtonType(isEdit ? "Enregistrer" : "Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            // Validate
            String titre = fTitre.getText().trim();
            if (titre.isEmpty()) { AlertUtil.showWarning("Validation", "Le titre est obligatoire."); return; }

            double prix; double qte;
            try {
                prix = Double.parseDouble(fPrix.getText().trim().replace(",", "."));
                qte  = Double.parseDouble(fQte.getText().trim().replace(",", "."));
            } catch (NumberFormatException ex) {
                AlertUtil.showWarning("Validation", "Prix et quantité doivent être des nombres valides."); return;
            }
            if (prix <= 0 || qte < 0) {
                AlertUtil.showWarning("Validation", "Le prix doit être > 0 et la quantité >= 0."); return;
            }

            Listing l = isEdit ? existing : new Listing();
            l.setTitre(titre);
            l.setDescription(fDesc.getText().trim());
            l.setCategorie(fCat.getValue());
            l.setPrix(prix);
            l.setUnite(fUnite.getValue());
            l.setQuantiteDisponible(qte);
            l.setWilaya(fWilaya.getValue());
            l.setStatut(ListingStatus.valueOf(fStatut.getValue()));

            try {
                if (isEdit) {
                    l.setSellerId(currentSeller.getId());
                    boolean ok = listingService.updateListing(l);
                    if (ok) { AlertUtil.showInfo("Succès", "Annonce modifiée avec succès."); loadMyListings(); }
                    else    { AlertUtil.showError("Erreur", "Modification échouée."); }
                } else {
                    l.setSellerId(currentSeller.getId());
                    Listing created = listingService.createListing(l);
                    if (created != null) { AlertUtil.showInfo("Succès", "Annonce créée avec succès."); loadMyListings(); }
                    else                { AlertUtil.showError("Erreur", "Création échouée."); }
                }
            } catch (Exception e) {
                logger.error("Erreur save listing", e);
                AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
            }
        });
    }

    private void addFormRow(GridPane grid, int row, String label, javafx.scene.Node field) {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        lbl.setMinWidth(120);
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
    }

    // ── Create Seller Profile ─────────────────────────────────────────────────

    @FXML
    private void onCreateSellerProfile() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Créer mon profil vendeur");
        dialog.setHeaderText("Remplissez les informations de votre boutique");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField fNom  = new TextField(); fNom.setPromptText("Nom de la boutique"); fNom.setPrefWidth(250);
        TextField fTel  = new TextField(); fTel.setPromptText("Téléphone (optionnel)");
        TextArea  fDesc = new TextArea();  fDesc.setPromptText("Description (optionnel)"); fDesc.setPrefRowCount(2);
        ComboBox<String> fWilaya = new ComboBox<>(); fWilaya.getItems().addAll(WILAYAS); fWilaya.getSelectionModel().selectFirst();

        addFormRow(grid, 0, "Nom boutique *", fNom);
        addFormRow(grid, 1, "Wilaya",         fWilaya);
        addFormRow(grid, 2, "Téléphone",      fTel);
        addFormRow(grid, 3, "Description",    fDesc);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnCreate = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCreate, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;
            String nom = fNom.getText().trim();
            if (nom.isEmpty()) { AlertUtil.showWarning("Validation", "Le nom de la boutique est obligatoire."); return; }

            SellerProfile sp = new SellerProfile(u.getId(), nom, fWilaya.getValue());
            sp.setTelephone(fTel.getText().trim());
            sp.setDescription(fDesc.getText().trim());
            sp.setStatut("APPROUVE");

            boolean ok = sellerService.createSellerProfile(sp);
            if (ok) {
                AlertUtil.showInfo("Succès", "Profil vendeur créé! Vous pouvez maintenant publier des annonces.");
                loadMyListings();
            } else {
                AlertUtil.showError("Erreur", "Impossible de créer le profil vendeur.");
            }
        });
    }
}
