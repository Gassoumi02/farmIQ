-- =====================================================
-- FarmIQ V3 - Marketplace & Extended Features
-- =====================================================
USE farmiq;

-- =====================================================
-- SELLER PROFILES
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
    INDEX idx_prix (prix),
    FULLTEXT INDEX ft_search (titre, description)
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
-- CART
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
-- PAYMENTS
-- =====================================================
CREATE TABLE IF NOT EXISTS payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    methode ENUM('ESPECES','VIREMENT','CARTE','PAIEMENT_A_LIVRAISON') NOT NULL,
    statut ENUM('EN_ATTENTE','COMPLETE','ECHOUE','REMBOURSE') DEFAULT 'EN_ATTENTE',
    montant DECIMAL(10,2) NOT NULL,
    reference VARCHAR(100),
    notes TEXT,
    paid_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- REVIEWS
-- =====================================================
CREATE TABLE IF NOT EXISTS reviews (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    seller_id INT NOT NULL,
    note INT NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire TEXT,
    statut ENUM('EN_ATTENTE','APPROUVE','REJETE') DEFAULT 'EN_ATTENTE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES seller_profiles(id),
    UNIQUE KEY unique_review (order_id, reviewer_id)
) ENGINE=InnoDB;

-- =====================================================
-- PRODUCTS (inventory)
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(150) NOT NULL,
    categorie ENUM('SEMENCE','ENGRAIS','PESTICIDE','RECOLTE','MATERIEL','AUTRE') NOT NULL,
    quantite DECIMAL(10,2) DEFAULT 0,
    unite VARCHAR(30),
    prix_unitaire DECIMAL(10,2),
    seuil_alerte DECIMAL(10,2) DEFAULT 10,
    fournisseur_id INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_categorie (categorie)
) ENGINE=InnoDB;

-- =====================================================
-- FOURNISSEURS
-- =====================================================
CREATE TABLE IF NOT EXISTS fournisseurs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(150) NOT NULL,
    contact VARCHAR(100),
    telephone VARCHAR(20),
    email VARCHAR(150),
    adresse TEXT,
    statut ENUM('ACTIF','INACTIF') DEFAULT 'ACTIF',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_statut (statut)
) ENGINE=InnoDB;

ALTER TABLE products ADD FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id) ON DELETE SET NULL;

-- =====================================================
-- NOTIFICATIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    titre VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('INFO','ALERTE','SUCCES','ERREUR','COMMANDE','PAIEMENT') DEFAULT 'INFO',
    lu BOOLEAN DEFAULT FALSE,
    lien VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_lu (user_id, lu)
) ENGINE=InnoDB;

-- =====================================================
-- TACHES (calendar)
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
-- VIEWS
-- =====================================================
CREATE OR REPLACE VIEW v_listings_full AS
SELECT l.*,
       sp.nom_boutique, sp.wilaya as seller_wilaya, sp.note_moyenne,
       u.nom as seller_nom, u.email as seller_email,
       li.image_url as image_principale,
       COUNT(DISTINCT r.id) as total_reviews
FROM listings l
INNER JOIN seller_profiles sp ON l.seller_id = sp.id
INNER JOIN users u ON sp.user_id = u.id
LEFT JOIN listing_images li ON l.id = li.listing_id AND li.is_principale = TRUE
LEFT JOIN reviews r ON sp.id = r.seller_id AND r.statut = 'APPROUVE'
GROUP BY l.id, sp.id, u.id, li.image_url;

CREATE OR REPLACE VIEW v_orders_full AS
SELECT o.*,
       u.nom as buyer_nom, u.email as buyer_email,
       sp.nom_boutique,
       p.statut as payment_statut, p.methode as payment_methode
FROM orders o
INNER JOIN users u ON o.buyer_id = u.id
INNER JOIN seller_profiles sp ON o.seller_id = sp.id
LEFT JOIN payments p ON o.id = p.order_id;

CREATE OR REPLACE VIEW v_seller_stats AS
SELECT sp.id as seller_id, sp.nom_boutique,
       COUNT(DISTINCT o.id) as total_commandes,
       SUM(CASE WHEN o.statut = 'LIVRE' THEN o.montant_total ELSE 0 END) as revenus_total,
       AVG(r.note) as note_moyenne,
       COUNT(DISTINCT l.id) as total_listings
