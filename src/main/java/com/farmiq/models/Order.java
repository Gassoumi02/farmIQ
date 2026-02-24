package com.farmiq.models;

import com.farmiq.models.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int id;
    private int buyerId;
    private int sellerId;
    private OrderStatus statut;
    private double montantTotal;
    private double fraisLivraison;
    private String adresseLivraison;
    private String notes;
    private String codeSuivi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Joined fields
    private String buyerNom;
    private String buyerEmail;
    private String nomBoutique;
    private String paymentStatut;
    private String paymentMethode;
    
    // Related items
    private List<OrderItem> items;

    public Order() {
        this.statut = OrderStatus.EN_ATTENTE;
    }

    public Order(int buyerId, int sellerId, double montantTotal, String adresseLivraison) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.montantTotal = montantTotal;
        this.fraisLivraison = 0;
        this.adresseLivraison = adresseLivraison;
        this.statut = OrderStatus.EN_ATTENTE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public OrderStatus getStatut() {
        return statut;
    }

    public void setStatut(OrderStatus statut) {
        this.statut = statut;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public double getFraisLivraison() {
        return fraisLivraison;
    }

    public void setFraisLivraison(double fraisLivraison) {
        this.fraisLivraison = fraisLivraison;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCodeSuivi() {
        return codeSuivi;
    }

    public void setCodeSuivi(String codeSuivi) {
        this.codeSuivi = codeSuivi;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getBuyerNom() {
        return buyerNom;
    }

    public void setBuyerNom(String buyerNom) {
        this.buyerNom = buyerNom;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getNomBoutique() {
        return nomBoutique;
    }

    public void setNomBoutique(String nomBoutique) {
        this.nomBoutique = nomBoutique;
    }

    public String getPaymentStatut() {
        return paymentStatut;
    }

    public void setPaymentStatut(String paymentStatut) {
        this.paymentStatut = paymentStatut;
    }

    public String getPaymentMethode() {
        return paymentMethode;
    }

    public void setPaymentMethode(String paymentMethode) {
        this.paymentMethode = paymentMethode;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
