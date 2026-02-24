package com.farmiq.services;

import com.farmiq.models.SellerProfile;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SellerService {
    private static final Logger logger = LogManager.getLogger(SellerService.class);

    public SellerProfile getSellerProfileByUserId(int userId) {
        String sql = "SELECT * FROM seller_profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToSellerProfile(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération profil vendeur", e);
        }
        return null;
    }

    public SellerProfile getSellerProfileById(int id) {
        String sql = "SELECT * FROM seller_profiles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToSellerProfile(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération profil vendeur", e);
        }
        return null;
    }

    public boolean createSellerProfile(SellerProfile profile) {
        String sql = "INSERT INTO seller_profiles (user_id, nom_boutique, description, wilaya, adresse, telephone, statut) VALUES (?, ?, ?, ?, ?, ?, 'EN_ATTENTE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getNomBoutique());
            stmt.setString(3, profile.getDescription());
            stmt.setString(4, profile.getWilaya());
            stmt.setString(5, profile.getAdresse());
            stmt.setString(6, profile.getTelephone());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    profile.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur création profil vendeur", e);
        }
        return false;
    }

    public boolean updateSellerProfile(SellerProfile profile) {
        String sql = "UPDATE seller_profiles SET nom_boutique = ?, description = ?, wilaya = ?, adresse = ?, telephone = ?, logo_url = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, profile.getNomBoutique());
            stmt.setString(2, profile.getDescription());
            stmt.setString(3, profile.getWilaya());
            stmt.setString(4, profile.getAdresse());
            stmt.setString(5, profile.getTelephone());
            stmt.setString(6, profile.getLogoUrl());
            stmt.setInt(7, profile.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour profil vendeur", e);
        }
        return false;
    }

    public boolean updateStatut(int sellerId, String statut) {
        String sql = "UPDATE seller_profiles SET statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            stmt.setInt(2, sellerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour statut", e);
        }
        return false;
    }

    public List<SellerProfile> getAllSellers() {
        List<SellerProfile> sellers = new ArrayList<>();
        String sql = "SELECT sp.*, u.nom as seller_nom, u.email as seller_email FROM seller_profiles sp JOIN users u ON sp.user_id = u.id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SellerProfile sp = mapResultSetToSellerProfile(rs);
                sp.setSellerNom(rs.getString("seller_nom"));
                sp.setSellerEmail(rs.getString("seller_email"));
                sellers.add(sp);
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération vendeurs", e);
        }
        return sellers;
    }

    public List<SellerProfile> getApprovedSellers() {
        List<SellerProfile> sellers = new ArrayList<>();
        String sql = "SELECT * FROM seller_profiles WHERE statut = 'APPROUVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sellers.add(mapResultSetToSellerProfile(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération vendeurs approuvés", e);
        }
        return sellers;
    }

    private SellerProfile mapResultSetToSellerProfile(ResultSet rs) throws SQLException {
        SellerProfile sp = new SellerProfile();
        sp.setId(rs.getInt("id"));
        sp.setUserId(rs.getInt("user_id"));
        sp.setNomBoutique(rs.getString("nom_boutique"));
        sp.setDescription(rs.getString("description"));
        sp.setWilaya(rs.getString("wilaya"));
        sp.setAdresse(rs.getString("adresse"));
        sp.setTelephone(rs.getString("telephone"));
        sp.setLogoUrl(rs.getString("logo_url"));
        sp.setNoteMoyenne(rs.getDouble("note_moyenne"));
        sp.setTotalVentes(rs.getInt("total_ventes"));
        sp.setStatut(rs.getString("statut"));
        sp.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return sp;
    }
}
