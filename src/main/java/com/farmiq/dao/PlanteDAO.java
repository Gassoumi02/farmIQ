package com.farmiq.dao;

import com.farmiq.models.Plante;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des plantes
 * @author FarmIQ Team
 * @version 1.0
 */
public class PlanteDAO {
    private static final Logger logger = LogManager.getLogger(PlanteDAO.class);

    /**
     * Récupère toutes les plantes
     * @return Liste de toutes les plantes
     * @throws SQLException en cas d'erreur
     */
    public List<Plante> findAll() throws SQLException {
        List<Plante> list = new ArrayList<>();
        String sql = "SELECT * FROM plante ORDER BY datePlantation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractPlanteFromResultSet(rs));
            }
            logger.debug("Chargement de {} plantes", list.size());
        } catch (SQLException e) {
            logger.error("Erreur chargement plantes", e);
            throw e;
        }
        return list;
    }

    /**
     * Récupère les plantes d'un utilisateur spécifique
     * @param userId ID de l'utilisateur
     * @return Liste des plantes de l'utilisateur
     * @throws SQLException en cas d'erreur
     */
    public List<Plante> findByUserId(int userId) throws SQLException {
        List<Plante> list = new ArrayList<>();
        String sql = "SELECT * FROM plante WHERE user_id = ? ORDER BY datePlantation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractPlanteFromResultSet(rs));
                }
            }
            logger.debug("Chargement de {} plantes pour utilisateur {}", list.size(), userId);
        } catch (SQLException e) {
            logger.error("Erreur chargement plantes utilisateur {}", userId, e);
            throw e;
        }
        return list;
    }

    /**
     * Récupère une plante par son ID
     * @param id ID de la plante
     * @return La plante ou null si non trouvée
     * @throws SQLException en cas d'erreur
     */
    public Plante findById(int id) throws SQLException {
        String sql = "SELECT * FROM plante WHERE idPlante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractPlanteFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur chargement plante ID {}", id, e);
            throw e;
        }
        return null;
    }

    /**
     * Crée une nouvelle plante
     * @param plante La plante à créer
     * @return true si création réussie
     * @throws SQLException en cas d'erreur
     */
    public boolean create(Plante plante) throws SQLException {
        String sql = "INSERT INTO plante(nom, type, datePlantation, dateRecoltePrevue, quantite, etat, user_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, plante.getNom());
            ps.setString(2, plante.getType());
            ps.setDate(3, Date.valueOf(plante.getDatePlantation()));
            ps.setDate(4, Date.valueOf(plante.getDateRecoltePrevue()));
            ps.setInt(5, plante.getQuantite());
            ps.setString(6, plante.getEtat());

            if (plante.getUserId() != null) {
                ps.setInt(7, plante.getUserId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            int affected = ps.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        plante.setIdPlante(rs.getInt(1));
                    }
                }
                logger.info("Plante créée: {} (ID: {})", plante.getNom(), plante.getIdPlante());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur création plante", e);
            throw e;
        }
        return false;
    }

    /**
     * Met à jour une plante existante
     * @param plante La plante à mettre à jour
     * @return true si mise à jour réussie
     * @throws SQLException en cas d'erreur
     */
    public boolean update(Plante plante) throws SQLException {
        String sql = "UPDATE plante SET nom=?, type=?, datePlantation=?, dateRecoltePrevue=?, " +
                "quantite=?, etat=?, user_id=? WHERE idPlante=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plante.getNom());
            ps.setString(2, plante.getType());
            ps.setDate(3, Date.valueOf(plante.getDatePlantation()));
            ps.setDate(4, Date.valueOf(plante.getDateRecoltePrevue()));
            ps.setInt(5, plante.getQuantite());
            ps.setString(6, plante.getEtat());

            if (plante.getUserId() != null) {
                ps.setInt(7, plante.getUserId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            ps.setInt(8, plante.getIdPlante());

            int affected = ps.executeUpdate();
            logger.info("Plante mise à jour: {} (ID: {})", plante.getNom(), plante.getIdPlante());
            return affected > 0;

        } catch (SQLException e) {
            logger.error("Erreur mise à jour plante ID {}", plante.getIdPlante(), e);
            throw e;
        }
    }

    /**
     * Supprime une plante
     * @param id ID de la plante à supprimer
     * @return true si suppression réussie
     * @throws SQLException en cas d'erreur
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM plante WHERE idPlante=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                logger.info("Plante supprimée ID: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur suppression plante ID {}", id, e);
            throw e;
        }
        return false;
    }

    /**
     * Compte le nombre total de plantes
     * @return Nombre de plantes
     * @throws SQLException en cas d'erreur
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM plante";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur comptage plantes", e);
            throw e;
        }
        return 0;
    }

    /**
     * Compte les plantes par état
     * @param etat État recherché
     * @return Nombre de plantes dans cet état
     * @throws SQLException en cas d'erreur
     */
    public int countByEtat(String etat) throws SQLException {
        String sql = "SELECT COUNT(*) FROM plante WHERE etat = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, etat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur comptage plantes par état {}", etat, e);
            throw e;
        }
        return 0;
    }

    /**
     * Extrait un objet Plante depuis un ResultSet
     * @param rs ResultSet positionné sur une ligne
     * @return Objet Plante
     * @throws SQLException en cas d'erreur
     */
    private Plante extractPlanteFromResultSet(ResultSet rs) throws SQLException {
        Plante plante = new Plante();
        plante.setIdPlante(rs.getInt("idPlante"));
        plante.setNom(rs.getString("nom"));
        plante.setType(rs.getString("type"));
        plante.setDatePlantation(rs.getDate("datePlantation").toLocalDate());
        plante.setDateRecoltePrevue(rs.getDate("dateRecoltePrevue").toLocalDate());
        plante.setQuantite(rs.getInt("quantite"));
        plante.setEtat(rs.getString("etat"));

        // user_id peut être NULL
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            plante.setUserId(userId);
        }

        return plante;
    }
}