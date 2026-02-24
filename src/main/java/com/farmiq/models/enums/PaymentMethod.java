package com.farmiq.models.enums;

public enum PaymentMethod {
    ESPECES("Espèces"),
    VIREMENT("Virement bancaire"),
    CARTE("Carte bancaire"),
    PAIEMENT_A_LIVRAISON("Paiement à la livraison");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromString(String method) {
        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.name().equalsIgnoreCase(method)) {
                return pm;
            }
        }
        return ESPECES;
    }
}
