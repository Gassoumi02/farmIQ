package com.farmiq.services;

import com.farmiq.dao.UserDAO;
import com.farmiq.dao.RoleDAO;
import com.farmiq.models.User;
import com.farmiq.models.Role;
import com.farmiq.utils.PasswordUtil;
import com.farmiq.utils.ValidationUtil;
import com.farmiq.exceptions.UserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.roleDAO = new RoleDAO();
    }

    public List<User> getAllUsers() throws UserException {
        try {
            return userDAO.findAll();
        } catch (SQLException e) {
            logger.error("Erreur chargement utilisateurs", e);
            throw new UserException("Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }

    public User getUserById(int id) throws UserException {
        try {
            return userDAO.findById(id);
        } catch (SQLException e) {
            logger.error("Erreur chargement utilisateur ID: {}", id, e);
            throw new UserException("Erreur lors du chargement de l'utilisateur: " + e.getMessage());
        }
    }

    public User createUser(String nom, String email, String password, int roleId, String statut) throws UserException {
        String nomErr = ValidationUtil.validateNom(nom);
        if (nomErr != null) throw new UserException(nomErr);

        String emailErr = ValidationUtil.validateEmail(email);
        if (emailErr != null) throw new UserException(emailErr);

        String pwdErr = ValidationUtil.validatePassword(password);
        if (pwdErr != null) throw new UserException(pwdErr);

        try {
            if (userDAO.emailExists(email.trim())) {
                throw new UserException("Cet email est déjà utilisé");
            }

            Role role = roleDAO.findById(roleId);
            if (role == null) throw new UserException("Rôle invalide");

            User user = new User();
            user.setNom(nom.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setPassword(PasswordUtil.hashPassword(password));
            user.setRole(role);
            user.setStatut(statut != null ? statut : "ACTIF");

            boolean created = userDAO.create(user);
            if (!created) throw new UserException("Impossible de créer l'utilisateur");

            logger.info("Utilisateur créé: {} ({})", nom, email);
            return user;

        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL création utilisateur", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    public void updateUser(int id, String nom, String email, int roleId, String statut) throws UserException {
        String nomErr = ValidationUtil.validateNom(nom);
        if (nomErr != null) throw new UserException(nomErr);

        String emailErr = ValidationUtil.validateEmail(email);
        if (emailErr != null) throw new UserException(emailErr);

        try {
            if (userDAO.emailExistsForOtherUser(email.trim(), id)) {
                throw new UserException("Cet email est déjà utilisé par un autre utilisateur");
            }

            Role role = roleDAO.findById(roleId);
            if (role == null) throw new UserException("Rôle invalide");

            User user = userDAO.findById(id);
            if (user == null) throw new UserException("Utilisateur introuvable");

            user.setNom(nom.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setRole(role);
            user.setStatut(statut);

            boolean updated = userDAO.update(user);
            if (!updated) throw new UserException("Impossible de mettre à jour l'utilisateur");

            logger.info("Utilisateur mis à jour: {} (ID: {})", nom, id);

        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL mise à jour utilisateur", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    public void updatePassword(int userId, String newPassword) throws UserException {
        String pwdErr = ValidationUtil.validatePassword(newPassword);
        if (pwdErr != null) throw new UserException(pwdErr);

        try {
            String hashed = PasswordUtil.hashPassword(newPassword);
            boolean updated = userDAO.updatePassword(userId, hashed);
            if (!updated) throw new UserException("Impossible de mettre à jour le mot de passe");
            logger.info("Mot de passe mis à jour pour utilisateur ID: {}", userId);
        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL mise à jour mot de passe", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    public boolean verifyPassword(String email, String password) {
        try {
            User user = userDAO.findByEmail(email.trim().toLowerCase());
            if (user == null) return false;
            return PasswordUtil.verifyPassword(password, user.getPassword());
        } catch (SQLException e) {
            logger.error("Erreur vérification mot de passe", e);
            return false;
        }
    }

    public void updateUser(User user) throws UserException {
        try {
            if (userDAO.emailExistsForOtherUser(user.getEmail().trim(), user.getId())) {
                throw new UserException("Cet email est déjà utilisé par un autre utilisateur");
            }
            
            boolean updated = userDAO.update(user);
            if (!updated) throw new UserException("Impossible de mettre à jour l'utilisateur");
            
            logger.info("Utilisateur mis à jour: {} (ID: {})", user.getNom(), user.getId());
        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL mise à jour utilisateur", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    public void deleteUser(int id) throws UserException {
        try {
            boolean deleted = userDAO.delete(id);
            if (!deleted) throw new UserException("Impossible de supprimer l'utilisateur. Il peut avoir des dépendances.");
            logger.info("Utilisateur supprimé ID: {}", id);
        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL suppression utilisateur", e);
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("a foreign key constraint")) {
                throw new UserException("Impossible de supprimer : cet utilisateur a des transactions associées.");
            }
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    public void toggleUserStatus(int userId, String currentStatut) throws UserException {
        try {
            String newStatut = "ACTIF".equals(currentStatut) ? "INACTIF" : "ACTIF";
            boolean updated = userDAO.updateStatut(userId, newStatut);
            if (!updated) throw new UserException("Impossible de modifier le statut");
            logger.info("Statut utilisateur ID {} changé à: {}", userId, newStatut);
        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL toggle statut", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    public void disableUser(int userId) throws UserException {
        try {
            boolean updated = userDAO.updateStatut(userId, "INACTIF");
            if (!updated) throw new UserException("Impossible de désactiver l'utilisateur");
            logger.info("Utilisateur désactivé ID: {}", userId);
        } catch (UserException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL désactivation utilisateur", e);
            throw new UserException("Erreur base de données: " + e.getMessage());
        }
    }

    public boolean emailExists(String email) throws UserException {
        try {
            return userDAO.emailExists(email);
        } catch (SQLException e) {
            throw new UserException("Erreur vérification email: " + e.getMessage());
        }
    }

    public int countUsers() throws UserException {
        try {
            return userDAO.count();
        } catch (SQLException e) {
            throw new UserException("Erreur comptage utilisateurs: " + e.getMessage());
        }
    }

    public int countByRole(int roleId) throws UserException {
        try {
            return userDAO.countByRole(roleId);
        } catch (SQLException e) {
            throw new UserException("Erreur comptage par rôle: " + e.getMessage());
        }
    }

    public int getActiveUserCount() throws UserException {
        try {
            return userDAO.countByStatut("ACTIF");
        } catch (SQLException e) {
            throw new UserException("Erreur comptage utilisateurs actifs: " + e.getMessage());
        }
    }
}
