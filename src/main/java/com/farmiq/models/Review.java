package com.farmiq.models;

import java.time.LocalDateTime;

public class Review {
    private int id;
    private int orderId;
    private int reviewerId;
    private int sellerId;
    private int note;
    private String commentaire;
    private String statut;
    private LocalDateTime createdAt;
    
    // Joined fields
    private String reviewerNom;
    private String nomBoutique;

    public Review() {
        this.statut = "EN_ATTENTE";
    }

    public Review(int orderId, int reviewerId, int sellerId, int note, String commentaire) {
        this.orderId = orderId;
        this.reviewerId = reviewerId;
        this.sellerId = sellerId;
        this.note = note;
        this.commentaire = commentaire;
        this.statut = "EN_ATTENTE";
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

    public int getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getReviewerNom() {
        return reviewerNom;
    }

    public void setReviewerNom(String reviewerNom) {
        this.reviewerNom = reviewerNom;
    }

    public String getNomBoutique() {
        return nomBoutique;
    }

    public void setNomBoutique(String nomBoutique) {
        this.nomBoutique = nomBoutique;
    }
}
