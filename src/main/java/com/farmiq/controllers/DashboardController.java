package com.farmiq.controllers;

import com.farmiq.models.Transaction;
import com.farmiq.models.User;
import com.farmiq.services.TransactionService;
import com.farmiq.services.UserService;
import com.farmiq.services.WeatherService;
import com.farmiq.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller pour le tableau de bord principal
 */
public class DashboardController implements Initializable {
    
    private static final Logger logger = LogManager.getLogger(DashboardController.class);
    
    // Services
    private final TransactionService transactionService = new TransactionService();
    private final UserService userService = new UserService();
    
    // KPI Labels
    @FXML private Label lblVentesMois;
    @FXML private Label lblAchatsMois;
    @FXML private Label lblBeneficeNet;
    @FXML private Label lblUtilisateursActifs;
    @FXML private Label lblProduitsAlerte;
    @FXML private Label lblCommandesEnAttente;
    @FXML private Label lblNouvellesAnnonces;
    
    // Weather Widget
    @FXML private Label lblWeatherCity;
    @FXML private Label lblWeatherTemp;
    @FXML private Label lblWeatherDesc;
    @FXML private Label lblWeatherIcon;
    
    // Charts
    @FXML private LineChart<String, Number> chartRevenus;
    @FXML private PieChart chartCategories;
    
    // Recent Orders Table
    @FXML private TableView<Transaction> tableRecentTransactions;
    @FXML private TableColumn<Transaction, String> colTransactionDate;
    @FXML private TableColumn<Transaction, String> colTransactionType;
    @FXML private TableColumn<Transaction, String> colTransactionMontant;
    @FXML private TableColumn<Transaction, String> colTransactionStatut;
    
    private ObservableList<Transaction> recentTransactions = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du DashboardController");
        
        // Verify session
        if (!SessionManager.getInstance().isLoggedIn()) {
            logger.warn("Utilisateur non connecté");
            return;
        }
        
        // Initialize columns
        setupColumns();
        
        // Load data
        loadKPIData();
        loadChartsData();
        loadRecentTransactions();
        loadWeatherData();
    }
    
    private void setupColumns() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        colTransactionDate.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getDate() != null ? 
                    cellData.getValue().getDate().format(formatter) : ""
            )
        );
        
        colTransactionType.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getType() != null ? 
                    cellData.getValue().getType() : ""
            )
        );
        
        colTransactionMontant.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> String.format("%,.2f TND", cellData.getValue().getMontant())
            )
        );
        
        colTransactionStatut.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getStatut() != null ? 
                    cellData.getValue().getStatut() : ""
            )
        );
        
        tableRecentTransactions.setItems(recentTransactions);
    }
    
    private void loadKPIData() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            boolean isAdmin = currentUser != null && currentUser.getRole() != null && 
                             currentUser.getRole().getName().equals("ADMIN");
            
            // Ventes ce mois (simulated - would need proper transaction service)
            lblVentesMois.setText("12 450,00 TND");
            
            // Achats ce mois
            lblAchatsMois.setText("8 320,00 TND");
            
            // Bénéfice net
            lblBeneficeNet.setText("4 130,00 TND");
            
            // Utilisateurs actifs
            if (isAdmin) {
                int activeUsers = userService.getActiveUserCount();
                lblUtilisateursActifs.setText(String.valueOf(activeUsers));
            } else {
                lblUtilisateursActifs.setText("—");
            }
            
            // Produits en alerte stock (would need ProductService)
            lblProduitsAlerte.setText("3");
            
            // Commandes en attente (would need OrderService)
            lblCommandesEnAttente.setText("5");
            
            // Nouvelles annonces ce mois (would need ListingService)
            lblNouvellesAnnonces.setText("8");
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des KPIs", e);
        }
    }
    
    private void loadChartsData() {
        try {
            // Revenus chart - 6 derniers mois
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenus");
            
            // Sample data - in production, fetch from OrderService
            series.getData().add(new XYChart.Data<>("Sep", 8500));
            series.getData().add(new XYChart.Data<>("Oct", 9200));
            series.getData().add(new XYChart.Data<>("Nov", 7800));
            series.getData().add(new XYChart.Data<>("Dec", 11000));
            series.getData().add(new XYChart.Data<>("Jan", 10500));
            series.getData().add(new XYChart.Data<>("Fév", 12450));
            
            chartRevenus.getData().add(series);
            chartRevenus.setTitle("Revenus des 6 derniers mois");
            
            // Categories pie chart
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Semences", 35),
                new PieChart.Data("Engrais", 25),
                new PieChart.Data("Récolte", 20),
                new PieChart.Data("Matériel", 15),
                new PieChart.Data("Autre", 5)
            );
            
            chartCategories.setData(pieData);
            chartCategories.setTitle("Répartition par catégorie");
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des graphiques", e);
        }
    }
    
    private void loadRecentTransactions() {
        try {
            // Load recent transactions (5 last)
            List<Transaction> transactions = transactionService.getRecentTransactions(5);
            recentTransactions.clear();
            recentTransactions.addAll(transactions);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des transactions récentes", e);
        }
    }
    
    private void loadWeatherData() {
        try {
            WeatherService.WeatherData weather = WeatherService.getInstance().getCurrentWeather();
            if (weather != null) {
                Platform.runLater(() -> {
                    lblWeatherCity.setText(weather.city);
                    lblWeatherTemp.setText(String.format("%.1f°C", weather.temperature));
                    lblWeatherDesc.setText(weather.description);
                    lblWeatherIcon.setText(weather.icon);
                });
            } else {
                lblWeatherCity.setText("Tunis");
                lblWeatherTemp.setText("--°C");
                lblWeatherDesc.setText("Indisponible");
                lblWeatherIcon.setText("🌡️");
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la météo", e);
            lblWeatherCity.setText("Tunis");
            lblWeatherTemp.setText("--°C");
            lblWeatherDesc.setText("Erreur");
            lblWeatherIcon.setText("🌡️");
        }
    }
    
    @FXML
    private void refreshData() {
        loadKPIData();
        loadRecentTransactions();
        loadWeatherData();
    }
}
