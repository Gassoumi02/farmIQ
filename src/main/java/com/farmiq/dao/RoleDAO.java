package com.farmiq.dao;

import com.farmiq.models.Role;
import com.farmiq.models.Permission;
import com.farmiq.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des rôles
 * @author FarmIQ Team
 * @version 1.0
 */
public class RoleDAO {

    /**
     * Récupérer tous les rôles
     * @return Liste de tous les rôles
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Role> findAll() throws SQLException {
        String sql = "SELECT * FROM roles ORDER BY name";
        List<Role> roles = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                roles.add(mapResultSetToRole(rs));
            }
        }
        
        System.out.println("📋 " + roles.size() + " rôle(s) chargé(s)");
        return roles;
    }

    /**
     * Rechercher un rôle par nom
     * @param name Nom du rôle
     * @return Rôle trouvé ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public Role findByName(String name) throws SQLException {
        String sql = "SELECT * FROM roles WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRole(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Rechercher un rôle par ID
     * @param id ID du rôle
     * @return Rôle trouvé ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public Role findById(int id) throws SQLException {
        String sql = "SELECT * FROM roles WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRole(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Créer un nouveau rôle
     * @param role Rôle à créer
     * @return true si la création réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean create(Role role) throws SQLException {
        String sql = "INSERT INTO roles (name, description) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, role.getName());
            pstmt.setString(2, role.getDescription());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        role.setId(rs.getInt(1));
                    }
                }
                System.out.println("✅ Rôle créé : " + role.getName());
                return true;
            }
            return false;
        }
    }

    /**
     * Mettre à jour un rôle
     * @param role Rôle avec données mises à jour
     * @return true si la mise à jour réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean update(Role role) throws SQLException {
        String sql = "UPDATE roles SET name = ?, description = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role.getName());
            pstmt.setString(2, role.getDescription());
            pstmt.setInt(3, role.getId());
            
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Rôle mis à jour : " + role.getName());
            }
            return success;
        }
    }

    /**
     * Supprimer un rôle
     * @param id ID du rôle à supprimer
     * @return true si la suppression réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM roles WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            boolean success = pstmt.executeUpdate() > 0;
            
            if (success) {
                System.out.println("✅ Rôle supprimé (ID: " + id + ")");
            }
            return success;
        }
    }

    /**
     * Charger les permissions d'un rôle
     * @param roleId ID du rôle
     * @return Liste des permissions
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Permission> loadRolePermissions(int roleId) throws SQLException {
        String sql = "SELECT p.* FROM permissions p " +
                     "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
                     "WHERE rp.role_id = ?";
        
        List<Permission> permissions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Permission permission = new Permission();
                    permission.setId(rs.getInt("id"));
                    permission.setName(rs.getString("name"));
                    permission.setDescription(rs.getString("description"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        permission.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    
                    permissions.add(permission);
                }
            }
        }
        
        return permissions;
    }

    /**
     * Assigner une permission à un rôle
     * @param roleId ID du rôle
     * @param permissionId ID de la permission
     * @return true si l'assignation réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean assignPermission(int roleId, int permissionId) throws SQLException {
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Ignorer les doublons (clé primaire composite)
            if (e.getErrorCode() == 1062) { // Duplicate entry
                return true;
            }
            throw e;
        }
    }

    /**
     * Retirer une permission d'un rôle
     * @param roleId ID du rôle
     * @param permissionId ID de la permission
     * @return true si le retrait réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean removePermission(int roleId, int permissionId) throws SQLException {
        String sql = "DELETE FROM role_permissions WHERE role_id = ? AND permission_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Mapper un ResultSet vers un objet Role
     * @param rs ResultSet contenant les données
     * @return Objet Role
     * @throws SQLException en cas d'erreur SQL
     */
    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            role.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return role;
    }
}
