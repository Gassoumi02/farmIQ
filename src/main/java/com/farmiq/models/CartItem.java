package com.farmiq.models;

import java.time.LocalDateTime;

public class CartItem {
    private int id;
    private int userId;
    private int listingId;
    private double quantite;
    private LocalDateTime addedAt;
    
    // Joined fields
    private String titreListing;
    private double prix;
    private String unite;
    private String imageUrl;
    private String nomBoutique;
    private int sellerId;
    private double quantiteDisponible;

    public CartItem() {
        this.quantite = 1;
    }

    public CartItem(int userId, int listingId, double quantite) {
        this.userId = userId;
        this.listingId = listingId;
        this.quantite = quantite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getListingId() {
        return listingId;
    }

    public void setListingId(int listingId) {
        this.listingId = listingId;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public String getTitreListing() {
        return titreListing;
    }

    public void setTitreListing(String titreListing) {
        this.titreListing = titreListing;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNomBoutique() {
        return nomBoutique;
    }

    public void setNomBoutique(String nomBoutique) {
        this.nomBoutique = nomBoutique;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public double getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(double quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }
    
    public double getSousTotal() {
        return quantite * prix;
    }
}
