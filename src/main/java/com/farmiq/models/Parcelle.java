package com.farmiq.models;

import java.time.LocalDate;

/**
 * Modèle représentant une parcelle agricole dans le système FarmIQ
 * @author FarmIQ Team
 * @version 1.0
 */
public class Parcelle {
    private int idParcelle;
    private String nomParcelle;
    private double surface; // en m²
    private String localisation;
    private String typeSol;
    private String etatParcelle;
    private LocalDate dateDerniereCulture;
    private boolean irrigation;
    private String remarques;
    private Integer idPlante; // Plante actuellement cultivée (peut être null)
    private Integer userId; // Propriétaire de la parcelle

    // ==================== CONSTRUCTEURS ====================

    public Parcelle() {
        this.etatParcelle = "disponible";
        this.irrigation = false;
    }

    public Parcelle(int idParcelle, String nomParcelle, double surface, String localisation,
                    String typeSol, String etatParcelle, LocalDate dateDerniereCulture,
                    boolean irrigation, String remarques, Integer idPlante) {
        this.idParcelle = idParcelle;
        this.nomParcelle = nomParcelle;
        this.surface = surface;
        this.localisation = localisation;
        this.typeSol = typeSol;
        this.etatParcelle = etatParcelle;
        this.dateDerniereCulture = dateDerniereCulture;
        this.irrigation = irrigation;
        this.remarques = remarques;
        this.idPlante = idPlante;
    }

    // ==================== GETTERS ET SETTERS ====================

    public int getIdParcelle() {
        return idParcelle;
    }

    public void setIdParcelle(int idParcelle) {
        this.idParcelle = idParcelle;
    }

    public String getNomParcelle() {
        return nomParcelle;
    }

    public void setNomParcelle(String nomParcelle) {
        this.nomParcelle = nomParcelle;
    }

    public double getSurface() {
        return surface;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getTypeSol() {
        return typeSol;
    }

    public void setTypeSol(String typeSol) {
        this.typeSol = typeSol;
    }

    public String getEtatParcelle() {
        return etatParcelle;
    }

    public void setEtatParcelle(String etatParcelle) {
        this.etatParcelle = etatParcelle;
    }

    public LocalDate getDateDerniereCulture() {
        return dateDerniereCulture;
    }

    public void setDateDerniereCulture(LocalDate dateDerniereCulture) {
        this.dateDerniereCulture = dateDerniereCulture;
    }

    public boolean isIrrigation() {
        return irrigation;
    }

    public void setIrrigation(boolean irrigation) {
        this.irrigation = irrigation;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }

    public Integer getIdPlante() {
        return idPlante;
    }

    public void setIdPlante(Integer idPlante) {
        this.idPlante = idPlante;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie si la parcelle est disponible pour une nouvelle culture
     * @return true si disponible
     */
    public boolean isDisponible() {
        return "disponible".equalsIgnoreCase(etatParcelle);
    }

    /**
     * Vérifie si la parcelle est actuellement cultivée
     * @return true si en culture
     */
    public boolean isEnCulture() {
        return "en culture".equalsIgnoreCase(etatParcelle);
    }

    /**
     * Obtient un emoji représentant l'état de la parcelle
     * @return emoji de l'état
     */
    public String getEtatEmoji() {
        if (etatParcelle == null) return "❓";

        switch (etatParcelle.toLowerCase()) {
            case "disponible": return "✅";
            case "en culture": return "🌾";
            case "en repos": return "💤";
            case "en préparation": return "🚜";
            default: return "🟫";
        }
    }

    /**
     * Obtient l'icône d'irrigation
     * @return emoji d'irrigation
     */
    public String getIrrigationIcon() {
        return irrigation ? "💧" : "🚫";
    }

    /**
     * Calcule la surface en hectares
     * @return surface en ha
     */
    public double getSurfaceEnHectares() {
        return surface / 10000.0;
    }

    /**
     * Obtient une représentation formatée de la parcelle
     * @return Description de la parcelle
     */
    public String getDisplayInfo() {
        return String.format("%s %s - %.0f m² (%s)",
                getEtatEmoji(), nomParcelle, surface, typeSol);
    }

    // ==================== MÉTHODES OVERRIDE ====================

    @Override
    public String toString() {
        return "Parcelle{" +
                "id=" + idParcelle +
                ", nom='" + nomParcelle + '\'' +
                ", surface=" + surface +
                " m², localisation='" + localisation + '\'' +
                ", typeSol='" + typeSol + '\'' +
                ", etat='" + etatParcelle + '\'' +
                ", irrigation=" + irrigation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parcelle parcelle = (Parcelle) o;
        return idParcelle == parcelle.idParcelle;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idParcelle);
    }
}