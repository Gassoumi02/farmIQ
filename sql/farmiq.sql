-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : dim. 15 fév. 2026 à 22:37
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `farmiq`
--

CREATE DATABASE IF NOT EXISTS farmiq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE farmiq;

DELIMITER $$
--
-- Procédures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_check_user_permission` (IN `p_user_id` INT, IN `p_permission_name` VARCHAR(50), OUT `p_has_permission` BOOLEAN)   BEGIN
    SELECT COUNT(*) > 0 INTO p_has_permission
    FROM users u
    INNER JOIN role_permissions rp ON u.role_id = rp.role_id
    INNER JOIN permissions p ON rp.permission_id = p.id
    WHERE u.id = p_user_id AND p.name = p_permission_name;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `parcelle`
--

CREATE TABLE `parcelle` (
  `idParcelle` int(11) NOT NULL,
  `nomParcelle` varchar(100) NOT NULL,
  `surface` double NOT NULL,
  `localisation` varchar(100) DEFAULT NULL,
  `typeSol` varchar(50) DEFAULT NULL,
  `etatParcelle` varchar(50) DEFAULT NULL,
  `dateDerniereCulture` date DEFAULT NULL,
  `irrigation` tinyint(1) DEFAULT 0,
  `remarques` text DEFAULT NULL,
  `idPlante` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'Propriétaire de la parcelle'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `parcelle`
--

INSERT INTO `parcelle` (`idParcelle`, `nomParcelle`, `surface`, `localisation`, `typeSol`, `etatParcelle`, `dateDerniereCulture`, `irrigation`, `remarques`, `idPlante`, `user_id`) VALUES
(3, 'BBB', 2333, 'SUD', 'AGILEUX', 'libre', '2026-02-15', 0, 'AAAA', NULL, NULL),
(4, 'AAhh', 34, 'NORD', 'AIGUX', 'en repos', '2026-02-15', 0, 'AAAA', 8, NULL),
(5, 'RR', 24, '', '', 'libre', '2025-02-06', 0, 'AAA', NULL, NULL),
(6, 'bbbb', 222, 'mmm', 'AAA', 'libre', '2024-01-31', 0, 'AAAA', NULL, NULL),
(8, 'OIDIJDI', 250, 'djerba', 'argileux', 'en repos', '2026-02-15', 1, '', 6, NULL),
(9, 'test', 10, 'test', 'test1', 'en culture', '2026-02-15', 1, 'test', 10, NULL),
(10, 'test', 10, 'test', 'test10', 'disponible', '2026-02-15', 1, 'test', 10, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `permissions`
--

CREATE TABLE `permissions` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `permissions`
--

INSERT INTO `permissions` (`id`, `name`, `description`, `created_at`) VALUES
(1, 'MANAGE_USERS', 'Gérer les utilisateurs', '2026-02-15 20:53:21'),
(2, 'MANAGE_PRODUCTS', 'Gérer les produits', '2026-02-15 20:53:21'),
(3, 'MANAGE_SUPPLIERS', 'Gérer les fournisseurs', '2026-02-15 20:53:21'),
(4, 'VIEW_REPORTS', 'Voir les rapports', '2026-02-15 20:53:21'),
(5, 'MANAGE_ROLES', 'Gérer les rôles et permissions', '2026-02-15 20:53:21'),
(6, 'DELETE_DATA', 'Supprimer des données', '2026-02-15 20:53:21'),
(7, 'EXPORT_DATA', 'Exporter des données', '2026-02-15 20:53:21'),
(8, 'IMPORT_DATA', 'Importer des données', '2026-02-15 20:53:21'),
(9, 'MANAGE_TRANSACTIONS', 'Gérer toutes les transactions', '2026-02-15 20:53:21');

-- --------------------------------------------------------

--
-- Structure de la table `plante`
--

CREATE TABLE `plante` (
  `idPlante` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `datePlantation` date NOT NULL,
  `dateRecoltePrevue` date NOT NULL,
  `quantite` int(11) NOT NULL,
  `etat` varchar(50) NOT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `plante`
--

INSERT INTO `plante` (`idPlante`, `nom`, `type`, `datePlantation`, `dateRecoltePrevue`, `quantite`, `etat`, `user_id`) VALUES
(2, 'AABB', 'BBBBBBBBB', '2026-02-13', '2026-03-19', 333, 'en croissance', NULL),
(5, 'HH', 'HHHH', '2026-02-15', '2026-03-17', 444, 'en croissance', NULL),
(6, 'azerty3', 'AAA', '2024-02-01', '2026-03-17', 13, 'prête à récolter', NULL),
(8, 'mm', 'fruit', '2026-01-27', '2026-03-07', 45, 'en croissance', NULL),
(10, 'monstera', 'cac', '2026-02-15', '2026-03-17', 50, 'récoltée', 6),
(12, 'test', 'test10', '2026-02-15', '2026-03-17', 10, 'semée', 6);

-- --------------------------------------------------------

--
-- Structure de la table `roles`
--

CREATE TABLE `roles` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `roles`
--

INSERT INTO `roles` (`id`, `name`, `description`, `created_at`) VALUES
(1, 'ADMIN', 'Administrateur système avec tous les droits', '2026-02-15 20:53:20'),
(2, 'AGRICULTEUR', 'Utilisateur standard agriculteur - rôle par défaut', '2026-02-15 20:53:20'),
(3, 'TECHNICIEN', 'Technicien avec droits limités', '2026-02-15 20:53:20');

-- --------------------------------------------------------

--
-- Structure de la table `role_permissions`
--

CREATE TABLE `role_permissions` (
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `role_permissions`
--

INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(2, 2),
(2, 3),
(2, 4),
(2, 7),
(3, 4);

-- --------------------------------------------------------

--
-- Structure de la table `transactions`
--

CREATE TABLE `transactions` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT 1,
  `type` enum('VENTE','ACHAT') NOT NULL,
  `montant` decimal(10,2) NOT NULL CHECK (`montant` > 0),
  `date` date NOT NULL,
  `description` text DEFAULT NULL,
  `statut` enum('EN_ATTENTE','VALIDEE','ANNULEE') DEFAULT 'EN_ATTENTE',
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `transactions`
--

INSERT INTO `transactions` (`id`, `user_id`, `type`, `montant`, `date`, `description`, `statut`, `created_at`, `updated_at`) VALUES
(1, 1, 'VENTE', 1500.00, '2026-02-01', 'Vente de blé - Lot A', 'VALIDEE', '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(2, 2, 'ACHAT', 800.00, '2026-02-02', 'Achat de semences de maïs', 'VALIDEE', '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(3, 2, 'VENTE', 2300.00, '2026-02-05', 'Vente de tomates - Marché local', 'VALIDEE', '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(4, 3, 'ACHAT', 450.00, '2026-02-08', 'Achat d\'engrais bio', 'VALIDEE', '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(5, 1, 'VENTE', 1800.00, '2026-02-10', 'Vente d\'olives - Export', 'EN_ATTENTE', '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(6, 2, 'ACHAT', 600.00, '2026-02-12', 'Achat de matériel agricole', 'VALIDEE', '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(7, 3, 'VENTE', 950.00, '2026-02-14', 'Vente de fruits saisonniers', 'VALIDEE', '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(9, 6, 'VENTE', 1000.00, '2026-02-15', 'test', 'EN_ATTENTE', '2026-02-15 22:35:07', '2026-02-15 22:35:07');

--
-- Déclencheurs `transactions`
--
DELIMITER $$
CREATE TRIGGER `before_transaction_update` BEFORE UPDATE ON `transactions` FOR EACH ROW BEGIN SET NEW.updated_at = CURRENT_TIMESTAMP; END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role_id` int(11) NOT NULL DEFAULT 2,
  `statut` enum('ACTIF','INACTIF') DEFAULT 'ACTIF',
  `photo_url` varchar(255) DEFAULT NULL,
  `reset_token` varchar(255) DEFAULT NULL,
  `reset_token_expiry` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `nom`, `email`, `password`, `role_id`, `statut`, `photo_url`, `reset_token`, `reset_token_expiry`, `created_at`, `updated_at`) VALUES
