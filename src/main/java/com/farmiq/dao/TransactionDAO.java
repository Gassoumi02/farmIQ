package com.farmiq.dao;

import com.farmiq.models.Transaction;
import com.farmiq.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des transactions financières
 * @author FarmIQ Team
 * @version 1.0
 */
public class TransactionDAO {

    /**
     * Créer une nouvelle transaction
     * @param transaction Transaction à créer
     * @return true si la création réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean create(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, type, montant, date, description, statut) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, transaction.getUserId());
            pstmt.setString(2, transaction.getType());
            pstmt.setDouble(3, transaction.getMontant());
            pstmt.setDate(4, Date.valueOf(transaction.getDate()));
            pstmt.setString(5, transaction.getDescription());
            pstmt.setString(6, transaction.getStatut());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        transaction.setId(rs.getInt(1));
                    }
                }
                System.out.println("✅ Transaction créée : " + transaction.getType() + " - " + transaction.getMontant() + " DT");
                return true;
            }
            return false;
        }
    }

    /**
     * Récupérer toutes les transactions
     * @return Liste de toutes les transactions
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Transaction> findAll() throws SQLException {
        String sql = "SELECT * FROM transactions ORDER BY date DESC, created_at DESC";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        
        System.out.println("📋 " + transactions.size() + " transaction(s) chargée(s)");
        return transactions;
    }

    /**
     * Récupérer toutes les transactions avec informations utilisateur (pour admin)
     * @return Liste de toutes les transactions avec user info
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Transaction> findAllWithUserInfo() throws SQLException {
        String sql = "SELECT t.*, u.nom as user_nom, u.email as user_email " +
                     "FROM transactions t " +
                     "INNER JOIN users u ON t.user_id = u.id " +
                     "ORDER BY t.date DESC, t.created_at DESC";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Transaction t = mapResultSetToTransaction(rs);
                t.setUserNom(rs.getString("user_nom"));
                t.setUserEmail(rs.getString("user_email"));
                transactions.add(t);
            }
        }
        
        System.out.println("📋 " + transactions.size() + " transaction(s) chargée(s) avec info utilisateur");
        return transactions;
    }

    /**
     * Récupérer les transactions d'un utilisateur spécifique (pour front-office)
     * @param userId ID de l'utilisateur
     * @return Liste des transactions de l'utilisateur
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Transaction> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC, created_at DESC";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        
        System.out.println("📋 " + transactions.size() + " transaction(s) pour l'utilisateur ID: " + userId);
        return transactions;
    }

    /**
     * Rechercher une transaction par ID
     * @param id ID de la transaction
     * @return Transaction trouvée ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public Transaction findById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Mettre à jour une transaction
     * @param transaction Transaction avec données mises à jour
     * @return true si la mise à jour réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean update(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET type = ?, montant = ?, date = ?, description = ?, statut = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, transaction.getType());
            pstmt.setDouble(2, transaction.getMontant());
            pstmt.setDate(3, Date.valueOf(transaction.getDate()));
            pstmt.setString(4, transaction.getDescription());
            pstmt.setString(5, transaction.getStatut());
            pstmt.setInt(6, transaction.getId());
            
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Transaction mise à jour (ID: " + transaction.getId() + ")");
            }
            return success;
        }
    }

    /**
     * Supprimer une transaction
     * @param id ID de la transaction à supprimer
     * @return true si la suppression réussit
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            boolean success = pstmt.executeUpdate() > 0;
            
            if (success) {
                System.out.println("✅ Transaction supprimée (ID: " + id + ")");
            }
            return success;
        }
    }

    /**
     * Filtrer les transactions par type
     * @param type Type de transaction (VENTE/ACHAT)
     * @return Liste des transactions filtrées
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Transaction> findByType(String type) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE type = ? ORDER BY date DESC";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        
        System.out.println("🔍 " + transactions.size() + " transaction(s) de type " + type + " trouvée(s)");
        return transactions;
    }

    /**
     * Filtrer les transactions par période
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des transactions filtrées
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Transaction> findByPeriod(LocalDate dateDebut, LocalDate dateFin) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(dateDebut));
            pstmt.setDate(2, Date.valueOf(dateFin));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        
        System.out.println("🔍 " + transactions.size() + " transaction(s) trouvée(s) pour la période");
        return transactions;
    }

    /**
     * Filtrer les transactions par montant
     * @param montantMin Montant minimum
     * @param montantMax Montant maximum
     * @return Liste des transactions filtrées
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Transaction> findByMontant(double montantMin, double montantMax) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE montant BETWEEN ? AND ? ORDER BY montant DESC";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, montantMin);
            pstmt.setDouble(2, montantMax);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        
        System.out.println("🔍 " + transactions.size() + " transaction(s) trouvée(s) dans la fourchette de montant");
        return transactions;
    }

    /**
     * Filtrer les transactions avec critères multiples
     * @param type Type de transaction (peut être null)
     * @param dateDebut Date de début (peut être null)
     * @param dateFin Date de fin (peut être null)
     * @param montantMin Montant minimum (peut être null)
     * @param montantMax Montant maximum (peut être null)
     * @return Liste des transactions filtrées
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Transaction> findWithFilters(String type, LocalDate dateDebut, LocalDate dateFin, 
                                             Double montantMin, Double montantMax) throws SQLException {
        return findWithFilters(null, type, dateDebut, dateFin, montantMin, montantMax);
    }

    /**
     * Filtrer les transactions avec critères multiples pour un utilisateur spécifique
     * @param userId ID de l'utilisateur (null pour tous)
     * @param type Type de transaction (peut être null)
     * @param dateDebut Date de début (peut être null)
     * @param dateFin Date de fin (peut être null)
     * @param montantMin Montant minimum (peut être null)
     * @param montantMax Montant maximum (peut être null)
     * @return Liste des transactions filtrées
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Transaction> findWithFilters(Integer userId, String type, LocalDate dateDebut, LocalDate dateFin, 
                                             Double montantMin, Double montantMax) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        
        if (type != null && !type.isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        
        if (dateDebut != null) {
            sql.append(" AND date >= ?");
            params.add(Date.valueOf(dateDebut));
        }
        
        if (dateFin != null) {
            sql.append(" AND date <= ?");
            params.add(Date.valueOf(dateFin));
        }
        
        if (montantMin != null) {
            sql.append(" AND montant >= ?");
            params.add(montantMin);
        }
        
        if (montantMax != null) {
            sql.append(" AND montant <= ?");
            params.add(montantMax);
        }
        
        sql.append(" ORDER BY date DESC");
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Date) {
                    pstmt.setDate(i + 1, (Date) param);
                } else if (param instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) param);
                }
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        
        System.out.println("🔍 " + transactions.size() + " transaction(s) trouvée(s) avec filtres");
        return transactions;
    }

    /**
     * Calculer le total des ventes
     * @return Total des ventes
     * @throws SQLException en cas d'erreur SQL
     */
    public double getTotalVentes() throws SQLException {
        return getTotalVentes(null);
    }

