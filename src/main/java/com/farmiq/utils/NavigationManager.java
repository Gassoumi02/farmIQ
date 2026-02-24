package com.farmiq.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NavigationManager {
    private static final Logger logger = LogManager.getLogger(NavigationManager.class);
    
    private static NavigationManager instance;
    private Stage primaryStage;
    private final Map<String, String> routes;
    
    private NavigationManager() {
        routes = new HashMap<>();
        initializeRoutes();
    }
    
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    private void initializeRoutes() {
        routes.put("login", "/views/fxml/Login.fxml");
        routes.put("register", "/views/fxml/Register.fxml");
        routes.put("admin", "/views/fxml/AdminLayout.fxml");
        routes.put("user", "/views/fxml/Userlayout.fxml");
        routes.put("dashboard", "/views/fxml/AdminLayout.fxml");
        routes.put("transactions", "/views/fxml/AdminTransactions.fxml");
        routes.put("users", "/views/fxml/AdminUsers.fxml");
        routes.put("parcelles", "/views/fxml/Parcelles_farmiq.fxml");
        routes.put("plantes", "/views/fxml/Plantes.fxml");
        routes.put("profil", "/views/fxml/Profil.fxml");
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    public void navigateTo(String routeKey) {
        navigateTo(routeKey, null);
    }
    
    public void navigateTo(String routeKey, Object data) {
        String fxmlPath = routes.get(routeKey);
        if (fxmlPath == null) {
            logger.warn("Route non trouvée: " + routeKey);
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (data != null) {
                Object controller = loader.getController();
                if (controller instanceof javafx.fxml.Initializable) {
                    ((javafx.fxml.Initializable) controller).initialize(null, null);
                }
            }
            
            if (primaryStage == null) {
                logger.error("PrimaryStage non défini");
                return;
            }
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            logger.info("Navigation vers: " + routeKey);
        } catch (IOException e) {
            logger.error("Erreur navigation vers: " + routeKey, e);
        }
    }
    
    public void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (primaryStage != null && primaryStage.getScene() != null) {
                primaryStage.getScene().setRoot(root);
            }
            
            logger.info("Contenu chargé: " + fxmlPath);
        } catch (IOException e) {
            logger.error("Erreur chargement contenu: " + fxmlPath, e);
        }
    }
    
    public void registerRoute(String key, String fxmlPath) {
        routes.put(key, fxmlPath);
    }
}
