package com.farmiq.services;

import com.farmiq.models.Order;
import com.farmiq.models.OrderItem;
import com.farmiq.models.enums.OrderStatus;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private static final Logger logger = LogManager.getLogger(OrderService.class);

    public Order createOrder(int buyerId, int sellerId, double montantTotal, double fraisLivraison, 
                            String adresseLivraison, String notes, List<OrderItem> items) {
        String sqlOrder = "INSERT INTO orders (buyer_id, seller_id, montant_total, frais_livraison, adresse_livraison, notes) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO order_items (order_id, listing_id, quantite, prix_unitaire, sous_total) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, buyerId);
                stmt.setInt(2, sellerId);
                stmt.setDouble(3, montantTotal);
                stmt.setDouble(4, fraisLivraison);
                stmt.setString(5, adresseLivraison);
                stmt.setString(6, notes);
                
                stmt.executeUpdate();
                
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    
                    try (PreparedStatement itemStmt = conn.prepareStatement(sqlItem)) {
                        for (OrderItem item : items) {
                            itemStmt.setInt(1, orderId);
                            itemStmt.setInt(2, item.getListingId());
                            itemStmt.setDouble(3, item.getQuantite());
                            itemStmt.setDouble(4, item.getPrixUnitaire());
                            itemStmt.setDouble(5, item.getSousTotal());
                            itemStmt.addBatch();
                        }
                        itemStmt.executeBatch();
                    }
                    
                    conn.commit();
                    
                    Order order = new Order();
                    order.setId(orderId);
                    order.setBuyerId(buyerId);
                    order.setSellerId(sellerId);
                    order.setMontantTotal(montantTotal);
                    order.setFraisLivraison(fraisLivraison);
                    order.setAdresseLivraison(adresseLivraison);
                    order.setNotes(notes);
                    order.setStatut(OrderStatus.EN_ATTENTE);
                    return order;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Erreur création commande", e);
        }
        return null;
    }

    public Order getOrderById(int id) {
        String sql = "SELECT o.*, u.nom as buyer_nom, u.email as buyer_email, sp.nom_boutique " +
                     "FROM orders o " +
                     "JOIN users u ON o.buyer_id = u.id " +
                     "JOIN seller_profiles sp ON o.seller_id = sp.id " +
                     "WHERE o.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(getOrderItems(order.getId()));
                return order;
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération commande", e);
        }
        return null;
    }

    public List<Order> getOrdersByBuyer(int buyerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, sp.nom_boutique FROM orders o JOIN seller_profiles sp ON o.seller_id = sp.id WHERE o.buyer_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, buyerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération commandes acheteur", e);
        }
        return orders;
    }

    public List<Order> getOrdersBySeller(int sellerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.nom as buyer_nom, u.email as buyer_email FROM orders o JOIN users u ON o.buyer_id = u.id WHERE o.seller_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération commandes vendeur", e);
        }
        return orders;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.nom as buyer_nom, sp.nom_boutique FROM orders o JOIN users u ON o.buyer_id = u.id JOIN seller_profiles sp ON o.seller_id = sp.id ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération toutes les commandes", e);
        }
        return orders;
    }

    public boolean updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour statut commande", e);
        }
        return false;
    }

    public boolean updateTrackingCode(int orderId, String codeSuivi) {
        String sql = "UPDATE orders SET code_suivi = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codeSuivi);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour code suivi", e);
        }
        return false;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, l.titre as listing_titre, li.image_url FROM order_items oi JOIN listings l ON oi.listing_id = l.id LEFT JOIN listing_images li ON l.id = li.listing_id AND li.is_principale = TRUE WHERE oi.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setListingId(rs.getInt("listing_id"));
                item.setQuantite(rs.getDouble("quantite"));
                item.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                item.setSousTotal(rs.getDouble("sous_total"));
                item.setTitreListing(rs.getString("listing_titre"));
                item.setImageUrl(rs.getString("image_url"));
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération articles commande", e);
        }
        return items;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setBuyerId(rs.getInt("buyer_id"));
        order.setSellerId(rs.getInt("seller_id"));
        order.setStatut(OrderStatus.valueOf(rs.getString("statut")));
        order.setMontantTotal(rs.getDouble("montant_total"));
        order.setFraisLivraison(rs.getDouble("frais_livraison"));
        order.setAdresseLivraison(rs.getString("adresse_livraison"));
        order.setNotes(rs.getString("notes"));
        order.setCodeSuivi(rs.getString("code_suivi"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        
        try {
            order.setBuyerNom(rs.getString("buyer_nom"));
        } catch (SQLException e) {}
        try {
            order.setBuyerEmail(rs.getString("buyer_email"));
        } catch (SQLException e) {}
        try {
            order.setNomBoutique(rs.getString("nom_boutique"));
        } catch (SQLException e) {}
        
        return order;
    }
}
