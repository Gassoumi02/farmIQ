package com.farmiq.controllers;

import com.farmiq.models.Listing;
import com.farmiq.models.ListingImage;
import com.farmiq.models.SellerProfile;
import com.farmiq.models.User;
import com.farmiq.services.CartService;
import com.farmiq.services.ListingService;
import com.farmiq.services.SellerService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.CurrencyUtils;
import com.farmiq.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import java.util.stream.Collectors;

/**
 * Controller pour la navigation principale du marketplace
 */
public class MarketplaceController implements Initializable {
    
    private static final Logger logger = LogManager.getLogger(MarketplaceController.class);
    
    // Services
    private final ListingService listingService = new ListingService();
    private final SellerService sellerService = new SellerService();
    private final CartService cartService = new CartService();
    
    // UI Elements
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbCategorie;
    @FXML private ComboBox<String> cmbWilaya;
    @FXML private ComboBox<String> cmbSortBy;
    @FXML private Slider sliderPrixMin;
    @FXML private Slider sliderPrixMax;
    @FXML private Label lblPrixMin;
    @FXML private Label lblPrixMax;
    @FXML private FlowPane flowListings;
    @FXML private Label lblPageInfo;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label lblTotalResults;
    
    // Data
    private ObservableList<Listing> listings = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = 20;
    private int totalResults = 0;
    
    // Search filters
    private String currentSearch = "";
    private String currentCategorie = null;
    private String currentWilaya = null;
    private BigDecimal currentPrixMin = null;
    private BigDecimal currentPrixMax = null;
    private String currentSortBy = "recent";
    
    // Wilayas for Tunisia
    private static final String[] WILAYAS = {
        "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa", 
        "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", 
        "Mahdia", "Manouba", "Médénine", "Monastir", "Nabeul", 
        "Sfax", "Sidi Bouzid", "Siliana", "Sousse", "Tunis", 
        "Zaghouan"
    };
    
    // Categories
    private static final String[] CATEGORIES = {
        "SEMENCE", "ENGRAIS", "PESTICIDE", "RECOLTE", "MATERIEL", "BETAIL", "AUTRE"
    };
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du MarketplaceController");
        
        // Verify session
        if (!SessionManager.getInstance().isLoggedIn()) {
            logger.warn("Utilisateur non connecté");
            return;
        }
        
        // Initialize dropdowns
        initComboBoxes();
        initSliderListeners();
        
