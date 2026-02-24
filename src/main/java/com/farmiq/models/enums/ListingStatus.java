package com.farmiq.models.enums;

public enum ListingStatus {
    BROUILLON("Brouillon"),
    ACTIF("Actif"),
    PAUSE("En pause"),
    VENDU("Vendu"),
    EXPIRE("Expiré");

    private final String displayName;

    ListingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ListingStatus fromString(String status) {
        for (ListingStatus ls : ListingStatus.values()) {
            if (ls.name().equalsIgnoreCase(status)) {
                return ls;
            }
        }
        return BROUILLON;
    }
}