(1, 'Administrateur', 'admin@farmiq.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILSj8kfpm', 1, 'ACTIF', NULL, NULL, NULL, '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(2, 'Ahmed Ben Ali', 'ahmed@farmiq.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILSj8kfpm', 2, 'ACTIF', NULL, NULL, NULL, '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(3, 'Fatma Mansour', 'fatma@farmiq.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILSj8kfpm', 2, 'ACTIF', NULL, NULL, NULL, '2026-02-15 20:53:21', '2026-02-15 20:53:21'),
(4, 'Karim Trabelsi', 'karim@farmiq.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILSj8kfpm', 3, 'INACTIF', NULL, NULL, NULL, '2026-02-15 20:53:21', '2026-02-15 20:57:15'),
(5, 'hamza', 'hamza@gmail.com', '$2a$12$MTSzd6kKMU.mdrgcO2IEbu/NH3qSjr4pl9nbQNhM9Ge1BGrrMAO7O', 1, 'ACTIF', NULL, NULL, NULL, '2026-02-15 20:55:47', '2026-02-15 20:56:00'),
(6, 'omar', 'omar@gmail.com', '$2a$12$BJSR2VdeH/jyfzP2vjwmxONACLsKv6sB7jV.TcyTwaDICTXQd2.w.', 2, 'ACTIF', NULL, NULL, NULL, '2026-02-15 20:57:54', '2026-02-15 20:57:54');

--
-- Déclencheurs `users`
--
DELIMITER $$
CREATE TRIGGER `before_user_update` BEFORE UPDATE ON `users` FOR EACH ROW BEGIN SET NEW.updated_at = CURRENT_TIMESTAMP; END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Doublure de structure pour la vue `v_transactions_with_user`
-- (Voir ci-dessous la vue réelle)
--
CREATE TABLE `v_transactions_with_user` (
`id` int(11)
,`user_id` int(11)
,`type` enum('VENTE','ACHAT')
,`montant` decimal(10,2)
,`date` date
,`description` text
,`statut` enum('EN_ATTENTE','VALIDEE','ANNULEE')
,`created_at` datetime
,`updated_at` datetime
,`user_nom` varchar(100)
,`user_email` varchar(150)
,`user_role` varchar(50)
);

-- --------------------------------------------------------

--
-- Doublure de structure pour la vue `v_transaction_stats`
-- (Voir ci-dessous la vue réelle)
--
CREATE TABLE `v_transaction_stats` (
`total_transactions` bigint(21)
,`total_ventes` decimal(32,2)
,`total_achats` decimal(32,2)
,`benefice` decimal(33,2)
);

-- --------------------------------------------------------

--
-- Doublure de structure pour la vue `v_users_details`
-- (Voir ci-dessous la vue réelle)
--
CREATE TABLE `v_users_details` (
`id` int(11)
,`nom` varchar(100)
,`email` varchar(150)
,`statut` enum('ACTIF','INACTIF')
,`photo_url` varchar(255)
,`created_at` datetime
,`updated_at` datetime
,`role_id` int(11)
,`role_name` varchar(50)
,`role_description` varchar(255)
,`permissions` mediumtext
);

-- --------------------------------------------------------

--
-- Structure de la vue `v_transactions_with_user`
--
DROP TABLE IF EXISTS `v_transactions_with_user`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_transactions_with_user`  AS SELECT `t`.`id` AS `id`, `t`.`user_id` AS `user_id`, `t`.`type` AS `type`, `t`.`montant` AS `montant`, `t`.`date` AS `date`, `t`.`description` AS `description`, `t`.`statut` AS `statut`, `t`.`created_at` AS `created_at`, `t`.`updated_at` AS `updated_at`, `u`.`nom` AS `user_nom`, `u`.`email` AS `user_email`, `r`.`name` AS `user_role` FROM ((`transactions` `t` join `users` `u` on(`t`.`user_id` = `u`.`id`)) join `roles` `r` on(`u`.`role_id` = `r`.`id`)) ;

-- --------------------------------------------------------

--
-- Structure de la vue `v_transaction_stats`
--
DROP TABLE IF EXISTS `v_transaction_stats`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_transaction_stats`  AS SELECT count(0) AS `total_transactions`, sum(case when `transactions`.`type` = 'VENTE' and `transactions`.`statut` <> 'ANNULEE' then `transactions`.`montant` else 0 end) AS `total_ventes`, sum(case when `transactions`.`type` = 'ACHAT' and `transactions`.`statut` <> 'ANNULEE' then `transactions`.`montant` else 0 end) AS `total_achats`, sum(case when `transactions`.`type` = 'VENTE' and `transactions`.`statut` <> 'ANNULEE' then `transactions`.`montant` else 0 end) - sum(case when `transactions`.`type` = 'ACHAT' and `transactions`.`statut` <> 'ANNULEE' then `transactions`.`montant` else 0 end) AS `benefice` FROM `transactions` ;

-- --------------------------------------------------------

--
-- Structure de la vue `v_users_details`
--
DROP TABLE IF EXISTS `v_users_details`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_users_details`  AS SELECT `u`.`id` AS `id`, `u`.`nom` AS `nom`, `u`.`email` AS `email`, `u`.`statut` AS `statut`, `u`.`photo_url` AS `photo_url`, `u`.`created_at` AS `created_at`, `u`.`updated_at` AS `updated_at`, `r`.`id` AS `role_id`, `r`.`name` AS `role_name`, `r`.`description` AS `role_description`, group_concat(`p`.`name` separator ',') AS `permissions` FROM (((`users` `u` join `roles` `r` on(`u`.`role_id` = `r`.`id`)) left join `role_permissions` `rp` on(`r`.`id` = `rp`.`role_id`)) left join `permissions` `p` on(`rp`.`permission_id` = `p`.`id`)) GROUP BY `u`.`id`, `r`.`id` ;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `parcelle`
--
ALTER TABLE `parcelle`
  ADD PRIMARY KEY (`idParcelle`),
  ADD KEY `fk_parcelle_plante` (`idPlante`),
  ADD KEY `idx_parcelle_user_id` (`user_id`);

--
-- Index pour la table `permissions`
--
ALTER TABLE `permissions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`),
  ADD KEY `idx_name` (`name`);

--
-- Index pour la table `plante`
--
ALTER TABLE `plante`
  ADD PRIMARY KEY (`idPlante`),
  ADD KEY `idx_plante_user` (`user_id`);

--
-- Index pour la table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`),
  ADD KEY `idx_name` (`name`);

--
-- Index pour la table `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD PRIMARY KEY (`role_id`,`permission_id`),
  ADD KEY `permission_id` (`permission_id`);

--
-- Index pour la table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_type` (`type`),
  ADD KEY `idx_date` (`date`),
  ADD KEY `idx_statut` (`statut`),
  ADD KEY `idx_user_id` (`user_id`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_statut` (`statut`),
  ADD KEY `idx_role` (`role_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `parcelle`
--
ALTER TABLE `parcelle`
  MODIFY `idParcelle` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT pour la table `permissions`
--
ALTER TABLE `permissions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pour la table `plante`
--
ALTER TABLE `plante`
  MODIFY `idPlante` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `roles`
--
ALTER TABLE `roles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `parcelle`
--
ALTER TABLE `parcelle`
  ADD CONSTRAINT `fk_parcelle_plante` FOREIGN KEY (`idPlante`) REFERENCES `plante` (`idPlante`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_parcelle_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Contraintes pour la table `plante`
--
ALTER TABLE `plante`
  ADD CONSTRAINT `fk_plante_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Contraintes pour la table `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
