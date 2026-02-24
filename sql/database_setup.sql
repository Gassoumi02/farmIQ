-- =====================================================
-- FarmIQ - Script SQL Complet Back-Office Admin
-- Version 2.0 avec gestion utilisateurs + transactions
-- =====================================================

CREATE DATABASE IF NOT EXISTS farmiq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE farmiq;

-- =====================================================
-- ROLES
-- =====================================================
CREATE TABLE IF NOT EXISTS roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB;

-- =====================================================
-- PERMISSIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS permissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB;

-- =====================================================
-- ROLE_PERMISSIONS (liaison)
-- =====================================================
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- USERS
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL DEFAULT 2,
    statut ENUM('ACTIF','INACTIF') DEFAULT 'ACTIF',
    photo_url VARCHAR(255),
    reset_token VARCHAR(255),
    reset_token_expiry DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    INDEX idx_email (email),
    INDEX idx_statut (statut),
    INDEX idx_role (role_id)
) ENGINE=InnoDB;

-- =====================================================
-- TRANSACTIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL DEFAULT 1,
    type ENUM('VENTE','ACHAT') NOT NULL,
    montant DECIMAL(10,2) NOT NULL CHECK (montant > 0),
    date DATE NOT NULL,
    description TEXT,
    statut ENUM('EN_ATTENTE','VALIDEE','ANNULEE') DEFAULT 'EN_ATTENTE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_type (type),
    INDEX idx_date (date),
    INDEX idx_statut (statut),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB;

-- =====================================================
-- DONNEES DE BASE
-- =====================================================

-- Rôles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrateur système avec tous les droits'),
('AGRICULTEUR', 'Utilisateur standard agriculteur - rôle par défaut'),
('TECHNICIEN', 'Technicien avec droits limités')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Permissions
INSERT INTO permissions (name, description) VALUES
('MANAGE_USERS', 'Gérer les utilisateurs'),
('MANAGE_PRODUCTS', 'Gérer les produits'),
('MANAGE_SUPPLIERS', 'Gérer les fournisseurs'),
('VIEW_REPORTS', 'Voir les rapports'),
('MANAGE_ROLES', 'Gérer les rôles et permissions'),
('DELETE_DATA', 'Supprimer des données'),
('EXPORT_DATA', 'Exporter des données'),
('IMPORT_DATA', 'Importer des données'),
('MANAGE_TRANSACTIONS', 'Gérer toutes les transactions')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Permissions ADMIN: toutes
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- Permissions AGRICULTEUR
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(2, 2), -- MANAGE_PRODUCTS
(2, 3), -- MANAGE_SUPPLIERS
(2, 4), -- VIEW_REPORTS
(2, 7); -- EXPORT_DATA

-- Permissions TECHNICIEN
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(3, 4); -- VIEW_REPORTS

-- =====================================================
-- ADMIN PAR DEFAUT  (mot de passe: Admin123!)
-- Hash BCrypt généré avec 12 rounds
-- =====================================================
INSERT INTO users (nom, email, password, role_id, statut) VALUES
('Administrateur', 'admin@farmiq.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILSj8kfpm', 1, 'ACTIF')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- Utilisateurs de test
INSERT INTO users (nom, email, password, role_id, statut) VALUES
('Ahmed Ben Ali', 'ahmed@farmiq.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILSj8kfpm', 2, 'ACTIF'),
('Fatma Mansour', 'fatma@farmiq.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILSj8kfpm', 2, 'ACTIF'),
('Karim Trabelsi', 'karim@farmiq.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILSj8kfpm', 3, 'INACTIF')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- =====================================================
-- TRANSACTIONS DE TEST
-- =====================================================
INSERT INTO transactions (user_id, type, montant, date, description, statut) VALUES
(1, 'VENTE', 1500.00, '2026-02-01', 'Vente de blé - Lot A', 'VALIDEE'),
(2, 'ACHAT', 800.00, '2026-02-02', 'Achat de semences de maïs', 'VALIDEE'),
(2, 'VENTE', 2300.00, '2026-02-05', 'Vente de tomates - Marché local', 'VALIDEE'),
(3, 'ACHAT', 450.00, '2026-02-08', 'Achat d\'engrais bio', 'VALIDEE'),
(1, 'VENTE', 1800.00, '2026-02-10', 'Vente d\'olives - Export', 'EN_ATTENTE'),
(2, 'ACHAT', 600.00, '2026-02-12', 'Achat de matériel agricole', 'VALIDEE'),
(3, 'VENTE', 950.00, '2026-02-14', 'Vente de fruits saisonniers', 'VALIDEE')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- =====================================================
-- VUES
-- =====================================================
CREATE OR REPLACE VIEW v_users_details AS
SELECT u.id, u.nom, u.email, u.statut, u.photo_url, u.created_at, u.updated_at,
       r.id as role_id, r.name as role_name, r.description as role_description,
       GROUP_CONCAT(p.name) as permissions
FROM users u
INNER JOIN roles r ON u.role_id = r.id
LEFT JOIN role_permissions rp ON r.id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.id
GROUP BY u.id, r.id;

CREATE OR REPLACE VIEW v_transactions_with_user AS
SELECT t.*, u.nom as user_nom, u.email as user_email, r.name as user_role
FROM transactions t
INNER JOIN users u ON t.user_id = u.id
INNER JOIN roles r ON u.role_id = r.id;

CREATE OR REPLACE VIEW v_transaction_stats AS
SELECT COUNT(*) as total_transactions,
       SUM(CASE WHEN type='VENTE' AND statut!='ANNULEE' THEN montant ELSE 0 END) as total_ventes,
       SUM(CASE WHEN type='ACHAT' AND statut!='ANNULEE' THEN montant ELSE 0 END) as total_achats,
       SUM(CASE WHEN type='VENTE' AND statut!='ANNULEE' THEN montant ELSE 0 END) -
       SUM(CASE WHEN type='ACHAT' AND statut!='ANNULEE' THEN montant ELSE 0 END) as benefice
FROM transactions;

-- =====================================================
-- TRIGGERS
-- =====================================================
DELIMITER //
CREATE TRIGGER IF NOT EXISTS before_user_update
BEFORE UPDATE ON users FOR EACH ROW
BEGIN SET NEW.updated_at = CURRENT_TIMESTAMP; END //

CREATE TRIGGER IF NOT EXISTS before_transaction_update
BEFORE UPDATE ON transactions FOR EACH ROW
BEGIN SET NEW.updated_at = CURRENT_TIMESTAMP; END //

-- PROCEDURE: Vérifier permissions
CREATE PROCEDURE IF NOT EXISTS sp_check_user_permission(
    IN p_user_id INT, IN p_permission_name VARCHAR(50), OUT p_has_permission BOOLEAN)
BEGIN
    SELECT COUNT(*) > 0 INTO p_has_permission
    FROM users u
    INNER JOIN role_permissions rp ON u.role_id = rp.role_id
    INNER JOIN permissions p ON rp.permission_id = p.id
    WHERE u.id = p_user_id AND p.name = p_permission_name;
END //

DELIMITER ;

SELECT '✅ FarmIQ Back-Office DB initialisée avec succès!' as Status;
SELECT 'Connexion admin: admin@farmiq.com / Admin123!' as Credentials;
