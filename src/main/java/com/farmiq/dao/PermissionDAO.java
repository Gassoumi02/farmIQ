package com.farmiq.dao;

import com.farmiq.models.Permission;
import com.farmiq.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des permissions
 * @author FarmIQ Team
 * @version 1.0
 */
public class PermissionDAO {

    /**
     * Récupérer toutes les permissions
     * @return Liste de toutes les permissions
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Permission> findAll() throws SQLException {
        String sql = "SELECT * FROM permissions ORDER BY name";
        List<Permission> permissions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                permissions.add(mapResultSetToPermission(rs));
            }
        }
        
        System.out.println("📋 " + permissions.size() + " permission(s) chargée(s)");
        return permissions;
    }

    /**
     * Rechercher une permission par nom
     * @param name Nom de la permission
     * @return Permission trouvée ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public Permission findByName(String name) throws SQLException {
        String sql = "SELECT * FROM permissions WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPermission(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Rechercher une permission par ID
     * @param id ID de la permission
     * @return Permission trouvée ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public Permission findById(int id) throws SQLException {
        String sql = "SELECT * FROM permissions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPermission(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Créer une nouvelle permission
     * @param permission Permission à créer
     * @return true si la création réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean create(Permission permission) throws SQLException {
        String sql = "INSERT INTO permissions (name, description) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, permission.getName());
            pstmt.setString(2, permission.getDescription());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        permission.setId(rs.getInt(1));
                    }
                }
                System.out.println("✅ Permission créée : " + permission.getName());
                return true;
            }
            return false;
        }
    }

    /**
     * Mettre à jour une permission
     * @param permission Permission avec données mises à jour
     * @return true si la mise à jour réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean update(Permission permission) throws SQLException {
        String sql = "UPDATE permissions SET name = ?, description = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, permission.getName());
            pstmt.setString(2, permission.getDescription());
            pstmt.setInt(3, permission.getId());
            
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Permission mise à jour : " + permission.getName());
            }
            return success;
        }
    }

    /**
     * Supprimer une permission
     * @param id ID de la permission à supprimer
     * @return true si la suppression réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM permissions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            boolean success = pstmt.executeUpdate() > 0;
            
            if (success) {
                System.out.println("✅ Permission supprimée (ID: " + id + ")");
            }
            return success;
        }
    }

    /**
     * Mapper un ResultSet vers un objet Permission
     * @param rs ResultSet contenant les données
     * @return Objet Permission
     * @throws SQLException en cas d'erreur SQL
     */
    private Permission mapResultSetToPermission(ResultSet rs) throws SQLException {
        Permission permission = new Permission();
        permission.setId(rs.getInt("id"));
        permission.setName(rs.getString("name"));
        permission.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            permission.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return permission;
    }
}
