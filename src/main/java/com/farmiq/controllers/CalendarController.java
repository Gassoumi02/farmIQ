package com.farmiq.controllers;

import com.farmiq.models.Tache;
import com.farmiq.models.User;
import com.farmiq.services.TacheService;
import com.farmiq.utils.AlertUtil;
import com.farmiq.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller pour le calendrier agricole
 */
public class CalendarController implements Initializable {
    
    private static final Logger logger = LogManager.getLogger(CalendarController.class);
    
    // Service
    private final TacheService tacheService = new TacheService();
    
    // UI Elements
    @FXML private Label lblMonthYear;
    @FXML private GridPane gridCalendar;
    @FXML private VBox vboxTasks;
    @FXML private Label lblSelectedDate;
    @FXML private Button btnPrevMonth;
    @FXML private Button btnNextMonth;
    @FXML private Button btnToday;
    @FXML private Button btnAddTask;
    
    // Data
    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private Map<LocalDate, List<Tache>> tasksByDate = new HashMap<>();
    private ObservableList<Tache> selectedDateTasks = FXCollections.observableArrayList();
    
    // Seasonal suggestions for Tunisia
    private static final Map<Integer, String[]> SEASONAL_TIPS;
    static {
        SEASONAL_TIPS = new HashMap<>();
        SEASONAL_TIPS.put(1, new String[]{"Semis d'hiver: Pois chiche, Lentille"});
        SEASONAL_TIPS.put(2, new String[]{"Semis: Fève, Pois", " Préparation du sol"});
        SEASONAL_TIPS.put(3, new String[]{"Plantation: Tomate, Poivron", " Irrigation commence"});
        SEASONAL_TIPS.put(4, new String[]{"Semis: Haricot, Concombre", " Traitement phytosanitaire"});
        SEASONAL_TIPS.put(5, new String[]{"Récolte: Fraise", " Surveillance parasites"});
        SEASONAL_TIPS.put(6, new String[]{"Récolte: Pomme de terre", " Irrigation intensive"});
        SEASONAL_TIPS.put(7, new String[]{"Récolte: Tomate, Poivron", " Conservation"});
        SEASONAL_TIPS.put(8, new String[]{"Récolte: Melon, Pastèque", " Préparation semis"});
        SEASONAL_TIPS.put(9, new String[]{"Semis d'automne: Chou, Carotte", " Labour"});
        SEASONAL_TIPS.put(10, new String[]{"Semis: Ail, Oignon", " Plantation oliviers"});
        SEASONAL_TIPS.put(11, new String[]{"Récolte: Olivier", " Taille des arbres"});
        SEASONAL_TIPS.put(12, new String[]{"Entretien: Serres", " Planning annuel"});
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du CalendarController");
        
        // Verify session
        if (!SessionManager.getInstance().isLoggedIn()) {
            logger.warn("Utilisateur non connecté");
            return;
        }
        
        // Initialize calendar
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        
        // Initialize calendar grid
        initializeCalendarGrid();
        
        // Load tasks
        loadTasksForMonth();
        
        // Display current month
        updateMonthDisplay();
        updateSelectedDateDisplay();
    }
    
