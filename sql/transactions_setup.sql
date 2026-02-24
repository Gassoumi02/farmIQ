-- =====================================================
-- FarmIQ - Gestion des Transactions
-- Script SQL pour la table transactions
-- =====================================================

USE farmiq;

-- =====================================================
-- Table des transactions
-- =====================================================
CREATE TABLE IF NOT EXISTS transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type ENUM('VENTE', 'ACHAT') NOT NULL,
    montant DECIMAL(10, 2) NOT NULL CHECK (montant > 0),
    date DATE NOT NULL,
    description TEXT,
    statut ENUM('EN_ATTENTE', 'VALIDEE', 'ANNULEE') DEFAULT 'EN_ATTENTE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_type (type),
    INDEX idx_date (date),
    INDEX idx_statut (statut),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='Table des transactions financières (ventes et achats)';

-- =====================================================
-- Trigger : Mettre à jour updated_at automatiquement
-- =====================================================
DELIMITER //
CREATE TRIGGER IF NOT EXISTS before_transaction_update
BEFORE UPDATE ON transactions
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //
DELIMITER ;

-- =====================================================
-- Vue pour statistiques des transactions
-- =====================================================
CREATE OR REPLACE VIEW v_transaction_stats AS
SELECT 
    COUNT(*) as total_transactions,
    SUM(CASE WHEN type = 'VENTE' AND statut != 'ANNULEE' THEN montant ELSE 0 END) as total_ventes,
    SUM(CASE WHEN type = 'ACHAT' AND statut != 'ANNULEE' THEN montant ELSE 0 END) as total_achats,
    SUM(CASE WHEN type = 'VENTE' AND statut != 'ANNULEE' THEN montant ELSE 0 END) - 
    SUM(CASE WHEN type = 'ACHAT' AND statut != 'ANNULEE' THEN montant ELSE 0 END) as benefice,
    COUNT(CASE WHEN type = 'VENTE' THEN 1 END) as nb_ventes,
    COUNT(CASE WHEN type = 'ACHAT' THEN 1 END) as nb_achats,
    AVG(CASE WHEN type = 'VENTE' THEN montant END) as montant_moyen_vente,
    AVG(CASE WHEN type = 'ACHAT' THEN montant END) as montant_moyen_achat
FROM transactions;

-- =====================================================
-- Vue pour transactions récentes (30 derniers jours)
-- =====================================================
CREATE OR REPLACE VIEW v_transactions_recentes AS
SELECT 
    id,
    type,
    montant,
    date,
    statut,
    description,
    created_at
FROM transactions
WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
ORDER BY date DESC, created_at DESC;

-- =====================================================
-- Procédure stockée : Obtenir les statistiques par période
-- =====================================================
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_stats_periode(
    IN p_date_debut DATE,
    IN p_date_fin DATE
)
BEGIN
    SELECT 
        COUNT(*) as total_transactions,
        SUM(CASE WHEN type = 'VENTE' AND statut != 'ANNULEE' THEN montant ELSE 0 END) as total_ventes,
        SUM(CASE WHEN type = 'ACHAT' AND statut != 'ANNULEE' THEN montant ELSE 0 END) as total_achats,
        SUM(CASE WHEN type = 'VENTE' AND statut != 'ANNULEE' THEN montant ELSE 0 END) - 
        SUM(CASE WHEN type = 'ACHAT' AND statut != 'ANNULEE' THEN montant ELSE 0 END) as benefice
    FROM transactions
    WHERE date BETWEEN p_date_debut AND p_date_fin;
END //
DELIMITER ;

-- =====================================================
-- Procédure stockée : Top transactions par montant
-- =====================================================
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_top_transactions(
    IN p_type VARCHAR(10),
    IN p_limit INT
)
BEGIN
    IF p_type IS NULL OR p_type = '' THEN
        SELECT * FROM transactions 
        ORDER BY montant DESC 
        LIMIT p_limit;
    ELSE
        SELECT * FROM transactions 
        WHERE type = p_type
        ORDER BY montant DESC 
        LIMIT p_limit;
    END IF;
END //
DELIMITER ;

-- =====================================================
-- Données de test (optionnel)
-- =====================================================
INSERT INTO transactions (type, montant, date, description, statut) VALUES
('VENTE', 1500.00, '2026-02-01', 'Vente de blé - Lot A', 'VALIDEE'),
('ACHAT', 800.00, '2026-02-02', 'Achat de semences de maïs', 'VALIDEE'),
('VENTE', 2300.00, '2026-02-05', 'Vente de tomates - Marché local', 'VALIDEE'),
('ACHAT', 450.00, '2026-02-08', 'Achat d\'engrais bio', 'VALIDEE'),
('VENTE', 1800.00, '2026-02-10', 'Vente d\'olives - Export', 'EN_ATTENTE'),
('ACHAT', 600.00, '2026-02-12', 'Achat de matériel agricole', 'VALIDEE'),
('VENTE', 950.00, '2026-02-14', 'Vente de fruits saisonniers', 'VALIDEE'),
('ACHAT', 1200.00, '2026-02-15', 'Achat de pesticides naturels', 'EN_ATTENTE')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- =====================================================
-- Afficher les statistiques après insertion
-- =====================================================
SELECT 'Statistiques des transactions:' as Info;
SELECT * FROM v_transaction_stats;

SELECT 'Nombre total de transactions:' as Info, COUNT(*) as Total FROM transactions;
