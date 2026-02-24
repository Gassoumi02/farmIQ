package com.farmiq.models;

public class OrderItem {
    private int id;
    private int orderId;
    private int listingId;
    private double quantite;
    private double prixUnitaire;
    private double sousTotal;
    
    // Joined fields
    private String titreListing;
    private String imageUrl;

    public OrderItem() {
    }

    public OrderItem(int orderId, int listingId, double quantite, double prixUnitaire) {
        this.orderId = orderId;
        this.listingId = listingId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = quantite * prixUnitaire;
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

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public double getSousTotal() {
        return sousTotal;
    }

    public void setSousTotal(double sousTotal) {
        this.sousTotal = sousTotal;
    }

    public String getTitreListing() {
        return titreListing;
    }

    public void setTitreListing(String titreListing) {
        this.titreListing = titreListing;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
