package com.farmiq.controllers;

import com.farmiq.models.Role;
import com.farmiq.models.User;
import com.farmiq.services.RoleService;
import com.farmiq.services.UserService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminUserController {
    private static final Logger logger = LogManager.getLogger(AdminUserController.class);

    // ===== TABLE =====
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatut;
    @FXML private TableColumn<User, LocalDateTime> colCreatedAt;
    @FXML private TableColumn<User, Void> colActions;

    // ===== TOOLBAR =====
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterRoleCombo;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnDesactiver;
    @FXML private Button btnRafraichir;

    // ===== STATS =====
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActifs;
    @FXML private Label lblInactifs;
    @FXML private Label lblAdmins;
    @FXML private Label statusLabel;
    @FXML private Label userNameLabel;

    // ===== PAGINATION =====
    @FXML private Pagination pagination;
    private static final int PAGE_SIZE = 20;

    // ===== DATA =====
    private UserService userService;
    private RoleService roleService;
    private ObservableList<User> allUsers;
    private FilteredList<User> filteredUsers;
    private SortedList<User> sortedUsers;

    @FXML
    public void initialize() {
        userService = new UserService();
        roleService = new RoleService();
        allUsers = FXCollections.observableArrayList();

        checkAdminAccess();
        setupTable();
        setupFilters();
        setupSearch();
        setupSorting();
        loadUsers();
        updateHeaderInfo();
    }

    private void checkAdminAccess() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError("Accès refusé", "Vous n'avez pas les droits pour accéder au Back-Office.");
            redirectToLogin();
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colRole.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getRole() != null ? cellData.getValue().getRole().getName() : "N/A"
            ));

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("badge-actif", "badge-inactif");
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("ACTIF".equals(item) ? "badge-actif" : "badge-inactif");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colCreatedAt.setCellFactory(col -> new TableCell<User, LocalDateTime>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(fmt));
            }
        });

        addActionsColumn();

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            btnModifier.setDisable(!selected);
            btnSupprimer.setDisable(!selected);
            btnDesactiver.setDisable(!selected);
        });

        filteredUsers = new FilteredList<>(allUsers, p -> true);
        sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedUsers);
    }

    private void addActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDel = new Button("🗑️");
            private final Button btnToggle = new Button("🔒");
            private final HBox box = new HBox(4, btnEdit, btnToggle, btnDel);

            {
                btnEdit.getStyleClass().add("btn-action-edit");
                btnDel.getStyleClass().add("btn-action-delete");
                btnToggle.getStyleClass().add("btn-action-toggle");

                btnEdit.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    openEditDialog(user);
                });
                btnDel.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDelete(user);
                });
                btnToggle.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleToggleStatus(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupFilters() {
        filterRoleCombo.getItems().addAll("Tous", "ADMIN", "AGRICULTEUR", "TECHNICIEN");
        filterRoleCombo.setValue("Tous");
        filterStatutCombo.getItems().addAll("Tous", "ACTIF", "INACTIF");
        filterStatutCombo.setValue("Tous");

        filterRoleCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterStatutCombo.valueProperty().addListener((obs, o, n) -> applyFilters());

        try {
            List<Role> roles = roleService.getAllRoles();
            filterRoleCombo.getItems().clear();
            filterRoleCombo.getItems().add("Tous");
            roles.forEach(r -> filterRoleCombo.getItems().add(r.getName()));
            filterRoleCombo.setValue("Tous");
        } catch (Exception e) {
            logger.warn("Impossible de charger les rôles pour le filtre", e);
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupSorting() {
        sortCombo.getItems().addAll("Nom A-Z", "Nom Z-A", "Date création (récent)", "Date création (ancien)");
        sortCombo.setValue("Nom A-Z");
        sortCombo.valueProperty().addListener((obs, o, n) -> applySorting());
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String roleFilter = filterRoleCombo.getValue();
        String statutFilter = filterStatutCombo.getValue();

        filteredUsers.setPredicate(user -> {
            boolean matchSearch = search.isEmpty()
                || user.getNom().toLowerCase().contains(search)
                || user.getEmail().toLowerCase().contains(search);
            boolean matchRole = "Tous".equals(roleFilter)
                || (user.getRole() != null && roleFilter.equals(user.getRole().getName()));
            boolean matchStatut = "Tous".equals(statutFilter)
                || statutFilter.equals(user.getStatut());
            return matchSearch && matchRole && matchStatut;
        });

        updateStatusLabel();
    }

    private void applySorting() {
        String sort = sortCombo.getValue();
        if (sort == null) return;
        switch (sort) {
            case "Nom A-Z":
                sortedUsers.setComparator((a, b) -> a.getNom().compareToIgnoreCase(b.getNom()));
                break;
            case "Nom Z-A":
                sortedUsers.setComparator((a, b) -> b.getNom().compareToIgnoreCase(a.getNom()));
                break;
            case "Date création (récent)":
                sortedUsers.setComparator((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                });
                break;
            case "Date création (ancien)":
                sortedUsers.setComparator((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return a.getCreatedAt().compareTo(b.getCreatedAt());
                });
                break;
        }
    }

    private void loadUsers() {
        setStatus("Chargement...");
        new Thread(() -> {
            try {
                List<User> users = userService.getAllUsers();
                Platform.runLater(() -> {
                    allUsers.setAll(users);
                    updateStats(users);
                    updateStatusLabel();
                    setStatus("✅ " + users.size() + " utilisateur(s) chargé(s)");
                    logger.info("Utilisateurs chargés: {}", users.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setStatus("❌ Erreur chargement");
                    AlertUtil.showError("Erreur", e.getMessage());
                    logger.error("Erreur chargement utilisateurs", e);
                });
            }
        }).start();
    }

    private void updateStats(List<User> users) {
        long actifs = users.stream().filter(u -> "ACTIF".equals(u.getStatut())).count();
        long inactifs = users.stream().filter(u -> "INACTIF".equals(u.getStatut())).count();
        long admins = users.stream().filter(u -> u.getRole() != null && "ADMIN".equals(u.getRole().getName())).count();
        lblTotalUsers.setText(String.valueOf(users.size()));
        lblActifs.setText(String.valueOf(actifs));
        lblInactifs.setText(String.valueOf(inactifs));
        lblAdmins.setText(String.valueOf(admins));
    }

    private void updateStatusLabel() {
        int total = filteredUsers.size();
        statusLabel.setText("Résultats: " + total + " utilisateur(s)");
    }

    private void updateHeaderInfo() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getNom());
        }
    }

    @FXML
    private void handleAjouter() {
        openAddDialog();
    }

    @FXML
    private void handleModifier() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner un utilisateur");
            return;
        }
        openEditDialog(selected);
    }

    @FXML
    private void handleSupprimer() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner un utilisateur");
            return;
        }
        handleDelete(selected);
    }

    @FXML
    private void handleDesactiver() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner un utilisateur");
            return;
        }
        handleToggleStatus(selected);
    }

    @FXML
    private void handleRafraichir() {
        searchField.clear();
        filterRoleCombo.setValue("Tous");
        filterStatutCombo.setValue("Tous");
        loadUsers();
    }

    private void handleDelete(User user) {
        if (user == null) return;
        String currentUserEmail = SessionManager.getInstance().getCurrentUser().getEmail();
        if (user.getEmail().equals(currentUserEmail)) {
            AlertUtil.showError("Action interdite", "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
            "Confirmation de suppression",
            "Voulez-vous supprimer l'utilisateur '" + user.getNom() + "' ?\n" +
            "Cette action est irréversible."
        );
        if (!confirmed) return;

        try {
            userService.deleteUser(user.getId());
            allUsers.remove(user);
            setStatus("✅ Utilisateur supprimé: " + user.getNom());
            AlertUtil.showSuccess("Succès", "Utilisateur supprimé avec succès.");
        } catch (Exception e) {
            AlertUtil.showError("Erreur suppression", e.getMessage());
            logger.error("Erreur suppression utilisateur", e);
        }
    }

    private void handleToggleStatus(User user) {
        if (user == null) return;
        String currentUserEmail = SessionManager.getInstance().getCurrentUser().getEmail();
        if (user.getEmail().equals(currentUserEmail)) {
            AlertUtil.showError("Action interdite", "Vous ne pouvez pas modifier votre propre statut.");
            return;
        }

        String action = "ACTIF".equals(user.getStatut()) ? "désactiver" : "activer";
        boolean confirmed = AlertUtil.showConfirmation(
            "Confirmation",
            "Voulez-vous " + action + " le compte de '" + user.getNom() + "' ?"
        );
        if (!confirmed) return;

        try {
            userService.toggleUserStatus(user.getId(), user.getStatut());
            String newStatut = "ACTIF".equals(user.getStatut()) ? "INACTIF" : "ACTIF";
            user.setStatut(newStatut);
            usersTable.refresh();
            updateStats(allUsers);
            setStatus("✅ Statut modifié: " + user.getNom() + " → " + newStatut);
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/UserFormDialog.fxml"));
            Parent root = loader.load();
            UserFormController controller = loader.getController();
            controller.setMode(UserFormController.Mode.ADD);
            controller.setOnSaveCallback(this::loadUsers);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un utilisateur");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            logger.error("Erreur ouverture formulaire ajout", e);
        }
    }

    private void openEditDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/UserFormDialog.fxml"));
            Parent root = loader.load();
            UserFormController controller = loader.getController();
            controller.setMode(UserFormController.Mode.EDIT);
            controller.setUser(user);
            controller.setOnSaveCallback(this::loadUsers);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            logger.error("Erreur ouverture formulaire édition", e);
        }
    }

    @FXML
    private void handleGoTransactions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/AdminLayout.fxml"));
            Parent root = loader.load();
            AdminLayoutController ctrl = loader.getController();
            ctrl.showTransactions();
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
        } catch (IOException e) {
            logger.error("Erreur navigation vers transactions", e);
        }
    }

    @FXML
    private void handleDeconnexion() {
        if (AlertUtil.showConfirmation("Déconnexion", "Voulez-vous vous déconnecter ?")) {
            SessionManager.getInstance().logout();
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (usersTable != null ? usersTable.getScene().getWindow() : null);
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("FarmIQ - Connexion");
            }
        } catch (Exception e) {
            logger.error("Erreur redirection login", e);
        }
    }

    private void setStatus(String message) {
        if (statusLabel != null) statusLabel.setText(message);
    }
}
