package com.farmiq.services;

import com.farmiq.models.CartItem;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartService {
    private static final Logger logger = LogManager.getLogger(CartService.class);

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.*, l.titre as listing_titre, l.prix, l.unite, li.image_url, sp.nom_boutique, l.seller_id, l.quantite_disponible " +
                     "FROM cart_items ci " +
                     "JOIN listings l ON ci.listing_id = l.id " +
                     "JOIN seller_profiles sp ON l.seller_id = sp.id " +
                     "LEFT JOIN listing_images li ON l.id = li.listing_id AND li.is_principale = TRUE " +
                     "WHERE ci.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getInt("id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setListingId(rs.getInt("listing_id"));
                    item.setQuantite(rs.getDouble("quantite"));
                    item.setTitreListing(rs.getString("listing_titre"));
                    item.setPrix(rs.getDouble("prix"));
                    item.setUnite(rs.getString("unite"));
                    item.setImageUrl(rs.getString("image_url"));
                    item.setNomBoutique(rs.getString("nom_boutique"));
                    item.setSellerId(rs.getInt("seller_id"));
                    item.setQuantiteDisponible(rs.getDouble("quantite_disponible"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération panier", e);
        }
        return items;
    }

    public boolean addToCart(int userId, int listingId, double quantite) {
        String sql = "INSERT INTO cart_items (user_id, listing_id, quantite) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantite = quantite + ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, listingId);
            stmt.setDouble(3, quantite);
            stmt.setDouble(4, quantite);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur ajout au panier", e);
        }
        return false;
    }

    public boolean updateQuantity(int cartItemId, double quantite) {
        String sql = "UPDATE cart_items SET quantite = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantite);
            stmt.setInt(2, cartItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour quantité", e);
        }
        return false;
    }

    public boolean removeFromCart(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur suppression du panier", e);
        }
        return false;
    }

    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur vidage panier", e);
        }
        return false;
    }

    public int getCartItemCount(int userId) {
        String sql = "SELECT COUNT(*) FROM cart_items WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur comptage panier", e);
        }
        return 0;
    }

    public double getCartTotal(int userId) {
        String sql = "SELECT SUM(ci.quantite * l.prix) as total " +
                     "FROM cart_items ci " +
                     "JOIN listings l ON ci.listing_id = l.id " +
                     "WHERE ci.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur calcul total panier", e);
        }
        return 0;
    }
}
