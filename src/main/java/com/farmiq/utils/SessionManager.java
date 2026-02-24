package com.farmiq.utils;

import com.farmiq.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionManager {
    private static final Logger logger = LogManager.getLogger(SessionManager.class);
    private static SessionManager instance;
    private User currentUser;
    private String sessionId;

    private SessionManager() {
        this.sessionId = java.util.UUID.randomUUID().toString();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        logger.info("Session ouverte pour: {} ({})", user.getNom(), user.getRoleName());
    }

    public void logout() {
        if (currentUser != null) {
            logger.info("Session fermée pour: {}", currentUser.getNom());
        }
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean hasPermission(String permissionName) {
        if (currentUser == null) return false;
        if (currentUser.isAdmin()) return true;
        return currentUser.hasPermission(permissionName);
    }

    public void refreshUser(User user) {
        this.currentUser = user;
        logger.debug("Session rafraîchie pour: {}", user.getNom());
    }

    public String getSessionId() {
        return sessionId;
    }
}