    private void initializeCalendarGrid() {
        // Create day labels
        String[] dayNames = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        
        for (int i = 0; i < 7; i++) {
            Label lblDay = new Label(dayNames[i]);
            lblDay.setFont(Font.font("System", FontWeight.BOLD, 12));
            lblDay.setAlignment(Pos.CENTER);
            lblDay.setPrefWidth(80);
            lblDay.setPrefHeight(30);
            lblDay.setStyle("-fx-background-color: #E8F5E9;");
            gridCalendar.add(lblDay, i, 0);
        }
        
        // Create 6 rows for calendar cells
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                VBox cell = createCalendarCell(row, col);
                gridCalendar.add(cell, col, row);
            }
        }
    }
    
    private VBox createCalendarCell(int row, int col) {
        VBox cell = new VBox(2);
        cell.setPrefWidth(80);
        cell.setPrefHeight(70);
        cell.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0;");
        cell.setPadding(new Insets(4));
        cell.setId("cell-" + row + "-" + col);
        return cell;
    }
    
    private void loadTasksForMonth() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;
            
            // Get tasks for current month
            List<Tache> tasks = tacheService.getTachesByMonth(
                currentUser.getId(), 
                currentMonth.getYear(), 
                currentMonth.getMonthValue()
            );
            
            // Group by date
            tasksByDate.clear();
            for (Tache task : tasks) {
                LocalDate date = task.getDateDebut();
                tasksByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
            }
            
            // Refresh calendar display
            Platform.runLater(this::refreshCalendarCells);
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des tâches", e);
        }
    }
    
    private void refreshCalendarCells() {
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1 = Monday
        int daysInMonth = currentMonth.lengthOfMonth();
        
        for (int row = 1; row <= 6; row++) {
            final int currentRow = row;
            for (int col = 0; col < 7; col++) {
                final int currentCol = col;
                int cellIndex = (currentRow - 1) * 7 + currentCol;
                int dayNumber = cellIndex - (firstDayOfWeek - 1);
                
                VBox cell = (VBox) gridCalendar.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == currentRow && GridPane.getColumnIndex(node) == currentCol)
                    .findFirst()
                    .orElse(null);
                
                if (cell != null) {
                    cell.getChildren().clear();
                    
                    if (dayNumber >= 1 && dayNumber <= daysInMonth) {
                        LocalDate date = currentMonth.atDay(dayNumber);
                        
                        // Day number label
                        Label lblDay = new Label(String.valueOf(dayNumber));
                        lblDay.setFont(Font.font("System", dayNumber == selectedDate.getDayOfMonth() ? FontWeight.BOLD : FontWeight.NORMAL, 14));
                        
                        if (dayNumber == selectedDate.getDayOfMonth() && 
                            date.getMonth() == selectedDate.getMonth() &&
                            date.getYear() == selectedDate.getYear()) {
                            lblDay.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        }
                        
                        cell.getChildren().add(lblDay);
                        
                        // Add task indicators
                        List<Tache> dayTasks = tasksByDate.getOrDefault(date, Collections.emptyList());
                        for (int i = 0; i < Math.min(dayTasks.size(), 3); i++) {
                            Tache task = dayTasks.get(i);
                            Circle dot = new Circle(4);
                            dot.setFill(getColorForPriority(task.getPriorite()));
                            cell.getChildren().add(dot);
                        }
                        
                        // Click handler
                        cell.setOnMouseClicked(e -> {
                            selectedDate = date;
                            updateSelectedDateDisplay();
                            refreshCalendarCells();
                        });
                        
                        // Hover effect
                        cell.setOnMouseEntered(e -> 
                            cell.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #2E7D32;"));
                        cell.setOnMouseExited(e -> 
                            cell.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0;"));
                    }
                }
            }
        }
    }
    
    private Color getColorForPriority(String priorite) {
        if (priorite == null) return Color.GRAY;
        
        return switch (priorite) {
            case "BASSE" -> Color.valueOf("#4CAF50");
            case "MOYENNE" -> Color.valueOf("#2196F3");
            case "HAUTE" -> Color.valueOf("#FF9800");
            case "URGENTE" -> Color.valueOf("#F44336");
            default -> Color.GRAY;
        };
    }
    
    private void updateMonthDisplay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        lblMonthYear.setText(currentMonth.format(formatter));
    }
    
    private void updateSelectedDateDisplay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        lblSelectedDate.setText(selectedDate.format(formatter));
        
        // Show tasks for selected date
        List<Tache> tasks = tasksByDate.getOrDefault(selectedDate, Collections.emptyList());
        selectedDateTasks.clear();
        selectedDateTasks.addAll(tasks);
        
        displaySelectedDateTasks();
        displaySeasonalTip();
    }
    
    private void displaySelectedDateTasks() {
        vboxTasks.getChildren().clear();
        
        if (selectedDateTasks.isEmpty()) {
            Label lblNoTasks = new Label("Aucune tâche pour cette date");
            lblNoTasks.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
            vboxTasks.getChildren().add(lblNoTasks);
            return;
        }
        
        for (Tache task : selectedDateTasks) {
            VBox taskCard = createTaskCard(task);
            vboxTasks.getChildren().add(taskCard);
        }
    }
    
    private VBox createTaskCard(Tache task) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                     "-fx-border-color: #E0E0E0; -fx-border-radius: 8;");
        card.setPadding(new Insets(8));
        
        Label lblTitre = new Label(task.getTitre());
        lblTitre.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        // Status badge
        Label lblStatut = new Label(task.getStatut());
        lblStatut.setStyle(getStyleForStatut(task.getStatut()));
        lblStatut.setFont(Font.font("System", 10));
        
        // Priority indicator
        Circle priorityDot = new Circle(6);
        priorityDot.setFill(getColorForPriority(task.getPriorite()));
        
        HBox header = new HBox(8);
        header.getChildren().addAll(priorityDot, lblTitre, lblStatut);
        
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            Label lblDesc = new Label(task.getDescription());
            lblDesc.setFont(Font.font("System", 10));
            lblDesc.setStyle("-fx-text-fill: #757575;");
            lblDesc.setWrapText(true);
            card.getChildren().addAll(header, lblDesc);
        } else {
            card.getChildren().add(header);
        }
        
        return card;
    }
    
    private String getStyleForStatut(String statut) {
        return switch (statut) {
            case "A_FAIRE" -> "-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-background-radius: 4; -fx-padding: 2 6;";
            case "EN_COURS" -> "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-background-radius: 4; -fx-padding: 2 6;";
            case "TERMINE" -> "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-background-radius: 4; -fx-padding: 2 6;";
            case "ANNULE" -> "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-background-radius: 4; -fx-padding: 2 6;";
            default -> "-fx-background-color: #F5F5F5; -fx-text-fill: #757575; -fx-background-radius: 4; -fx-padding: 2 6;";
        };
    }
    
    private void displaySeasonalTip() {
        int month = currentMonth.getMonthValue();
        String[] tips = SEASONAL_TIPS.get(month);
        
        if (tips != null) {
            VBox tipBox = new VBox(4);
            tipBox.setStyle("-fx-background-color: #FFF8E1; -fx-background-radius: 8;");
            tipBox.setPadding(new Insets(8));
            
            Label lblTitle = new Label("💡 Conseils du mois");
            lblTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            tipBox.getChildren().add(lblTitle);
            
            for (String tip : tips) {
                Label lblTip = new Label("• " + tip);
                lblTip.setFont(Font.font("System", 11));
                tipBox.getChildren().add(lblTip);
            }
            
            vboxTasks.getChildren().add(tipBox);
        }
    }
    
    @FXML
    private void onPreviousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        updateMonthDisplay();
        loadTasksForMonth();
    }
    
    @FXML
    private void onNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        updateMonthDisplay();
        loadTasksForMonth();
    }
    
    @FXML
    private void onToday() {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        updateMonthDisplay();
        updateSelectedDateDisplay();
        loadTasksForMonth();
    }
    
    @FXML
    private void onAddTask() {
        // This would open a dialog to create a new task
        AlertUtil.showInfo("Nouvelle tâche", "Fonctionnalité de création de tâche à implémenter");
    }
}
