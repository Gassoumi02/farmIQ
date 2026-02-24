package com.farmiq.services;

import com.farmiq.models.Tache;
import com.farmiq.models.User;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les tâches du calendrier agricole
 */
public class TacheService {
    
    private static final Logger logger = LogManager.getLogger(TacheService.class);
    
    /**
     * Crée une nouvelle tâche
     */
    public Tache createTache(Tache tache) throws SQLException {
        String sql = """
            INSERT INTO taches (user_id, titre, description, date_debut, date_fin, 
                              priorite, statut, type)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, tache.getUserId());
            stmt.setString(2, tache.getTitre());
            stmt.setString(3, tache.getDescription());
            stmt.setDate(4, Date.valueOf(tache.getDateDebut()));
            
            if (tache.getDateFin() != null) {
                stmt.setDate(5, Date.valueOf(tache.getDateFin()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            stmt.setString(6, tache.getPriorite() != null ? tache.getPriorite() : "MOYENNE");
            stmt.setString(7, tache.getStatut() != null ? tache.getStatut() : "A_FAIRE");
            stmt.setString(8, tache.getType() != null ? tache.getType() : "AUTRE");
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    tache.setId(rs.getInt(1));
                }
            }
            
            logger.info("Tâche créée: {}", tache.getTitre());
            return tache;
        }
    }
    
    /**
     * Met à jour une tâche existante
     */
    public boolean updateTache(Tache tache) throws SQLException {
        String sql = """
            UPDATE taches SET 
                titre = ?, description = ?, date_debut = ?, date_fin = ?,
                priorite = ?, statut = ?, type = ?
            WHERE id = ? AND user_id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tache.getTitre());
            stmt.setString(2, tache.getDescription());
            stmt.setDate(3, Date.valueOf(tache.getDateDebut()));
            
            if (tache.getDateFin() != null) {
                stmt.setDate(4, Date.valueOf(tache.getDateFin()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            
            stmt.setString(5, tache.getPriorite() != null ? tache.getPriorite() : "MOYENNE");
            stmt.setString(6, tache.getStatut() != null ? tache.getStatut() : "A_FAIRE");
            stmt.setString(7, tache.getType() != null ? tache.getType() : "AUTRE");
            stmt.setInt(8, tache.getId());
            stmt.setInt(9, tache.getUserId());
            
            int rowsUpdated = stmt.executeUpdate();
            logger.info("Tâche mise à jour: {} ({} lignes)", tache.getTitre(), rowsUpdated);
            return rowsUpdated > 0;
        }
    }
    
    /**
     * Supprime une tâche
     */
    public boolean deleteTache(int tacheId, int userId) throws SQLException {
        String sql = "DELETE FROM taches WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tacheId);
            stmt.setInt(2, userId);
            
            int rowsDeleted = stmt.executeUpdate();
            logger.info("Tâche supprimée: id={}", tacheId);
            return rowsDeleted > 0;
        }
    }
    
    /**
     * Récupère une tâche par son ID
     */
    public Tache getTacheById(int id) throws SQLException {
        String sql = "SELECT * FROM taches WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTache(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère toutes les tâches d'un utilisateur
     */
    public List<Tache> getTachesByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM taches WHERE user_id = ? ORDER BY date_debut ASC";
        List<Tache> taches = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taches.add(mapResultSetToTache(rs));
                }
            }
        }
        return taches;
    }
    
    /**
     * Récupère les tâches pour une date spécifique
     */
    public List<Tache> getTachesByDate(int userId, LocalDate date) throws SQLException {
        String sql = """
            SELECT * FROM taches 
            WHERE user_id = ? AND date_debut <= ? 
            AND (date_fin IS NULL OR date_fin >= ?)
            ORDER BY priorite DESC, date_debut ASC
            """;
        List<Tache> taches = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setDate(3, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taches.add(mapResultSetToTache(rs));
                }
            }
        }
        return taches;
    }
    
    /**
     * Récupère les tâches pour un mois spécifique
     */
    public List<Tache> getTachesByMonth(int userId, int year, int month) throws SQLException {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        String sql = """
            SELECT * FROM taches 
            WHERE user_id = ? 
            AND date_debut <= ? 
            AND (date_fin IS NULL OR date_fin >= ?)
            ORDER BY date_debut ASC, priorite DESC
            """;
        List<Tache> taches = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(endDate));
            stmt.setDate(3, Date.valueOf(startDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taches.add(mapResultSetToTache(rs));
                }
            }
        }
        return taches;
    }
    
    /**
     * Met à jour le statut d'une tâche
     */
    public boolean updateStatut(int tacheId, int userId, String newStatut) throws SQLException {
        String sql = "UPDATE taches SET statut = ? WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatut);
            stmt.setInt(2, tacheId);
            stmt.setInt(3, userId);
            
            int rowsUpdated = stmt.executeUpdate();
            logger.info("Statut de la tâche {} mis à jour: {}", tacheId, newStatut);
            return rowsUpdated > 0;
        }
    }
    
    /**
     * Récupère les tâches en retard
     */
    public List<Tache> getTachesEnRetard(int userId) throws SQLException {
        String sql = """
            SELECT * FROM taches 
            WHERE user_id = ? 
            AND date_fin < CURDATE() 
            AND statut NOT IN ('TERMINE', 'ANNULE')
            ORDER BY date_fin ASC
            """;
        List<Tache> taches = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taches.add(mapResultSetToTache(rs));
                }
            }
        }
        return taches;
    }
    
    /**
     * Compte les tâches par statut pour un utilisateur
     */
    public java.util.Map<String, Long> getTacheStatsByUser(int userId) throws SQLException {
        String sql = """
            SELECT statut, COUNT(*) as count 
            FROM taches 
            WHERE user_id = ?
            GROUP BY statut
            """;
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("statut"), rs.getLong("count"));
                }
            }
        }
        return stats;
    }
    
    /**
     * Map ResultSet to Tache object
     */
    private Tache mapResultSetToTache(ResultSet rs) throws SQLException {
        Tache tache = new Tache();
        tache.setId(rs.getInt("id"));
        tache.setUserId(rs.getInt("user_id"));
        tache.setTitre(rs.getString("titre"));
        tache.setDescription(rs.getString("description"));
        tache.setDateDebut(rs.getDate("date_debut").toLocalDate());
        
        Date dateFin = rs.getDate("date_fin");
        if (dateFin != null) {
            tache.setDateFin(dateFin.toLocalDate());
        }
        
        tache.setPriorite(rs.getString("priorite"));
        tache.setStatut(rs.getString("statut"));
        tache.setType(rs.getString("type"));
        
        return tache;
    }
}
