package com.farmiq.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Tache {
    
    private int id;
    private int userId;
    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String priorite;
    private String statut;
    private String type;
    private LocalDateTime createdAt;

    public Tache() {
        this.statut = "A_FAIRE";
        this.priorite = "MOYENNE";
        this.type = "AUTRE";
    }

    public Tache(int userId, String titre, String description, LocalDate dateDebut, String priorite, String type) {
        this.userId = userId;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.priorite = priorite;
        this.type = type;
        this.statut = "A_FAIRE";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
