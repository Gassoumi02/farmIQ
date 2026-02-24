package com.farmiq.models.enums;

public enum PaymentStatus {
    EN_ATTENTE("En attente"),
    COMPLETE("Complété"),
    ECHOUE("Échoué"),
    REMBOURSE("Remboursé");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentStatus fromString(String status) {
        for (PaymentStatus ps : PaymentStatus.values()) {
            if (ps.name().equalsIgnoreCase(status)) {
                return ps;
            }
        }
        return EN_ATTENTE;
    }
}
