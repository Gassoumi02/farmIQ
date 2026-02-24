package com.farmiq.services;

import com.farmiq.models.Product;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private static final Logger logger = LogManager.getLogger(ProductService.class);

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, f.nom as fournisseur_nom FROM products p LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id ORDER BY p.nom";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                p.setFournisseurNom(rs.getString("fournisseur_nom"));
                products.add(p);
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération produits", e);
        }
        return products;
    }

    public Product getProductById(int id) {
        String sql = "SELECT p.*, f.nom as fournisseur_nom FROM products p LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                p.setFournisseurNom(rs.getString("fournisseur_nom"));
                return p;
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération produit", e);
        }
        return null;
    }

    public boolean createProduct(Product product) {
        String sql = "INSERT INTO products (nom, categorie, quantite, unite, prix_unitaire, seuil_alerte, fournisseur_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getNom());
            stmt.setString(2, product.getCategorie());
            stmt.setDouble(3, product.getQuantite());
            stmt.setString(4, product.getUnite());
            stmt.setDouble(5, product.getPrixUnitaire());
            stmt.setDouble(6, product.getSeuilAlerte());
            if (product.getFournisseurId() != null) {
                stmt.setInt(7, product.getFournisseurId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur création produit", e);
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET nom = ?, categorie = ?, quantite = ?, unite = ?, prix_unitaire = ?, seuil_alerte = ?, fournisseur_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getNom());
            stmt.setString(2, product.getCategorie());
            stmt.setDouble(3, product.getQuantite());
            stmt.setString(4, product.getUnite());
            stmt.setDouble(5, product.getPrixUnitaire());
            stmt.setDouble(6, product.getSeuilAlerte());
            if (product.getFournisseurId() != null) {
                stmt.setInt(7, product.getFournisseurId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setInt(8, product.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour produit", e);
        }
        return false;
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur suppression produit", e);
        }
        return false;
    }

    public List<Product> getProductsByCategory(String categorie) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, f.nom as fournisseur_nom FROM products p LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id WHERE p.categorie = ? ORDER BY p.nom";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categorie);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                p.setFournisseurNom(rs.getString("fournisseur_nom"));
                products.add(p);
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération produits par catégorie", e);
        }
        return products;
    }

    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, f.nom as fournisseur_nom FROM products p LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id WHERE p.quantite <= p.seuil_alerte ORDER BY p.quantite";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                p.setFournisseurNom(rs.getString("fournisseur_nom"));
                products.add(p);
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération produits en rupture", e);
        }
        return products;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, f.nom as fournisseur_nom FROM products p LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id WHERE p.nom LIKE ? ORDER BY p.nom";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                p.setFournisseurNom(rs.getString("fournisseur_nom"));
                products.add(p);
            }
        } catch (SQLException e) {
            logger.error("Erreur recherche produits", e);
        }
        return products;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setCategorie(rs.getString("categorie"));
        p.setQuantite(rs.getDouble("quantite"));
        p.setUnite(rs.getString("unite"));
        p.setPrixUnitaire(rs.getDouble("prix_unitaire"));
        p.setSeuilAlerte(rs.getDouble("seuil_alerte"));
        int fournisseurId = rs.getInt("fournisseur_id");
        if (!rs.wasNull()) {
            p.setFournisseurId(fournisseurId);
        }
        p.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        p.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return p;
    }
}