        // Load initial data
        loadListings();
    }
    
    private void initComboBoxes() {
        // Categories
        cmbCategorie.getItems().add("Toutes les catégories");
        cmbCategorie.getItems().addAll(CATEGORIES);
        cmbCategorie.getSelectionModel().selectFirst();
        
        // Wilayas
        cmbWilaya.getItems().add("Toutes les wilayas");
        cmbWilaya.getItems().addAll(WILAYAS);
        cmbWilaya.getSelectionModel().selectFirst();
        
        // Sort options
        cmbSortBy.getItems().addAll(
            "Plus récent",
            "Prix croissant",
            "Prix décroissant",
            "Mieux noté"
        );
        cmbSortBy.getSelectionModel().selectFirst();
        
        // Add listeners
        cmbCategorie.setOnAction(e -> onFilterChanged());
        cmbWilaya.setOnAction(e -> onFilterChanged());
        cmbSortBy.setOnAction(e -> onFilterChanged());
    }
    
    private void initSliderListeners() {
        sliderPrixMin.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblPrixMin.setText(String.format("%.0f TND", newVal.doubleValue()));
            currentPrixMin = BigDecimal.valueOf(newVal.doubleValue());
        });
        
        sliderPrixMax.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblPrixMax.setText(String.format("%.0f TND", newVal.doubleValue()));
            currentPrixMax = BigDecimal.valueOf(newVal.doubleValue());
        });
        
        // Initialize labels
        lblPrixMin.setText("0 TND");
        lblPrixMax.setText("1000 TND");
    }
    
    @FXML
    private void onSearch() {
        currentSearch = txtSearch.getText();
        currentPage = 1;
        loadListings();
    }
    
    private void onFilterChanged() {
        // Update filter values
        int catIndex = cmbCategorie.getSelectionModel().getSelectedIndex();
        currentCategorie = catIndex > 0 ? CATEGORIES[catIndex - 1] : null;
        
        int wilayaIndex = cmbWilaya.getSelectionModel().getSelectedIndex();
        currentWilaya = wilayaIndex > 0 ? WILAYAS[wilayaIndex - 1] : null;
        
        int sortIndex = cmbSortBy.getSelectionModel().getSelectedIndex();
        currentSortBy = switch (sortIndex) {
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
            // Load listings with filters
            List<Listing> results = listingService.searchListings(
                currentSearch.isEmpty() ? null : currentSearch,
                currentCategorie,
                currentWilaya,
                currentPrixMin,
                currentPrixMax,
                null, // active only
                currentPage,
                pageSize
            );
            
            // Get total count
            totalResults = listingService.countListings(
                currentSearch.isEmpty() ? null : currentSearch,
                currentCategorie,
                currentWilaya,
                currentPrixMin != null ? currentPrixMin.doubleValue() : null,
                currentPrixMax != null ? currentPrixMax.doubleValue() : null,
                null
            );
            
            totalPages = (int) Math.ceil((double) totalResults / pageSize);
            if (totalPages == 0) totalPages = 1;
            
            listings.clear();
            listings.addAll(results);
            
            // Update UI on FX thread
            Platform.runLater(() -> {
                displayListings();
                updatePagination();
            });
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des annonces", e);
            AlertUtil.showError("Erreur", "Impossible de charger les annonces: " + e.getMessage());
        }
    }
    
    private void displayListings() {
        flowListings.getChildren().clear();
        
        for (Listing listing : listings) {
            VBox card = createListingCard(listing);
            flowListings.getChildren().add(card);
        }
        
        lblTotalResults.setText(String.format("%d résultat(s) trouvé(s)", totalResults));
    }
    
    private VBox createListingCard(Listing listing) {
        VBox card = new VBox(8);
        card.setPrefWidth(280);
        card.setPrefHeight(320);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPadding(new Insets(12));
        
        try {
            // Image
            ImageView imgView = new ImageView();
            imgView.setFitWidth(256);
            imgView.setFitHeight(140);
            imgView.setPreserveRatio(true);
            imgView.setSmooth(true);
            
            // Try to load listing image
            List<ListingImage> images = listingService.getImagesByListing(listing.getId());
            if (!images.isEmpty()) {
                File imgFile = new File(images.get(0).getImageUrl());
                if (imgFile.exists()) {
                    imgView.setImage(new Image(imgFile.toURI().toString()));
                } else {
                    setPlaceholderImage(imgView);
                }
            } else {
                setPlaceholderImage(imgView);
            }
            
            // Title
            Label lblTitre = new Label(listing.getTitre());
            lblTitre.setFont(Font.font("System", FontWeight.BOLD, 14));
            lblTitre.setWrapText(true);
            lblTitre.setMaxWidth(256);
            
            // Price
            String priceText = CurrencyUtils.format(listing.getPrix()) + " / " + listing.getUnite();
            Label lblPrix = new Label(priceText);
            lblPrix.setFont(Font.font("System", FontWeight.BOLD, 16));
            lblPrix.setStyle("-fx-text-fill: #2E7D32;");
            
            // Seller info
            String sellerInfo = getSellerInfo(listing.getSellerId());
            Label lblVendeur = new Label(sellerInfo);
            lblVendeur.setFont(Font.font("System", 11));
            lblVendeur.setStyle("-fx-text-fill: #757575;");
            
            // Wilaya
            Label lblWilaya = new Label(listing.getWilaya() != null ? listing.getWilaya() : "");
            lblWilaya.setFont(Font.font("System", 11));
            lblWilaya.setStyle("-fx-text-fill: #757575;");
            
            // Buttons
            HBox buttons = new HBox(8);
            buttons.setAlignment(Pos.CENTER_RIGHT);
            
            Button btnVoir = new Button("Voir");
            btnVoir.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 4;");
            btnVoir.setOnAction(e -> onViewListing(listing));
            
            Button btnAjouter = new Button("🛒");
            btnAjouter.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4;");
            btnAjouter.setOnAction(e -> onAddToCart(listing));
            
            buttons.getChildren().addAll(btnVoir, btnAjouter);
            
            card.getChildren().addAll(imgView, lblTitre, lblPrix, lblVendeur, lblWilaya, buttons);
            
            // Add click listener for the whole card
            card.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    onViewListing(listing);
                }
            });
            
            // Hover effect
            card.setOnMouseEntered(e -> {
                card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 8; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);");
            });
            
            card.setOnMouseExited(e -> {
                card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            });
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la carte: {}", listing.getId(), e);
        }
        
        return card;
    }
    
    private void setPlaceholderImage(ImageView imgView) {
        // Set a placeholder colored rectangle
        imgView.setImage(null);
        imgView.setStyle("-fx-background-color: #E0E0E0;");
    }
    
    private String getSellerInfo(int sellerId) {
        try {
            SellerProfile seller = sellerService.getSellerProfileById(sellerId);
            if (seller != null) {
                double rating = seller.getNoteMoyenne();
                String ratingStr = rating > 0 ? String.format("%.1f", rating) : "N/A";
                return seller.getNomBoutique() + " ⭐ " + ratingStr;
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du vendeur", e);
        }
        return "Vendeur";
    }
    
    private void updatePagination() {
        lblPageInfo.setText(String.format("Page %d / %d", currentPage, totalPages));
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }
    
    @FXML
    private void onPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadListings();
        }
    }
    
    @FXML
    private void onNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadListings();
        }
    }
    
    @FXML
    private void onViewListing(Listing listing) {
        try {
            // Increment views
            listingService.incrementVues(listing.getId());
            
            // Load listing detail view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/listing_detail.fxml"));
            Parent root = loader.load();
            
            // Get controller and pass listing
            // ListingDetailController controller = loader.getController();
            // controller.setListing(listing);
            
            // Navigate to detail (would need NavigationManager)
            logger.info("Navigation vers détail de l'annonce: {}", listing.getId());
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'ouverture du détail", e);
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le détail de l'annonce");
        }
    }
    
    @FXML
    private void onAddToCart(Listing listing) {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                AlertUtil.showError("Erreur", "Veuillez vous connecter pour ajouter au panier");
                return;
            }
            
            // Add to cart with default quantity 1
            cartService.addToCart(currentUser.getId(), listing.getId(), 1.0);
            
            AlertUtil.showInfo("Panier", "Article ajouté au panier!");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout au panier", e);
            AlertUtil.showError("Erreur", "Impossible d'ajouter au panier: " + e.getMessage());
        }
    }
    
    @FXML
    private void onRefresh() {
        loadListings();
    }
}
