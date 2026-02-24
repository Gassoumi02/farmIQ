package com.farmiq.services;

import com.farmiq.models.Listing;
import com.farmiq.models.ListingImage;
import com.farmiq.models.enums.ListingStatus;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les annonces du marketplace
 */
public class ListingService {
    
    private static final Logger logger = LogManager.getLogger(ListingService.class);
    
    /**
     * Crée une nouvelle annonce
     */
    public Listing createListing(Listing listing) throws SQLException {
        String sql = """
            INSERT INTO listings (seller_id, titre, description, categorie, prix, unite,
                                quantite_disponible, quantite_minimum, wilaya, statut, date_expiration)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, listing.getSellerId());
            stmt.setString(2, listing.getTitre());
            stmt.setString(3, listing.getDescription());
            stmt.setString(4, listing.getCategorie());
            stmt.setDouble(5, listing.getPrix());
            stmt.setString(6, listing.getUnite());
            stmt.setDouble(7, listing.getQuantiteDisponible());
            stmt.setDouble(8, listing.getQuantiteMinimum() > 0 ? listing.getQuantiteMinimum() : 1.0);
            stmt.setString(9, listing.getWilaya());
            stmt.setString(10, listing.getStatut() != null ? listing.getStatut().name() : "BROUILLON");
            
            if (listing.getDateExpiration() != null) {
                stmt.setDate(11, Date.valueOf(listing.getDateExpiration()));
            } else {
                stmt.setNull(11, Types.DATE);
            }
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    listing.setId(rs.getInt(1));
                }
            }
            
            logger.info("Annonce créée: {} (ID: {})", listing.getTitre(), listing.getId());
            return listing;
        }
    }
    
    /**
     * Met à jour une annonce existante
     */
    public boolean updateListing(Listing listing) throws SQLException {
        String sql = """
            UPDATE listings SET 
                titre = ?, description = ?, categorie = ?, prix = ?, unite = ?,
                quantite_disponible = ?, quantite_minimum = ?, wilaya = ?, 
                statut = ?, date_expiration = ?, updated_at = NOW()
            WHERE id = ? AND seller_id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, listing.getTitre());
            stmt.setString(2, listing.getDescription());
            stmt.setString(3, listing.getCategorie());
            stmt.setDouble(4, listing.getPrix());
            stmt.setString(5, listing.getUnite());
            stmt.setDouble(6, listing.getQuantiteDisponible());
            stmt.setDouble(7, listing.getQuantiteMinimum());
            stmt.setString(8, listing.getWilaya());
            stmt.setString(9, listing.getStatut().name());
            
            if (listing.getDateExpiration() != null) {
                stmt.setDate(10, Date.valueOf(listing.getDateExpiration()));
            } else {
                stmt.setNull(10, Types.DATE);
            }
            
            stmt.setInt(11, listing.getId());
            stmt.setInt(12, listing.getSellerId());
            
