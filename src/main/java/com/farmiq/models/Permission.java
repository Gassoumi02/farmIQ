package com.farmiq.models;

import java.time.LocalDateTime;

/**
 * Modèle représentant une permission système
 * @author FarmIQ Team
 * @version 1.0
 */
public class Permission {
    private int id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    // ==================== CONSTRUCTEURS ====================
    
    public Permission() {}

    public Permission(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ==================== GETTERS ET SETTERS ====================
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== MÉTHODES OVERRIDE ====================
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
