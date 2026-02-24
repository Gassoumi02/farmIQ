package com.farmiq.utils;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern NOM_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']{3,100}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidNom(String nom) {
        return nom != null && NOM_PATTERN.matcher(nom.trim()).matches() && nom.trim().length() >= 3;
    }

    public static boolean isValidPassword(String password) {
        return PasswordUtil.isValidPassword(password);
    }

    public static String validateNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) return "Le nom est requis";
        if (nom.trim().length() < 3) return "Le nom doit contenir au moins 3 caractères";
        if (nom.chars().anyMatch(Character::isDigit)) return "Le nom ne doit pas contenir de chiffres";
        if (!NOM_PATTERN.matcher(nom.trim()).matches()) return "Le nom contient des caractères invalides";
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "L'email est requis";
        if (!isValidEmail(email.trim())) return "Format email invalide";
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) return "Le mot de passe est requis";
        if (password.length() < 8) return "Le mot de passe doit contenir au moins 8 caractères";
        if (!password.chars().anyMatch(Character::isUpperCase)) return "Le mot de passe doit contenir au moins 1 majuscule";
        if (!password.chars().anyMatch(Character::isDigit)) return "Le mot de passe doit contenir au moins 1 chiffre";
        if (!password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0))
            return "Le mot de passe doit contenir au moins 1 caractère spécial";
        return null;
    }
}
