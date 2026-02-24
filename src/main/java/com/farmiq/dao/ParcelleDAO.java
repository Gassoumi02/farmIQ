package com.farmiq.dao;

import com.farmiq.models.Parcelle;
import com.farmiq.models.Plante;
import com.farmiq.utils.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des parcelles
 * @author FarmIQ Team
 * @version 1.0
 */
public class ParcelleDAO {
    private static final Logger logger = LogManager.getLogger(ParcelleDAO.class);

    /**
     * Récupère toutes les parcelles
     */
    public List<Parcelle> findAll() throws SQLException {
        List<Parcelle> list = new ArrayList<>();
        String sql = "SELECT * FROM parcelle ORDER BY idParcelle DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractParcelleFromResultSet(rs));
            }
            logger.debug("Chargement de {} parcelles", list.size());
        } catch (SQLException e) {
            logger.error("Erreur chargement parcelles", e);
            throw e;
        }
        return list;
    }

    /**
     * Récupère les parcelles d'un utilisateur
     */
    public List<Parcelle> findByUserId(int userId) throws SQLException {
        List<Parcelle> list = new ArrayList<>();
        String sql = "SELECT * FROM parcelle WHERE user_id = ? ORDER BY idParcelle DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractParcelleFromResultSet(rs));
                }
            }
            logger.debug("Chargement de {} parcelles pour utilisateur {}", list.size(), userId);
        } catch (SQLException e) {
            logger.error("Erreur chargement parcelles utilisateur {}", userId, e);
            throw e;
        }
        return list;
    }

    /**
     * Crée une nouvelle parcelle
     */
    public boolean create(Parcelle parcelle) throws SQLException {
        String sql = """
            INSERT INTO parcelle
            (nomParcelle, surface, localisation, typeSol, etatParcelle,
             dateDerniereCulture, irrigation, remarques, idPlante, user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, parcelle.getNomParcelle());
            ps.setDouble(2, parcelle.getSurface());
            ps.setString(3, parcelle.getLocalisation());
            ps.setString(4, parcelle.getTypeSol());
            ps.setString(5, parcelle.getEtatParcelle());

            if (parcelle.getDateDerniereCulture() != null)
                ps.setDate(6, Date.valueOf(parcelle.getDateDerniereCulture()));
            else
                ps.setNull(6, Types.DATE);

            ps.setBoolean(7, parcelle.isIrrigation());
            ps.setString(8, parcelle.getRemarques());

            if (parcelle.getIdPlante() != null)
                ps.setInt(9, parcelle.getIdPlante());
            else
                ps.setNull(9, Types.INTEGER);

            if (parcelle.getUserId() != null)
                ps.setInt(10, parcelle.getUserId());
            else
                ps.setNull(10, Types.INTEGER);

            int affected = ps.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        parcelle.setIdParcelle(rs.getInt(1));
                    }
                }
                logger.info("Parcelle créée: {} (ID: {})", parcelle.getNomParcelle(), parcelle.getIdParcelle());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur création parcelle", e);
            throw e;
        }
        return false;
    }

    /**
     * Met à jour une parcelle
     */
    public boolean update(Parcelle parcelle) throws SQLException {
        String sql = """
        UPDATE parcelle SET
            nomParcelle = ?,
            surface = ?,
            localisation = ?,
            typeSol = ?,
            etatParcelle = ?,
            dateDerniereCulture = ?,
            irrigation = ?,
            remarques = ?,
            idPlante = ?,
            user_id = ?
        WHERE idParcelle = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, parcelle.getNomParcelle());
            ps.setDouble(2, parcelle.getSurface());
            ps.setString(3, parcelle.getLocalisation());
            ps.setString(4, parcelle.getTypeSol());
            ps.setString(5, parcelle.getEtatParcelle());

            if (parcelle.getDateDerniereCulture() != null) {
                ps.setDate(6, Date.valueOf(parcelle.getDateDerniereCulture()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setBoolean(7, parcelle.isIrrigation());
            ps.setString(8, parcelle.getRemarques());

            if (parcelle.getIdPlante() != null) {
                ps.setInt(9, parcelle.getIdPlante());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            if (parcelle.getUserId() != null) {
                ps.setInt(10, parcelle.getUserId());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            ps.setInt(11, parcelle.getIdParcelle());

            int affected = ps.executeUpdate();
            logger.info("Parcelle mise à jour: {} (ID: {})", parcelle.getNomParcelle(), parcelle.getIdParcelle());
            return affected > 0;

        } catch (SQLException e) {
            logger.error("Erreur mise à jour parcelle ID {}", parcelle.getIdParcelle(), e);
            throw e;
        }
    }

    /**
     * Supprime une parcelle
     */
    public boolean delete(int idParcelle) throws SQLException {
        String sql = "DELETE FROM parcelle WHERE idParcelle=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idParcelle);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                logger.info("Parcelle supprimée ID: {}", idParcelle);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur suppression parcelle ID {}", idParcelle, e);
            throw e;
        }
        return false;
    }

    /**
     * Extrait un objet Parcelle depuis un ResultSet
     */
    private Parcelle extractParcelleFromResultSet(ResultSet rs) throws SQLException {
        Parcelle parcelle = new Parcelle();
        parcelle.setIdParcelle(rs.getInt("idParcelle"));
        parcelle.setNomParcelle(rs.getString("nomParcelle"));
        parcelle.setSurface(rs.getDouble("surface"));
        parcelle.setLocalisation(rs.getString("localisation"));
        parcelle.setTypeSol(rs.getString("typeSol"));
        parcelle.setEtatParcelle(rs.getString("etatParcelle"));

        Date dateDerniereCulture = rs.getDate("dateDerniereCulture");
        if (dateDerniereCulture != null) {
            parcelle.setDateDerniereCulture(dateDerniereCulture.toLocalDate());
        }

        parcelle.setIrrigation(rs.getBoolean("irrigation"));
        parcelle.setRemarques(rs.getString("remarques"));

        // idPlante peut être NULL
        int idPlante = rs.getInt("idPlante");
        if (!rs.wasNull()) {
            parcelle.setIdPlante(idPlante);
        }

        // user_id peut être NULL
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            parcelle.setUserId(userId);
        }

        return parcelle;
    }
}