            int rowsUpdated = stmt.executeUpdate();
            logger.info("Annonce mise à jour: {} ({} lignes)", listing.getTitre(), rowsUpdated);
            return rowsUpdated > 0;
        }
    }
    
    /**
     * Supprime une annonce
     */
    public boolean deleteListing(int listingId, int sellerId) throws SQLException {
        String sql = "DELETE FROM listings WHERE id = ? AND seller_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, listingId);
            stmt.setInt(2, sellerId);
            
            int rowsDeleted = stmt.executeUpdate();
            logger.info("Annonce supprimée: id={}", listingId);
            return rowsDeleted > 0;
        }
    }
    
    /**
     * Récupère une annonce par son ID
     */
    public Listing getListingById(int id) throws SQLException {
        String sql = "SELECT * FROM listings WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToListing(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère les annonces d'un vendeur
     */
    public List<Listing> getListingsBySeller(int sellerId) throws SQLException {
        String sql = """
            SELECT * FROM listings 
            WHERE seller_id = ? 
            ORDER BY created_at DESC
            """;
        List<Listing> listings = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sellerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    listings.add(mapResultSetToListing(rs));
                }
            }
        }
        return listings;
    }
    
    /**
     * Recherche les annonces avec filtres
     */
    public List<Listing> searchListings(String searchTerm, String categorie, String wilaya,
                                         BigDecimal prixMin, BigDecimal prixMax, 
                                         ListingStatus statut, int page, int pageSize) throws SQLException {
        
        StringBuilder sql = new StringBuilder("SELECT * FROM listings WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (titre LIKE ? OR description LIKE ?)");
            String searchPattern = "%" + searchTerm + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (categorie != null && !categorie.isEmpty()) {
            sql.append(" AND categorie = ?");
            params.add(categorie);
        }
        
        if (wilaya != null && !wilaya.isEmpty()) {
            sql.append(" AND wilaya = ?");
            params.add(wilaya);
        }
        
        if (prixMin != null) {
            sql.append(" AND prix >= ?");
            params.add(prixMin);
        }
        
        if (prixMax != null) {
            sql.append(" AND prix <= ?");
            params.add(prixMax);
        }
        
        if (statut != null) {
            sql.append(" AND statut = ?");
            params.add(statut.name());
        } else {
            sql.append(" AND statut = 'ACTIF'");
        }
        
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        List<Listing> listings = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    listings.add(mapResultSetToListing(rs));
                }
            }
        }
        
        return listings;
    }
    
    /**
     * Compte les annonces avec filtres (sans pagination)
     */
    public int countListings(String searchTerm, String categorie, String wilaya,
                             Double prixMin, Double prixMax, Integer sellerId) throws SQLException {
        
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM listings WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (titre LIKE ? OR description LIKE ?)");
            String searchPattern = "%" + searchTerm + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (categorie != null && !categorie.isEmpty()) {
            sql.append(" AND categorie = ?");
            params.add(categorie);
        }
        
        if (wilaya != null && !wilaya.isEmpty()) {
            sql.append(" AND wilaya = ?");
            params.add(wilaya);
        }
        
        if (prixMin != null) {
            sql.append(" AND prix >= ?");
            params.add(prixMin);
        }
        
        if (prixMax != null) {
            sql.append(" AND prix <= ?");
            params.add(prixMax);
        }
        
        if (sellerId != null) {
            sql.append(" AND seller_id = ?");
            params.add(sellerId);
        }
        
        // Only count ACTIF listings
        sql.append(" AND statut = 'ACTIF'");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * Met à jour le statut d'une annonce
     */
    public boolean updateStatut(int listingId, int sellerId, ListingStatus newStatut) throws SQLException {
        String sql = "UPDATE listings SET statut = ?, updated_at = NOW() WHERE id = ? AND seller_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatut.name());
            stmt.setInt(2, listingId);
            stmt.setInt(3, sellerId);
            
            int rowsUpdated = stmt.executeUpdate();
            logger.info("Statut de l'annonce {} mis à jour: {}", listingId, newStatut);
            return rowsUpdated > 0;
        }
    }
    
    /**
     * Incrémente le compteur de vues
     */
    public void incrementVues(int listingId) throws SQLException {
        String sql = "UPDATE listings SET vues = vues + 1 WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listingId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Ajoute une image à une annonce
     */
    public ListingImage addImage(int listingId, String imageUrl, boolean isPrincipale) throws SQLException {
        String sql = "INSERT INTO listing_images (listing_id, image_url, is_principale, ordre) VALUES (?, ?, ?, ?)";
        
        if (isPrincipale) {
            String updateSql = "UPDATE listing_images SET is_principale = FALSE WHERE listing_id = ? AND is_principale = TRUE";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, listingId);
                updateStmt.executeUpdate();
            }
        }
        
        int maxOrdre = 0;
        String countSql = "SELECT MAX(ordre) FROM listing_images WHERE listing_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            countStmt.setInt(1, listingId);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next() && rs.getObject(1) != null) {
                    maxOrdre = rs.getInt(1);
                }
            }
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, listingId);
            stmt.setString(2, imageUrl);
            stmt.setBoolean(3, isPrincipale);
            stmt.setInt(4, maxOrdre + 1);
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ListingImage image = new ListingImage();
                    image.setId(rs.getInt(1));
                    image.setListingId(listingId);
                    image.setImageUrl(imageUrl);
                    image.setPrincipale(isPrincipale);
                    image.setOrdre(maxOrdre + 1);
                    return image;
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère les images d'une annonce
     */
    public List<ListingImage> getImagesByListing(int listingId) throws SQLException {
        String sql = "SELECT * FROM listing_images WHERE listing_id = ? ORDER BY ordre ASC";
        List<ListingImage> images = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, listingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ListingImage image = new ListingImage();
                    image.setId(rs.getInt("id"));
                    image.setListingId(rs.getInt("listing_id"));
                    image.setImageUrl(rs.getString("image_url"));
                    image.setPrincipale(rs.getBoolean("is_principale"));
                    image.setOrdre(rs.getInt("ordre"));
                    images.add(image);
                }
            }
        }
        return images;
    }
    
    /**
     * Map ResultSet to Listing object
     */
    private Listing mapResultSetToListing(ResultSet rs) throws SQLException {
        Listing listing = new Listing();
        listing.setId(rs.getInt("id"));
        listing.setSellerId(rs.getInt("seller_id"));
        listing.setTitre(rs.getString("titre"));
        listing.setDescription(rs.getString("description"));
        listing.setCategorie(rs.getString("categorie"));
        listing.setPrix(rs.getDouble("prix"));
        listing.setUnite(rs.getString("unite"));
        listing.setQuantiteDisponible(rs.getDouble("quantite_disponible"));
        
        double qteMin = rs.getDouble("quantite_minimum");
        listing.setQuantiteMinimum(qteMin > 0 ? qteMin : 1.0);
        
        listing.setWilaya(rs.getString("wilaya"));
        listing.setStatut(ListingStatus.valueOf(rs.getString("statut")));
        listing.setVues(rs.getInt("vues"));
        
        Date dateExp = rs.getDate("date_expiration");
        if (dateExp != null) {
            listing.setDateExpiration(dateExp.toLocalDate());
        }
        
        listing.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        listing.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return listing;
    }
}
