package com.farmiq.services;

import com.farmiq.dao.UserDAO;
import com.farmiq.models.User;
import com.farmiq.utils.PasswordUtil;
import com.farmiq.utils.SessionManager;
import com.farmiq.exceptions.AuthException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class AuthService {
    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User login(String email, String password) throws AuthException {
        try {
            User user = userDAO.findByEmail(email);
            if (user == null) throw new AuthException("Email ou mot de passe incorrect");
            if (!user.isActive()) throw new AuthException("Votre compte a été désactivé. Contactez l'administrateur.");
            if (!PasswordUtil.checkPassword(password, user.getPassword()))
                throw new AuthException("Email ou mot de passe incorrect");

            user.setPermissions(userDAO.loadUserPermissions(user.getId()));
            SessionManager.getInstance().login(user);
            logger.info("Connexion réussie: {} ({})", user.getNom(), user.getRoleName());
            return user;
        } catch (AuthException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL lors du login", e);
            throw new AuthException("Erreur base de données: " + e.getMessage());
        }
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public boolean isLoggedIn() {
        return SessionManager.getInstance().isLoggedIn();
    }

    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    public boolean hasPermission(String permissionName) {
        return SessionManager.getInstance().hasPermission(permissionName);
    }

    public boolean isAdmin() {
        return SessionManager.getInstance().isAdmin();
    }

    public void refreshCurrentUser() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                User refreshed = userDAO.findById(currentUser.getId());
                if (refreshed != null) {
                    refreshed.setPermissions(userDAO.loadUserPermissions(refreshed.getId()));
                    SessionManager.getInstance().refreshUser(refreshed);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur rafraîchissement utilisateur", e);
        }
    }
}
