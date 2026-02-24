package com.farmiq.controllers;

import com.farmiq.models.CartItem;
import com.farmiq.models.Listing;
import com.farmiq.models.User;
import com.farmiq.services.CartService;
import com.farmiq.services.ListingService;
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
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

/**
 * Controller pour le panier d'achat
 */
public class CartController implements Initializable {
    
    private static final Logger logger = LogManager.getLogger(CartController.class);
    
    // Services
    private final CartService cartService = new CartService();
    private final ListingService listingService = new ListingService();
    
    // UI Elements
    @FXML private VBox vboxCartItems;
    @FXML private Label lblTotal;
    @FXML private Label lblItemCount;
    @FXML private Button btnCommander;
    @FXML private Button btnVider;
    @FXML private Label lblEmptyMessage;
    
    // Data
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du CartController");
        
        // Verify session
        if (!SessionManager.getInstance().isLoggedIn()) {
            showError("Veuillez vous connecter pour voir votre panier");
            return;
        }
        
        loadCartItems();
    }
    
    private void loadCartItems() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showError("Utilisateur non connecté");
                return;
            }
            
            // Get cart items
            List<CartItem> items = cartService.getCartItems(currentUser.getId());
            cartItems.clear();
            cartItems.addAll(items);
            
            // Calculate total and display
            Platform.runLater(() -> displayCartItems(items));
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du panier", e);
            AlertUtil.showError("Erreur", "Impossible de charger le panier: " + e.getMessage());
        }
    }
    
    private void displayCartItems(List<CartItem> items) {
        vboxCartItems.getChildren().clear();
        
        if (items.isEmpty()) {
            lblEmptyMessage.setVisible(true);
            btnCommander.setDisable(true);
            btnVider.setDisable(true);
            lblTotal.setText("0,00 TND");
            lblItemCount.setText("0 article(s)");
            return;
        }
        
        lblEmptyMessage.setVisible(false);
        btnCommander.setDisable(false);
        btnVider.setDisable(false);
        
        totalAmount = BigDecimal.ZERO;
        
        // Group items by seller
        Map<Integer, List<CartItem>> itemsBySeller = new HashMap<>();
        for (CartItem item : items) {
            itemsBySeller.computeIfAbsent(item.getListingId(), k -> new ArrayList<>()).add(item);
        }
        
        // Display each cart item
        for (CartItem item : items) {
            try {
                Listing listing = listingService.getListingById(item.getListingId());
                if (listing == null) continue;
                
                VBox itemCard = createCartItemCard(item, listing);
                vboxCartItems.getChildren().add(itemCard);
                
                // Calculate subtotal
                double subtotal = listing.getPrix() * item.getQuantite();
                totalAmount = totalAmount.add(BigDecimal.valueOf(subtotal));
                
            } catch (Exception e) {
                logger.error("Erreur lors de l'affichage de l'item: {}", item.getId(), e);
            }
        }
        
        // Update totals
        lblTotal.setText(CurrencyUtils.format(totalAmount.doubleValue()));
        lblItemCount.setText(String.format("%d article(s)", items.size()));
    }
    
    private VBox createCartItemCard(CartItem item, Listing listing) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                     "-fx-border-color: #E0E0E0; -fx-border-radius: 8;");
        card.setPadding(new Insets(16));
        
        // Title and price row
        HBox topRow = new HBox();
        topRow.setSpacing(8);
        
        Label lblTitre = new Label(listing.getTitre());
        lblTitre.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitre.setPrefWidth(300);
        
        Label lblPrix = new Label(CurrencyUtils.format(listing.getPrix()) + " / " + listing.getUnite());
        lblPrix.setFont(Font.font("System", 12));
        lblPrix.setStyle("-fx-text-fill: #757575;");
        
        topRow.getChildren().addAll(lblTitre, lblPrix);
        
        // Quantity row
        HBox qtyRow = new HBox(8);
        qtyRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label lblQteLabel = new Label("Quantité:");
        lblQteLabel.setFont(Font.font("System", 12));
        
        Button btnMinus = new Button("-");
        btnMinus.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 4;");
        btnMinus.setPrefWidth(30);
        
        Label lblQte = new Label(String.valueOf(item.getQuantite()));
        lblQte.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblQte.setPrefWidth(40);
        lblQte.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button btnPlus = new Button("+");
        btnPlus.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 4;");
        btnPlus.setPrefWidth(30);
        
        // Quantity change handlers
        btnMinus.setOnAction(e -> {
            double newQty = item.getQuantite() - 1;
            if (newQty >= 1) {
                updateQuantity(item, newQty);
            }
        });
        
        btnPlus.setOnAction(e -> {
            double newQty = item.getQuantite() + 1;
            if (newQty <= listing.getQuantiteDisponible()) {
                updateQuantity(item, newQty);
            } else {
                AlertUtil.showWarning("Stock insuffisant", 
                    "Quantité maximale disponible: " + listing.getQuantiteDisponible());
            }
        });
        
        Label lblSousTotal = new Label();
        double sousTotal = listing.getPrix() * item.getQuantite();
        lblSousTotal.setText("Sous-total: " + CurrencyUtils.format(sousTotal));
        lblSousTotal.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblSousTotal.setStyle("-fx-text-fill: #2E7D32;");
        
        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-background-radius: 4;");
        btnSupprimer.setOnAction(e -> onRemoveItem(item));
        
        qtyRow.getChildren().addAll(lblQteLabel, btnMinus, lblQte, btnPlus, lblSousTotal, btnSupprimer);
        
        card.getChildren().addAll(topRow, qtyRow);
        
        return card;
    }
    
    private void updateQuantity(CartItem item, double newQuantity) {
        try {
            cartService.updateQuantity(item.getId(), newQuantity);
            item.setQuantite(newQuantity);
            loadCartItems(); // Refresh
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de la quantité", e);
            AlertUtil.showError("Erreur", "Impossible de modifier la quantité");
        }
    }
    
    @FXML
    private void onRemoveItem(CartItem item) {
        try {
            AlertDialog dialog = new AlertDialog();
            if (dialog.showConfirmation("Supprimer l'article", 
                "Voulez-vous vraiment supprimer cet article du panier?")) {
                
                cartService.removeFromCart(item.getId());
                loadCartItems();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression", e);
            AlertUtil.showError("Erreur", "Impossible de supprimer l'article");
        }
    }
    
    @FXML
    private void onViderPanier() {
        try {
            AlertDialog dialog = new AlertDialog();
            if (dialog.showConfirmation("Vider le panier", 
                "Voulez-vous vraiment vider votre panier?")) {
                
                User currentUser = SessionManager.getInstance().getCurrentUser();
                cartService.clearCart(currentUser.getId());
                loadCartItems();
            }
        } catch (Exception e) {
            logger.error("Erreur lors du vidage du panier", e);
            AlertUtil.showError("Erreur", "Impossible de vider le panier");
        }
    }
    
    @FXML
    private void onCommander() {
        try {
            if (cartItems.isEmpty()) {
                AlertUtil.showWarning("Panier vide", "Votre panier est vide");
                return;
            }
            
            // Navigate to checkout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/checkout.fxml"));
            Parent root = loader.load();
            
            // Get main stage and navigate
            // NavigationManager.getInstance().navigateTo(root);
            
            AlertUtil.showInfo("Commande", "Redirection vers la page de paiement...");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la navigation vers le checkout", e);
            AlertUtil.showError("Erreur", "Impossible de procéder au paiement");
        }
    }
    
    @FXML
    private void onRefresh() {
        loadCartItems();
    }
    
    private void showError(String message) {
        lblEmptyMessage.setText(message);
        lblEmptyMessage.setVisible(true);
    }
    
    /**
     * Simple alert dialog helper
     */
    private static class AlertDialog {
        public boolean showConfirmation(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }
    }
}
