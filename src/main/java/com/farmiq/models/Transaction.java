package com.farmiq.models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle représentant une transaction financière du système FarmIQ
 * @author FarmIQ Team
 * @version 1.0
 */
public class Transaction {
    private IntegerProperty id;
    private IntegerProperty userId;
    private StringProperty type;
    private DoubleProperty montant;
    private ObjectProperty<LocalDate> date;
    private StringProperty description;
    private StringProperty statut;
    private ObjectProperty<LocalDateTime> createdAt;
    private ObjectProperty<LocalDateTime> updatedAt;
    
    // Pour affichage dans le tableau (admin)
    private StringProperty userNom;
    private StringProperty userEmail;

    // ==================== CONSTRUCTEURS ====================
    
    public Transaction() {
        this.id = new SimpleIntegerProperty();
        this.userId = new SimpleIntegerProperty();
        this.type = new SimpleStringProperty();
        this.montant = new SimpleDoubleProperty();
        this.date = new SimpleObjectProperty<>();
        this.description = new SimpleStringProperty();
        this.statut = new SimpleStringProperty("EN_ATTENTE");
        this.createdAt = new SimpleObjectProperty<>();
        this.updatedAt = new SimpleObjectProperty<>();
        this.userNom = new SimpleStringProperty();
        this.userEmail = new SimpleStringProperty();
    }

    public Transaction(String type, double montant, LocalDate date, String description, String statut) {
        this();
        setType(type);
        setMontant(montant);
        setDate(date);
        setDescription(description);
        setStatut(statut);
    }

    // ==================== GETTERS ET SETTERS ====================
    
    // ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // User ID
    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    // Type
    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    // Montant
    public double getMontant() {
        return montant.get();
    }

    public void setMontant(double montant) {
        this.montant.set(montant);
    }

    public DoubleProperty montantProperty() {
        return montant;
    }

    // Date
    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    // Description
    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    // Statut
    public String getStatut() {
        return statut.get();
    }

    public void setStatut(String statut) {
        this.statut.set(statut);
    }

    public StringProperty statutProperty() {
        return statut;
    }

    // CreatedAt
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    // UpdatedAt
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    // User Nom (for display in admin view)
    public String getUserNom() {
        return userNom.get();
    }

    public void setUserNom(String userNom) {
        this.userNom.set(userNom);
    }

    public StringProperty userNomProperty() {
        return userNom;
    }

    // User Email (for display in admin view)
    public String getUserEmail() {
        return userEmail.get();
    }

    public void setUserEmail(String userEmail) {
        this.userEmail.set(userEmail);
    }

    public StringProperty userEmailProperty() {
        return userEmail;
    }

    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie si la transaction est une vente
     * @return true si le type est VENTE
     */
    public boolean isVente() {
        return "VENTE".equalsIgnoreCase(getType());
    }

    /**
     * Vérifie si la transaction est un achat
     * @return true si le type est ACHAT
     */
    public boolean isAchat() {
        return "ACHAT".equalsIgnoreCase(getType());
    }

    /**
     * Vérifie si la transaction est validée
     * @return true si le statut est VALIDEE
     */
    public boolean isValidee() {
        return "VALIDEE".equalsIgnoreCase(getStatut());
    }

    /**
     * Vérifie si la transaction est en attente
     * @return true si le statut est EN_ATTENTE
     */
    public boolean isEnAttente() {
        return "EN_ATTENTE".equalsIgnoreCase(getStatut());
    }

    /**
     * Vérifie si la transaction est annulée
     * @return true si le statut est ANNULEE
     */
    public boolean isAnnulee() {
        return "ANNULEE".equalsIgnoreCase(getStatut());
    }

    /**
     * Obtenir le montant formaté avec le signe
     * @return Montant formaté (ex: +1500.00 ou -800.50)
     */
    public String getMontantFormate() {
        String prefix = isVente() ? "+" : "-";
        return String.format("%s%.2f DT", prefix, getMontant());
    }

    /**
     * Obtenir une description courte (max 50 caractères)
     * @return Description tronquée si nécessaire
     */
    public String getDescriptionCourte() {
        if (getDescription() == null || getDescription().isEmpty()) {
            return "N/A";
        }
        return getDescription().length() > 50 
            ? getDescription().substring(0, 47) + "..." 
            : getDescription();
    }

    // ==================== MÉTHODES OVERRIDE ====================
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + getId() +
                ", type='" + getType() + '\'' +
                ", montant=" + getMontant() +
                ", date=" + getDate() +
                ", statut='" + getStatut() + '\'' +
                ", description='" + getDescriptionCourte() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
