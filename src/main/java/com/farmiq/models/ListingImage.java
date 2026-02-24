package com.farmiq.models;

import java.time.LocalDateTime;

public class ListingImage {
    private int id;
    private int listingId;
    private String imageUrl;
    private boolean isPrincipale;
    private int ordre;
    private LocalDateTime createdAt;

    public ListingImage() {
    }

    public ListingImage(int listingId, String imageUrl, boolean isPrincipale) {
        this.listingId = listingId;
        this.imageUrl = imageUrl;
        this.isPrincipale = isPrincipale;
        this.ordre = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getListingId() {
        return listingId;
    }

    public void setListingId(int listingId) {
        this.listingId = listingId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPrincipale() {
        return isPrincipale;
    }

    public void setPrincipale(boolean principale) {
        isPrincipale = principale;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
