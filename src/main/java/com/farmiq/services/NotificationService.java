package com.farmiq.services;

import com.farmiq.models.Notification;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    public boolean createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, titre, message, type, lien) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getTitre());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getType());
            stmt.setString(5, notification.getLien());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur création notification", e);
        }
        return false;
    }

    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération notifications", e);
        }
        return notifications;
    }

    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND lu = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur comptage notifications non lues", e);
        }
        return 0;
    }

    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET lu = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour notification", e);
        }
        return false;
    }

    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET lu = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour toutes les notifications", e);
        }
        return false;
    }

    public boolean deleteNotification(int notificationId) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur suppression notification", e);
        }
        return false;
    }

    public void notifyNewOrder(int sellerUserId, String buyerName, double amount) {
        Notification notification = new Notification();
        notification.setUserId(sellerUserId);
        notification.setTitre("Nouvelle commande!");
        notification.setMessage(buyerName + " a passé une commande de " + amount + " TND");
        notification.setType("COMMANDE");
        notification.setLien("/orders");
        createNotification(notification);
    }

    public void notifyOrderStatusChanged(int buyerUserId, int orderId, String status) {
        Notification notification = new Notification();
        notification.setUserId(buyerUserId);
        notification.setTitre("Statut commande mis à jour");
        notification.setMessage("Votre commande #" + orderId + " est maintenant: " + status);
        notification.setType("INFO");
        notification.setLien("/orders");
        createNotification(notification);
    }

    public void notifyLowStock(int userId, String productName) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitre("Alerte stock");
        notification.setMessage("Le produit \"" + productName + "\" est en rupture de stock!");
        notification.setType("ALERTE");
        notification.setLien("/products");
        createNotification(notification);
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitre(rs.getString("titre"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setLu(rs.getBoolean("lu"));
        n.setLien(rs.getString("lien"));
        n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return n;
    }
}