FROM seller_profiles sp
LEFT JOIN orders o ON sp.id = o.seller_id
LEFT JOIN reviews r ON sp.id = r.seller_id AND r.statut = 'APPROUVE'
LEFT JOIN listings l ON sp.id = l.seller_id AND l.statut = 'ACTIF'
GROUP BY sp.id;

-- =====================================================
-- NEW PERMISSIONS
-- =====================================================
INSERT IGNORE INTO permissions (name, description) VALUES
('MANAGE_PRODUCTS', 'Gérer les produits et inventaire'),
('MANAGE_SUPPLIERS', 'Gérer les fournisseurs'),
('MANAGE_TASKS', 'Gérer les tâches du calendrier'),
('VIEW_DASHBOARD', 'Voir le tableau de bord'),
('EXPORT_PDF', 'Exporter des rapports PDF'),
('MARKETPLACE_SELL', 'Vendre sur le marketplace'),
('MARKETPLACE_BUY', 'Acheter sur le marketplace'),
('MANAGE_ORDERS', 'Gérer les commandes'),
('MANAGE_LISTINGS', 'Gérer les annonces'),
('MANAGE_MARKETPLACE', 'Administration complète du marketplace');

-- Assign permissions to ADMIN role
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- Assign permissions to AGRICULTEUR role
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions
WHERE name IN ('MANAGE_PRODUCTS','MANAGE_SUPPLIERS','MANAGE_TASKS',
               'VIEW_DASHBOARD','EXPORT_PDF','MARKETPLACE_SELL',
               'MARKETPLACE_BUY','MANAGE_ORDERS','MANAGE_LISTINGS');

-- Assign permissions to TECHNICIEN role
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE name IN ('VIEW_DASHBOARD','MANAGE_TASKS','MARKETPLACE_BUY');

-- =====================================================
-- SAMPLE DATA
-- =====================================================
INSERT IGNORE INTO fournisseurs (nom, contact, telephone, email, statut) VALUES
('AgriSemences Tunisie', 'Mehdi Sassi', '+216 71 234 567', 'contact@agrisemences.tn', 'ACTIF'),
('Engrais Bio Nord', 'Leila Hammami', '+216 72 345 678', 'info@engraisbio.tn', 'ACTIF'),
('Matériel Agricole SA', 'Nizar Jebali', '+216 73 456 789', 'vente@materielagri.tn', 'ACTIF');

-- Create seller profiles for existing users (assuming user IDs 2 and 3 exist)
INSERT IGNORE INTO seller_profiles (user_id, nom_boutique, description, wilaya, statut) VALUES
(2, 'Ferme Ben Ali', 'Producteur de céréales et légumes bio', 'Bizerte', 'APPROUVE'),
(3, 'Jardins de Fatma', 'Spécialiste fruits et légumes saisonniers', 'Nabeul', 'APPROUVE');

-- Sample listings
INSERT IGNORE INTO listings (seller_id, titre, description, categorie, prix, unite, quantite_disponible, wilaya, statut) VALUES
(1, 'Blé Dur Premium - Récolte 2025', 'Blé dur de haute qualité, idéal pour semoule et pâtes', 'RECOLTE', 85.00, 'quintal', 200, 'Bizerte', 'ACTIF'),
(1, 'Semences de Maïs Hybride', 'Semences certifiées, haut rendement, résistantes à la sécheresse', 'SEMENCE', 45.00, 'kg', 500, 'Bizerte', 'ACTIF'),
(2, 'Tomates Roma Fraîches', 'Tomates cultivées sans pesticides chimiques', 'RECOLTE', 1.20, 'kg', 1000, 'Nabeul', 'ACTIF'),
(2, 'Engrais Naturel Compost', 'Compost 100% organique, enrichi NPK', 'ENGRAIS', 25.00, 'sac 50kg', 100, 'Nabeul', 'ACTIF');

SELECT '✅ FarmIQ V3 Migration complète!' as Status;
