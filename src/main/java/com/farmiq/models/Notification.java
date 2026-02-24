package com.farmiq.models;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private String titre;
    private String message;
    private String type;
    private boolean lu;
    private String lien;
    private LocalDateTime createdAt;

    public Notification() {
        this.lu = false;
        this.type = "INFO";
    }

    public Notification(int userId, String titre, String message, String type) {
        this.userId = userId;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.lu = false;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
