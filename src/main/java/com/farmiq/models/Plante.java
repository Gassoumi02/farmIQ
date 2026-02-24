package com.farmiq.models;

import java.time.LocalDate;

/**
 * Modèle représentant une plante dans le système FarmIQ
 * @author FarmIQ Team
 * @version 1.0
 */
public class Plante {
    private int idPlante;
    private String nom;
    private String type;
    private LocalDate datePlantation;
    private LocalDate dateRecoltePrevue;
    private int quantite;
    private String etat;
    private Integer userId; // Pour associer à un agriculteur

    // ==================== CONSTRUCTEURS ====================

    public Plante() {
        this.etat = "semée";
    }

    public Plante(int idPlante, String nom, String type, LocalDate datePlantation,
                  LocalDate dateRecoltePrevue, int quantite, String etat) {
        this.idPlante = idPlante;
        this.nom = nom;
        this.type = type;
        this.datePlantation = datePlantation;
        this.dateRecoltePrevue = dateRecoltePrevue;
        this.quantite = quantite;
        this.etat = etat;
    }

    // ==================== GETTERS ET SETTERS ====================

    public int getIdPlante() {
        return idPlante;
    }

    public void setIdPlante(int idPlante) {
        this.idPlante = idPlante;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDatePlantation() {
        return datePlantation;
    }

    public void setDatePlantation(LocalDate datePlantation) {
        this.datePlantation = datePlantation;
    }

    public LocalDate getDateRecoltePrevue() {
        return dateRecoltePrevue;
    }

    public void setDateRecoltePrevue(LocalDate dateRecoltePrevue) {
        this.dateRecoltePrevue = dateRecoltePrevue;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie si la plante est prête à être récoltée
     * @return true si la date de récolte est aujourd'hui ou passée
     */
    public boolean isPretePourRecolte() {
        if (dateRecoltePrevue == null) return false;
        return !dateRecoltePrevue.isAfter(LocalDate.now());
    }

    /**
     * Calcule le nombre de jours avant la récolte
     * @return nombre de jours (négatif si déjà passé)
     */
    public long getJoursAvantRecolte() {
        if (dateRecoltePrevue == null) return 0;
        return LocalDate.now().until(dateRecoltePrevue).getDays();
    }

    /**
     * Obtient un emoji représentant l'état
     * @return emoji de l'état
     */
    public String getEtatEmoji() {
        switch (etat.toLowerCase()) {
            case "semée": return "🌱";
            case "en croissance": return "🌿";
            case "prête à récolter": return "🌾";
            case "récoltée": return "✅";
            default: return "❓";
        }
    }

    /**
     * Obtient une représentation formatée de la plante
     * @return Description de la plante
     */
    public String getDisplayInfo() {
        return String.format("%s %s (%s) - %d unités",
                getEtatEmoji(), nom, type, quantite);
    }

    // ==================== MÉTHODES OVERRIDE ====================

    @Override
    public String toString() {
        return "Plante{" +
                "id=" + idPlante +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", plantation=" + datePlantation +
                ", recolte=" + dateRecoltePrevue +
                ", quantite=" + quantite +
                ", etat='" + etat + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plante plante = (Plante) o;
        return idPlante == plante.idPlante;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idPlante);
    }
}