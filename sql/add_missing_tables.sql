-- =====================================================
-- FarmIQ - Missing Tables Script
-- Run this in phpMyAdmin after importing the main dump
-- to enable Marketplace, Calendar, and Cart features.
-- =====================================================

USE farmiq;

-- =====================================================
-- SELLER PROFILES (Required for marketplace listings)
-- =====================================================
CREATE TABLE IF NOT EXISTS seller_profiles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    nom_boutique VARCHAR(200) NOT NULL,
    description TEXT,
    wilaya VARCHAR(100),
    adresse TEXT,
    telephone VARCHAR(20),
    logo_url VARCHAR(255),
    note_moyenne DECIMAL(3,2) DEFAULT 0,
    total_ventes INT DEFAULT 0,
    statut ENUM('EN_ATTENTE','APPROUVE','SUSPENDU') DEFAULT 'EN_ATTENTE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_statut (statut),
    INDEX idx_wilaya (wilaya)
) ENGINE=InnoDB;

-- =====================================================
-- MARKETPLACE LISTINGS
-- =====================================================
CREATE TABLE IF NOT EXISTS listings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    seller_id INT NOT NULL,
    titre VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    categorie ENUM('SEMENCE','ENGRAIS','PESTICIDE','RECOLTE','MATERIEL','BETAIL','AUTRE') NOT NULL,
    prix DECIMAL(10,2) NOT NULL CHECK (prix > 0),
    unite VARCHAR(30) NOT NULL,
    quantite_disponible DECIMAL(10,2) NOT NULL DEFAULT 0,
    quantite_minimum DECIMAL(10,2) DEFAULT 1,
    wilaya VARCHAR(100),
    statut ENUM('BROUILLON','ACTIF','PAUSE','VENDU','EXPIRE') DEFAULT 'BROUILLON',
    vues INT DEFAULT 0,
    date_expiration DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES seller_profiles(id) ON DELETE CASCADE,
    INDEX idx_categorie (categorie),
    INDEX idx_statut (statut),
    INDEX idx_wilaya (wilaya),
    INDEX idx_prix (prix)
) ENGINE=InnoDB;

-- =====================================================
-- LISTING IMAGES
-- =====================================================
CREATE TABLE IF NOT EXISTS listing_images (
    id INT PRIMARY KEY AUTO_INCREMENT,
    listing_id INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_principale BOOLEAN DEFAULT FALSE,
    ordre INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- CART ITEMS
-- =====================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    listing_id INT NOT NULL,
    quantite DECIMAL(10,2) NOT NULL DEFAULT 1,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_item (user_id, listing_id)
) ENGINE=InnoDB;

-- =====================================================
-- ORDERS
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    buyer_id INT NOT NULL,
    seller_id INT NOT NULL,
    statut ENUM('EN_ATTENTE','CONFIRME','EN_PREPARATION','EXPEDIE','LIVRE','ANNULE','REMBOURSE') DEFAULT 'EN_ATTENTE',
    montant_total DECIMAL(10,2) NOT NULL,
    frais_livraison DECIMAL(10,2) DEFAULT 0,
    adresse_livraison TEXT,
    notes TEXT,
    code_suivi VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES seller_profiles(id),
    INDEX idx_buyer (buyer_id),
    INDEX idx_seller (seller_id),
    INDEX idx_statut (statut)
) ENGINE=InnoDB;

-- =====================================================
-- ORDER ITEMS
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    listing_id INT NOT NULL,
    quantite DECIMAL(10,2) NOT NULL,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    sous_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (listing_id) REFERENCES listings(id)
) ENGINE=InnoDB;

-- =====================================================
-- TACHES (Calendar tasks)
-- =====================================================
CREATE TABLE IF NOT EXISTS taches (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    date_debut DATE NOT NULL,
    date_fin DATE,
    priorite ENUM('BASSE','MOYENNE','HAUTE','URGENTE') DEFAULT 'MOYENNE',
    statut ENUM('A_FAIRE','EN_COURS','TERMINE','ANNULE') DEFAULT 'A_FAIRE',
    type ENUM('PLANTATION','RECOLTE','IRRIGATION','TRAITEMENT','MAINTENANCE','AUTRE') DEFAULT 'AUTRE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_date (date_debut),
    INDEX idx_statut (statut)
) ENGINE=InnoDB;

-- =====================================================
-- SAMPLE DATA (safe with INSERT IGNORE)
-- =====================================================

-- Seller profile for admin user
INSERT IGNORE INTO seller_profiles (user_id, nom_boutique, description, wilaya, statut) 
VALUES (1, 'Boutique FarmIQ Admin', 'Boutique principale FarmIQ', 'Tunis', 'APPROUVE');

-- Sample listings
INSERT IGNORE INTO listings (seller_id, titre, description, categorie, prix, unite, quantite_disponible, wilaya, statut) VALUES
(1, 'Blé Dur Premium - Récolte 2025', 'Blé dur de haute qualité, idéal pour semoule et pâtes', 'RECOLTE', 85.00, 'quintal', 200, 'Bizerte', 'ACTIF'),
(1, 'Semences de Pomme de Terre Spunta', 'Semences certifiées, variété Spunta - rendement élevé', 'SEMENCE', 120.00, 'quintal', 50, 'Kef', 'ACTIF'),
(1, 'Engrais Organique Premium', 'Engrais naturel riche en azote et phosphore', 'ENGRAIS', 45.00, 'sac', 100, 'Sfax', 'ACTIF'),
(1, 'Tomates Roma - Production locale', 'Tomates Roma fraîches de production locale', 'RECOLTE', 1.20, 'kg', 500, 'Nabeul', 'ACTIF'),
(1, 'Pesticide Bio Neem', 'Pesticide biologique à base de neem, sans résidus', 'PESTICIDE', 35.00, 'litre', 80, 'Tunis', 'ACTIF');

SELECT 'Marketplace tables created successfully!' AS result;
