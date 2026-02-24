package com.farmiq.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modèle représentant un utilisateur du système FarmIQ
 * @author FarmIQ Team
 * @version 1.0
 */
public class User {
    private int id;
    private String nom;
    private String email;
    private String password;
    private Role role;
    private String statut;
    private String photoUrl;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime derniereConnexion;
    private List<Permission> permissions;

    // ==================== CONSTRUCTEURS ====================
    
    public User() {
        this.statut = "ACTIF";
    }

    public User(String nom, String email, String password, Role role) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.role = role;
        this.statut = "ACTIF";
    }

    // ==================== GETTERS ET SETTERS ====================
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie si l'utilisateur possède une permission spécifique
     * @param permissionName Nom de la permission à vérifier
     * @return true si l'utilisateur possède la permission
     */
    public boolean hasPermission(String permissionName) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    /**
     * Vérifie si l'utilisateur est administrateur
     * @return true si l'utilisateur a le rôle ADMIN
     */
    public boolean isAdmin() {
        return role != null && "ADMIN".equals(role.getName());
    }

    /**
     * Vérifie si le compte utilisateur est actif
     * @return true si le statut est ACTIF
     */
    public boolean isActive() {
        return "ACTIF".equals(statut);
    }

    /**
     * Vérifie si l'utilisateur est agriculteur
     * @return true si l'utilisateur a le rôle AGRICULTEUR
     */
    public boolean isAgriculteur() {
        return role != null && "AGRICULTEUR".equals(role.getName());
    }

    /**
     * Vérifie si l'utilisateur est technicien
     * @return true si l'utilisateur a le rôle TECHNICIEN
     */
    public boolean isTechnicien() {
        return role != null && "TECHNICIEN".equals(role.getName());
    }

    /**
     * Obtenir le nom complet du rôle
     * @return Nom du rôle ou "N/A" si non défini
     */
    public String getRoleName() {
        return role != null ? role.getName() : "N/A";
    }

    /**
     * Obtenir une description formatée de l'utilisateur
     * @return Description de l'utilisateur
     */
    public String getDisplayName() {
        return nom + " (" + getRoleName() + ")";
    }

    // ==================== MÉTHODES OVERRIDE ====================
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", role=" + getRoleName() +
                ", statut='" + statut + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
