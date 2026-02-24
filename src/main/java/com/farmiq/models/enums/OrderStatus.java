package com.farmiq.models.enums;

public enum OrderStatus {
    EN_ATTENTE("En attente"),
    CONFIRME("Confirmé"),
    EN_PREPARATION("En préparation"),
    EXPEDIE("Expédié"),
    LIVRE("Livré"),
    ANNULE("Annulé"),
    REMBOURSE("Remboursé");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static OrderStatus fromString(String status) {
        for (OrderStatus os : OrderStatus.values()) {
            if (os.name().equalsIgnoreCase(status)) {
                return os;
            }
        }
        return EN_ATTENTE;
    }
}
