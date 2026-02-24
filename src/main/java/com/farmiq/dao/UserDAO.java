package com.farmiq.dao;

import com.farmiq.models.User;
import com.farmiq.models.Role;
import com.farmiq.models.Permission;
import com.farmiq.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des utilisateurs
 * @author FarmIQ Team
 * @version 1.0
 */
public class UserDAO {

    /**
     * Créer un nouvel utilisateur
     * @param user Utilisateur à créer
     * @return true si la création réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean create(User user) throws SQLException {
        String sql = "INSERT INTO users (nom, email, password, role_id, statut, photo_url) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setInt(4, user.getRole().getId());
            pstmt.setString(5, user.getStatut());
            pstmt.setString(6, user.getPhotoUrl());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                System.out.println("✅ Utilisateur créé : " + user.getNom());
                return true;
            }
            return false;
        }
    }

    /**
     * Récupérer tous les utilisateurs
     * @return Liste de tous les utilisateurs
     * @throws SQLException en cas d'erreur SQL
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT u.*, r.id as role_id, r.name as role_name, r.description as role_desc " +
                     "FROM users u " +
                     "INNER JOIN roles r ON u.role_id = r.id " +
                     "ORDER BY u.created_at DESC";
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        
        System.out.println("📋 " + users.size() + " utilisateur(s) chargé(s)");
        return users;
    }

    /**
     * Rechercher un utilisateur par email
     * @param email Email de l'utilisateur
     * @return Utilisateur trouvé ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT u.*, r.id as role_id, r.name as role_name, r.description as role_desc " +
                     "FROM users u " +
                     "INNER JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Rechercher un utilisateur par ID
     * @param id ID de l'utilisateur
     * @return Utilisateur trouvé ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT u.*, r.id as role_id, r.name as role_name, r.description as role_desc " +
                     "FROM users u " +
                     "INNER JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Mettre à jour un utilisateur
     * @param user Utilisateur avec données mises à jour
     * @return true si la mise à jour réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET nom = ?, email = ?, role_id = ?, statut = ?, photo_url = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getRole().getId());
            pstmt.setString(4, user.getStatut());
            pstmt.setString(5, user.getPhotoUrl());
            pstmt.setInt(6, user.getId());
            
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Utilisateur mis à jour : " + user.getNom());
            }
            return success;
        }
    }

    /**
     * Mettre à jour le mot de passe d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param hashedPassword Nouveau mot de passe hashé
     * @return true si la mise à jour réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean updatePassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Mot de passe mis à jour pour l'utilisateur ID: " + userId);
            }
            return success;
        }
    }

    /**
     * Supprimer un utilisateur
     * @param id ID de l'utilisateur à supprimer
     * @return true si la suppression réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            boolean success = pstmt.executeUpdate() > 0;
            
            if (success) {
                System.out.println("✅ Utilisateur supprimé (ID: " + id + ")");
            }
            return success;
        }
    }

    /**
     * Vérifier si un email existe déjà
     * @param email Email à vérifier
     * @return true si l'email existe
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * Vérifier si un email existe pour un autre utilisateur
     * @param email Email à vérifier
     * @param excludeUserId ID de l'utilisateur à exclure
     * @return true si l'email existe pour un autre utilisateur
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean emailExistsForOtherUser(String email, int excludeUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setInt(2, excludeUserId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * Mettre à jour le token de réinitialisation de mot de passe
     * @param email Email de l'utilisateur
     * @param token Token de réinitialisation
     * @param expiry Date d'expiration du token
     * @return true si la mise à jour réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean updateResetToken(String email, String token, LocalDateTime expiry) throws SQLException {
        String sql = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            pstmt.setTimestamp(2, Timestamp.valueOf(expiry));
            pstmt.setString(3, email);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Rechercher un utilisateur par token de réinitialisation
     * @param token Token de réinitialisation
     * @return Utilisateur trouvé ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public User findByResetToken(String token) throws SQLException {
        String sql = "SELECT u.*, r.id as role_id, r.name as role_name, r.description as role_desc " +
                     "FROM users u " +
                     "INNER JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.reset_token = ? AND u.reset_token_expiry > NOW()";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Changer le statut d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param statut Nouveau statut (ACTIF/INACTIF)
     * @return true si la mise à jour réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean updateStatut(int userId, String statut) throws SQLException {
        String sql = "UPDATE users SET statut = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, statut);
            pstmt.setInt(2, userId);
            
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Statut mis à jour : " + statut + " pour utilisateur ID: " + userId);
            }
            return success;
        }
    }

    /**
     * Charger les permissions d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des permissions
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Permission> loadUserPermissions(int userId) throws SQLException {
        String sql = "SELECT p.* FROM permissions p " +
                     "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
                     "INNER JOIN users u ON u.role_id = rp.role_id " +
                     "WHERE u.id = ?";
        
        List<Permission> permissions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
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
     * Compter le nombre total d'utilisateurs
     * @return Nombre d'utilisateurs
     * @throws SQLException en cas d'erreur SQL
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }

    /**
     * Compter les utilisateurs par rôle
     * @param roleId ID du rôle
     * @return Nombre d'utilisateurs
     * @throws SQLException en cas d'erreur SQL
     */
    public int countByRole(int roleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }

    public int countByStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE statut = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statut);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Mapper un ResultSet vers un objet User
     * @param rs ResultSet contenant les données
     * @return Objet User
     * @throws SQLException en cas d'erreur SQL
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setStatut(rs.getString("statut"));
        user.setPhotoUrl(rs.getString("photo_url"));
        user.setResetToken(rs.getString("reset_token"));
        
        Timestamp resetExpiry = rs.getTimestamp("reset_token_expiry");
        if (resetExpiry != null) {
            user.setResetTokenExpiry(resetExpiry.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Créer le rôle
        Role role = new Role();
        role.setId(rs.getInt("role_id"));
        role.setName(rs.getString("role_name"));
        role.setDescription(rs.getString("role_desc"));
        user.setRole(role);
        
        return user;
    }
}
