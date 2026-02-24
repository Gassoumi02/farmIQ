package com.farmiq.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PasswordUtil {
    private static final Logger logger = LogManager.getLogger(PasswordUtil.class);
    private static final int BCRYPT_ROUNDS = 12;

    public static String hashPassword(String plainPassword) {
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
        logger.debug("Password hashed with BCrypt");
        return hashed;
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("Erreur vérification mot de passe", e);
            return false;
        }
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return checkPassword(plainPassword, hashedPassword);
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);
        return hasUpper && hasDigit && hasSpecial;
    }

    public static String getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) return "VIDE";
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.chars().anyMatch(Character::isUpperCase)) score++;
        if (password.chars().anyMatch(Character::isDigit)) score++;
        if (password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0)) score++;
        if (score <= 2) return "FAIBLE";
        if (score <= 3) return "MOYEN";
        return "FORT";
    }
}
