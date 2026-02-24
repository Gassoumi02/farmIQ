package com.farmiq.services;

import com.farmiq.dao.ParcelleDAO;
import com.farmiq.models.Parcelle;
import com.farmiq.exceptions.UserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service pour la gestion des parcelles
 * @author FarmIQ Team
 * @version 1.0
 */
public class ParcelleService {
    private static final Logger logger = LogManager.getLogger(ParcelleService.class);
    private final ParcelleDAO parcelleDAO;

    public ParcelleService() {
        this.parcelleDAO = new ParcelleDAO();
    }

    /**
     * Récupère toutes les parcelles
     */
    public List<Parcelle> getAllParcelles() throws UserException {
        try {
            return parcelleDAO.findAll();
        } catch (SQLException e) {
            logger.error("Erreur chargement parcelles", e);
            throw new UserException("Erreur lors du chargement des parcelles: " + e.getMessage());
        }
    }

    /**
     * Récupère les parcelles d'un utilisateur
     */
    public List<Parcelle> getParcellesByUser(int userId) throws UserException {
        try {
            return parcelleDAO.findByUserId(userId);
        } catch (SQLException e) {
            logger.error("Erreur chargement parcelles utilisateur {}", userId, e);
            throw new UserException("Erreur lors du chargement des parcelles: " + e.getMessage());
        }
    }

    /**
     * Crée une nouvelle parcelle avec validation
     */
    public Parcelle createParcelle(String nomParcelle, double surface, String localisation,
                                   String typeSol, String etatParcelle, LocalDate dateDerniereCulture,
                                   boolean irrigation, String remarques, Integer idPlante, Integer userId)
            throws UserException {

        // Validation
        validateParcelleData(nomParcelle, surface, etatParcelle);

        try {
            Parcelle parcelle = new Parcelle();
            parcelle.setNomParcelle(nomParcelle.trim());
            parcelle.setSurface(surface);
            parcelle.setLocalisation(localisation != null ? localisation.trim() : "");
            parcelle.setTypeSol(typeSol != null ? typeSol.trim() : "");
            parcelle.setEtatParcelle(etatParcelle);
            parcelle.setDateDerniereCulture(dateDerniereCulture);
            parcelle.setIrrigation(irrigation);
            parcelle.setRemarques(remarques);
            parcelle.setIdPlante(idPlante);
            parcelle.setUserId(userId);

            boolean created = parcelleDAO.create(parcelle);
            if (!created) {
                throw new UserException("Impossible de créer la parcelle");
            }

            logger.info("Parcelle créée: {} ({} m²)", nomParcelle, surface);
            return parcelle;

        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL création parcelle", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    /**
     * Met à jour une parcelle existante
     */
    public void updateParcelle(int idParcelle, String nomParcelle, double surface, String localisation,
                               String typeSol, String etatParcelle, LocalDate dateDerniereCulture,
                               boolean irrigation, String remarques, Integer idPlante)
            throws UserException {

        // Validation
        validateParcelleData(nomParcelle, surface, etatParcelle);

        try {
            Parcelle parcelle = new Parcelle();
            parcelle.setIdParcelle(idParcelle);
            parcelle.setNomParcelle(nomParcelle.trim());
            parcelle.setSurface(surface);
            parcelle.setLocalisation(localisation != null ? localisation.trim() : "");
            parcelle.setTypeSol(typeSol != null ? typeSol.trim() : "");
            parcelle.setEtatParcelle(etatParcelle);
            parcelle.setDateDerniereCulture(dateDerniereCulture);
            parcelle.setIrrigation(irrigation);
            parcelle.setRemarques(remarques);
            parcelle.setIdPlante(idPlante);

            boolean updated = parcelleDAO.update(parcelle);
            if (!updated) {
                throw new UserException("Impossible de mettre à jour la parcelle");
            }

            logger.info("Parcelle mise à jour: {} (ID: {})", nomParcelle, idParcelle);

        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL mise à jour parcelle", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    /**
     * Supprime une parcelle
     */
    public void deleteParcelle(int idParcelle) throws UserException {
        try {
            boolean deleted = parcelleDAO.delete(idParcelle);
            if (!deleted) {
                throw new UserException("Impossible de supprimer la parcelle");
            }
            logger.info("Parcelle supprimée ID: {}", idParcelle);
        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL suppression parcelle", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    /**
     * Valide les données d'une parcelle
     */
    private void validateParcelleData(String nomParcelle, double surface, String etatParcelle)
            throws UserException {

        // Validation du nom
        if (nomParcelle == null || nomParcelle.trim().isEmpty()) {
            throw new UserException("Le nom de la parcelle est obligatoire");
        }
        if (nomParcelle.trim().length() < 2) {
            throw new UserException("Le nom doit contenir au moins 2 caractères");
        }

        // Validation de la surface
        if (surface <= 0) {
            throw new UserException("La surface doit être supérieure à 0");
        }

        // Validation de l'état
        if (etatParcelle == null || etatParcelle.trim().isEmpty()) {
            throw new UserException("L'état de la parcelle est obligatoire");
        }

        List<String> etatsValides = List.of("disponible", "en culture", "en repos", "en préparation");
        if (!etatsValides.contains(etatParcelle.toLowerCase())) {
            throw new UserException("État invalide. États valides: " + String.join(", ", etatsValides));
        }
    }
}