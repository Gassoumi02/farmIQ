package com.farmiq.services;

import com.farmiq.dao.PlanteDAO;
import com.farmiq.models.Plante;
import com.farmiq.exceptions.UserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service pour la gestion des plantes
 * @author FarmIQ Team
 * @version 1.0
 */
public class PlanteService {
    private static final Logger logger = LogManager.getLogger(PlanteService.class);
    private final PlanteDAO planteDAO;

    public PlanteService() {
        this.planteDAO = new PlanteDAO();
    }

    /**
     * Récupère toutes les plantes
     * @return Liste de toutes les plantes
     * @throws UserException en cas d'erreur
     */
    public List<Plante> getAllPlantes() throws UserException {
        try {
            return planteDAO.findAll();
        } catch (SQLException e) {
            logger.error("Erreur chargement plantes", e);
            throw new UserException("Erreur lors du chargement des plantes: " + e.getMessage());
        }
    }

    /**
     * Récupère les plantes d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des plantes de l'utilisateur
     * @throws UserException en cas d'erreur
     */
    public List<Plante> getPlantesByUser(int userId) throws UserException {
        try {
            return planteDAO.findByUserId(userId);
        } catch (SQLException e) {
            logger.error("Erreur chargement plantes utilisateur {}", userId, e);
            throw new UserException("Erreur lors du chargement des plantes: " + e.getMessage());
        }
    }

    /**
     * Récupère une plante par son ID
     * @param id ID de la plante
     * @return La plante ou null
     * @throws UserException en cas d'erreur
     */
    public Plante getPlanteById(int id) throws UserException {
        try {
            return planteDAO.findById(id);
        } catch (SQLException e) {
            logger.error("Erreur chargement plante ID {}", id, e);
            throw new UserException("Erreur lors du chargement de la plante: " + e.getMessage());
        }
    }

    /**
     * Crée une nouvelle plante avec validation
     * @param nom Nom de la plante
     * @param type Type de plante
     * @param datePlantation Date de plantation
     * @param dateRecoltePrevue Date de récolte prévue
     * @param quantite Quantité
     * @param etat État de la plante
     * @param userId ID de l'utilisateur propriétaire (peut être null)
     * @return La plante créée
     * @throws UserException en cas d'erreur de validation ou d'insertion
     */
    public Plante createPlante(String nom, String type, LocalDate datePlantation,
                               LocalDate dateRecoltePrevue, int quantite, String etat,
                               Integer userId) throws UserException {

        // Validation
        validatePlanteData(nom, type, datePlantation, dateRecoltePrevue, quantite, etat);

        try {
            Plante plante = new Plante();
            plante.setNom(nom.trim());
            plante.setType(type.trim());
            plante.setDatePlantation(datePlantation);
            plante.setDateRecoltePrevue(dateRecoltePrevue);
            plante.setQuantite(quantite);
            plante.setEtat(etat);
            plante.setUserId(userId);

            boolean created = planteDAO.create(plante);
            if (!created) {
                throw new UserException("Impossible de créer la plante");
            }

            logger.info("Plante créée: {} ({})", nom, type);
            return plante;

        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL création plante", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    /**
     * Met à jour une plante existante
     * @param id ID de la plante
     * @param nom Nom de la plante
     * @param type Type de plante
     * @param datePlantation Date de plantation
     * @param dateRecoltePrevue Date de récolte prévue
     * @param quantite Quantité
     * @param etat État de la plante
     * @throws UserException en cas d'erreur
     */
    public void updatePlante(int id, String nom, String type, LocalDate datePlantation,
                             LocalDate dateRecoltePrevue, int quantite, String etat)
            throws UserException {

        // Validation
        validatePlanteData(nom, type, datePlantation, dateRecoltePrevue, quantite, etat);

        try {
            Plante plante = planteDAO.findById(id);
            if (plante == null) {
                throw new UserException("Plante introuvable");
            }

            plante.setNom(nom.trim());
            plante.setType(type.trim());
            plante.setDatePlantation(datePlantation);
            plante.setDateRecoltePrevue(dateRecoltePrevue);
            plante.setQuantite(quantite);
            plante.setEtat(etat);

            boolean updated = planteDAO.update(plante);
            if (!updated) {
                throw new UserException("Impossible de mettre à jour la plante");
            }

            logger.info("Plante mise à jour: {} (ID: {})", nom, id);

        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL mise à jour plante", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    /**
     * Supprime une plante
     * @param id ID de la plante
     * @throws UserException en cas d'erreur
     */
    public void deletePlante(int id) throws UserException {
        try {
            boolean deleted = planteDAO.delete(id);
            if (!deleted) {
                throw new UserException("Impossible de supprimer la plante");
            }
            logger.info("Plante supprimée ID: {}", id);
        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL suppression plante", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    /**
     * Compte le nombre total de plantes
     * @return Nombre de plantes
     * @throws UserException en cas d'erreur
     */
    public int countPlantes() throws UserException {
        try {
            return planteDAO.count();
        } catch (SQLException e) {
            throw new UserException("Erreur comptage plantes: " + e.getMessage());
        }
    }

    /**
     * Compte les plantes par état
     * @param etat État recherché
     * @return Nombre de plantes
     * @throws UserException en cas d'erreur
     */
    public int countByEtat(String etat) throws UserException {
        try {
            return planteDAO.countByEtat(etat);
        } catch (SQLException e) {
            throw new UserException("Erreur comptage par état: " + e.getMessage());
        }
    }

    /**
     * Valide les données d'une plante
     * @throws UserException si validation échoue
     */
    private void validatePlanteData(String nom, String type, LocalDate datePlantation,
                                    LocalDate dateRecoltePrevue, int quantite, String etat)
            throws UserException {

        // Validation du nom
        if (nom == null || nom.trim().isEmpty()) {
            throw new UserException("Le nom de la plante est obligatoire");
        }
        if (nom.trim().length() < 2) {
            throw new UserException("Le nom doit contenir au moins 2 caractères");
        }

        // Validation du type
        if (type == null || type.trim().isEmpty()) {
            throw new UserException("Le type de plante est obligatoire");
        }
        if (type.trim().length() < 2) {
            throw new UserException("Le type doit contenir au moins 2 caractères");
        }

        // Validation des dates
        if (datePlantation == null) {
            throw new UserException("La date de plantation est obligatoire");
        }
        if (dateRecoltePrevue == null) {
            throw new UserException("La date de récolte prévue est obligatoire");
        }
        if (dateRecoltePrevue.isBefore(datePlantation)) {
            throw new UserException("La date de récolte doit être après la date de plantation");
        }

        // Validation de la quantité
        if (quantite <= 0) {
            throw new UserException("La quantité doit être supérieure à 0");
        }

        // Validation de l'état
        if (etat == null || etat.trim().isEmpty()) {
            throw new UserException("L'état de la plante est obligatoire");
        }

        List<String> etatsValides = List.of("semée", "en croissance", "prête à récolter", "récoltée");
        if (!etatsValides.contains(etat.toLowerCase())) {
            throw new UserException("État invalide. États valides: " + String.join(", ", etatsValides));
        }
    }
}