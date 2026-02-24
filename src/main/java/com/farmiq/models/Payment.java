package com.farmiq.models;

import com.farmiq.models.enums.PaymentMethod;
import com.farmiq.models.enums.PaymentStatus;
import java.time.LocalDateTime;

public class Payment {
    private int id;
    private int orderId;
    private PaymentMethod methode;
    private PaymentStatus statut;
    private double montant;
    private String reference;
    private String notes;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public Payment() {
        this.statut = PaymentStatus.EN_ATTENTE;
    }

    public Payment(int orderId, PaymentMethod methode, double montant) {
        this.orderId = orderId;
        this.methode = methode;
        this.montant = montant;
        this.statut = PaymentStatus.EN_ATTENTE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getMethode() {
        return methode;
    }

    public void setMethode(PaymentMethod methode) {
        this.methode = methode;
    }

    public PaymentStatus getStatut() {
        return statut;
    }

    public void setStatut(PaymentStatus statut) {
        this.statut = statut;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