    /**
     * Calculer le total des ventes pour un utilisateur spécifique
     * @param userId ID de l'utilisateur (null pour tous)
     * @return Total des ventes
     * @throws SQLException en cas d'erreur SQL
     */
    public double getTotalVentes(Integer userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM transactions WHERE type = 'VENTE' AND statut != 'ANNULEE'";
        if (userId != null) {
            sql += " AND user_id = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (userId != null) {
                pstmt.setInt(1, userId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        
        return 0.0;
    }

    /**
     * Calculer le total des achats
     * @return Total des achats
     * @throws SQLException en cas d'erreur SQL
     */
    public double getTotalAchats() throws SQLException {
        return getTotalAchats(null);
    }

    /**
     * Calculer le total des achats pour un utilisateur spécifique
     * @param userId ID de l'utilisateur (null pour tous)
     * @return Total des achats
     * @throws SQLException en cas d'erreur SQL
     */
    public double getTotalAchats(Integer userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM transactions WHERE type = 'ACHAT' AND statut != 'ANNULEE'";
        if (userId != null) {
            sql += " AND user_id = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (userId != null) {
                pstmt.setInt(1, userId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        
        return 0.0;
    }

    /**
     * Calculer le bénéfice (Ventes - Achats)
     * @return Bénéfice total
     * @throws SQLException en cas d'erreur SQL
     */
    public double getBenefice() throws SQLException {
        return getBenefice(null);
    }

    /**
     * Calculer le bénéfice pour un utilisateur spécifique
     * @param userId ID de l'utilisateur (null pour tous)
     * @return Bénéfice
     * @throws SQLException en cas d'erreur SQL
     */
    public double getBenefice(Integer userId) throws SQLException {
        return getTotalVentes(userId) - getTotalAchats(userId);
    }


    /**
     * Compter le nombre total de transactions
     * @return Nombre de transactions
     * @throws SQLException en cas d'erreur SQL
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions";
        
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
     * Mapper un ResultSet vers un objet Transaction
     * @param rs ResultSet contenant les données
     * @return Objet Transaction
     * @throws SQLException en cas d'erreur SQL
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setType(rs.getString("type"));
        transaction.setMontant(rs.getDouble("montant"));
        
        Date date = rs.getDate("date");
        if (date != null) {
            transaction.setDate(date.toLocalDate());
        }
        
        transaction.setDescription(rs.getString("description"));
        transaction.setStatut(rs.getString("statut"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            transaction.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return transaction;
    }
}
