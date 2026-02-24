package com.farmiq.models;

import java.time.LocalDateTime;

public class SellerProfile {
    private int id;
    private int userId;
    private String nomBoutique;
    private String description;
    private String wilaya;
    private String adresse;
    private String telephone;
    private String logoUrl;
    private double noteMoyenne;
    private int totalVentes;
    private String statut;
    private LocalDateTime createdAt;
    
    // Joined fields
    private String sellerNom;
    private String sellerEmail;

    public SellerProfile() {
    }

    public SellerProfile(int userId, String nomBoutique, String wilaya) {
        this.userId = userId;
        this.nomBoutique = nomBoutique;
        this.wilaya = wilaya;
        this.statut = "EN_ATTENTE";
        this.noteMoyenne = 0.0;
        this.totalVentes = 0;
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

    public String getNomBoutique() {
        return nomBoutique;
    }

    public void setNomBoutique(String nomBoutique) {
        this.nomBoutique = nomBoutique;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWilaya() {
        return wilaya;
    }

    public void setWilaya(String wilaya) {
        this.wilaya = wilaya;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public double getNoteMoyenne() {
        return noteMoyenne;
    }

    public void setNoteMoyenne(double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }

    public int getTotalVentes() {
        return totalVentes;
    }

    public void setTotalVentes(int totalVentes) {
        this.totalVentes = totalVentes;
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

    public String getSellerNom() {
        return sellerNom;
    }

    public void setSellerNom(String sellerNom) {
        this.sellerNom = sellerNom;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }
}
