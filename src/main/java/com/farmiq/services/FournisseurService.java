package com.farmiq.services;

import com.farmiq.models.Fournisseur;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurService {
    private static final Logger logger = LogManager.getLogger(FournisseurService.class);

    public List<Fournisseur> getAllFournisseurs() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs ORDER BY nom";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                fournisseurs.add(mapResultSetToFournisseur(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération fournisseurs", e);
        }
        return fournisseurs;
    }

    public List<Fournisseur> getActiveFournisseurs() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs WHERE statut = 'ACTIF' ORDER BY nom";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                fournisseurs.add(mapResultSetToFournisseur(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération fournisseurs actifs", e);
        }
        return fournisseurs;
    }

    public Fournisseur getFournisseurById(int id) {
        String sql = "SELECT * FROM fournisseurs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFournisseur(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur récupération fournisseur", e);
        }
        return null;
    }

    public boolean createFournisseur(Fournisseur fournisseur) {
        String sql = "INSERT INTO fournisseurs (nom, contact, telephone, email, adresse, statut) VALUES (?, ?, ?, ?, ?, 'ACTIF')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fournisseur.getNom());
            stmt.setString(2, fournisseur.getContact());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());
            stmt.setString(5, fournisseur.getAdresse());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        fournisseur.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur création fournisseur", e);
        }
        return false;
    }

    public boolean updateFournisseur(Fournisseur fournisseur) {
        String sql = "UPDATE fournisseurs SET nom = ?, contact = ?, telephone = ?, email = ?, adresse = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fournisseur.getNom());
            stmt.setString(2, fournisseur.getContact());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());
            stmt.setString(5, fournisseur.getAdresse());
            stmt.setInt(6, fournisseur.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour fournisseur", e);
        }
        return false;
    }

    public boolean toggleStatut(int id) {
        String sql = "UPDATE fournisseurs SET statut = CASE WHEN statut = 'ACTIF' THEN 'INACTIF' ELSE 'ACTIF' END WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur changement statut fournisseur", e);
        }
        return false;
    }

    public boolean deleteFournisseur(int id) {
        String sql = "DELETE FROM fournisseurs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur suppression fournisseur", e);
        }
        return false;
    }

    public List<Fournisseur> searchFournisseurs(String keyword) {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs WHERE nom LIKE ? OR contact LIKE ? OR email LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String search = "%" + keyword + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fournisseurs.add(mapResultSetToFournisseur(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur recherche fournisseurs", e);
        }
        return fournisseurs;
    }

    private Fournisseur mapResultSetToFournisseur(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setId(rs.getInt("id"));
        f.setNom(rs.getString("nom"));
        f.setContact(rs.getString("contact"));
        f.setTelephone(rs.getString("telephone"));
        f.setEmail(rs.getString("email"));
        f.setAdresse(rs.getString("adresse"));
        f.setStatut(rs.getString("statut"));
        f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return f;
    }
}
