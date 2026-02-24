package com.farmiq;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            if (!DatabaseConnection.testConnection()) {
                logger.error("Impossible de se connecter à la base de données");
                System.err.println("❌ ERREUR: Vérifiez que MySQL est lancé et que la base 'farmiq' existe");
                System.exit(1);
            }
            logger.info("✅ Connexion DB réussie");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/css/admin.css").toExternalForm());

            primaryStage.setTitle("FarmIQ - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

            logger.info("🚀 Application FarmIQ démarrée");

        } catch (Exception e) {
            logger.error("Erreur démarrage application", e);
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
        logger.info("👋 Application FarmIQ fermée");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
