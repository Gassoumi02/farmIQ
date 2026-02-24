package com.farmiq.models.enums;

/**
 * Types de notifications dans FarmIQ
 */
public enum NotificationType {
    INFO("Information"),
    ALERTE("Alerte"),
    SUCCES("Succès"),
    ERREUR("Erreur"),
    COMMANDE("Commande"),
    PAIEMENT("Paiement");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static NotificationType fromString(String value) {
        if (value == null) {
            return INFO;
        }
        try {
            return NotificationType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INFO;
        }
    }
}
