-- =====================================================
-- FarmIQ - Ajouter user_id à la table transactions
-- Migration pour lier les transactions aux utilisateurs
-- =====================================================

USE farmiq;

-- Ajouter la colonne user_id si elle n'existe pas
ALTER TABLE transactions 
ADD COLUMN IF NOT EXISTS user_id INT NOT NULL DEFAULT 1 AFTER id,
ADD INDEX idx_user_id (user_id),
ADD CONSTRAINT fk_transaction_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Mettre à jour les transactions existantes pour les lier à l'admin
UPDATE transactions SET user_id = 1 WHERE user_id = 0;

-- Vue pour les transactions avec informations utilisateur
CREATE OR REPLACE VIEW v_transactions_with_user AS
SELECT 
    t.*,
    u.nom as user_nom,
    u.email as user_email,
    r.name as user_role
FROM transactions t
INNER JOIN users u ON t.user_id = u.id
INNER JOIN roles r ON u.role_id = r.id;

-- Procédure pour obtenir les transactions d'un utilisateur
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_get_user_transactions(
    IN p_user_id INT
)
BEGIN
    SELECT * FROM transactions 
    WHERE user_id = p_user_id 
    ORDER BY date DESC, created_at DESC;
END //
DELIMITER ;

SELECT '✅ Migration terminée : user_id ajouté à la table transactions' as Status;
