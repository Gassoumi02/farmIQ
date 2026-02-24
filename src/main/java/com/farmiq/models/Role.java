package com.farmiq.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un rôle utilisateur
 * @author FarmIQ Team
 * @version 1.0
 */
public class Role {
    private int id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<Permission> permissions;

    // ==================== CONSTRUCTEURS ====================

    public Role() {
        this.permissions = new ArrayList<>();
    }

    public Role(int id, String name) {
        this.id = id;
        this.name = name;
        this.permissions = new ArrayList<>();
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
        this.permissions = new ArrayList<>();
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

    /**
     * Alias pour getName() - retourne le nom du rôle
     * @return Nom du rôle
     */
    public String getNom() {
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

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions != null ? permissions : new ArrayList<>();
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Ajouter une permission au rôle
     * @param permission Permission à ajouter
     */
    public void addPermission(Permission permission) {
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    /**
     * Retirer une permission du rôle
     * @param permission Permission à retirer
     */
    public void removePermission(Permission permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
    }

    /**
     * Vérifie si le rôle possède une permission spécifique
     * @param permissionName Nom de la permission
     * @return true si le rôle possède la permission
     */
    public boolean hasPermission(String permissionName) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    /**
     * Obtenir le nombre de permissions
     * @return Nombre de permissions
     */
    public int getPermissionCount() {
        return permissions != null ? permissions.size() : 0;
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
        Role role = (Role) o;
        return id == role.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}