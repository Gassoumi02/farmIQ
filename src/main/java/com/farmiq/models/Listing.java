package com.farmiq.models;

import com.farmiq.models.enums.ListingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Listing {
    private int id;
    private int sellerId;
    private String titre;
    private String description;
    private String categorie;
    private double prix;
    private String unite;
    private double quantiteDisponible;
    private double quantiteMinimum;
    private String wilaya;
    private ListingStatus statut;
    private int vues;
    private LocalDate dateExpiration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Joined fields
    private String nomBoutique;
    private String sellerWilaya;
    private double noteMoyenne;
    private String sellerNom;
    private String sellerEmail;
    private String imagePrincipale;
    private int totalReviews;

    public Listing() {
        this.quantiteMinimum = 1;
        this.statut = ListingStatus.BROUILLON;
    }

    public Listing(int sellerId, String titre, String description, String categorie, 
                   double prix, String unite, double quantiteDisponible, String wilaya) {
        this.sellerId = sellerId;
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.prix = prix;
        this.unite = unite;
        this.quantiteDisponible = quantiteDisponible;
        this.quantiteMinimum = 1;
        this.wilaya = wilaya;
        this.statut = ListingStatus.BROUILLON;
        this.vues = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getUnite() {return unite;}

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public double getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(double quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public double getQuantiteMinimum() {
        return quantiteMinimum;
    }

    public void setQuantiteMinimum(double quantiteMinimum) {
        this.quantiteMinimum = quantiteMinimum;
    }

    public String getWilaya() {
        return wilaya;
    }

    public void setWilaya(String wilaya) {
        this.wilaya = wilaya;
    }

    public ListingStatus getStatut() {
        return statut;
    }

    public void setStatut(ListingStatus statut) {
        this.statut = statut;
    }

    public int getVues() {
        return vues;
    }

    public void setVues(int vues) {
        this.vues = vues;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
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

    public String getNomBoutique() {
        return nomBoutique;
    }

    public void setNomBoutique(String nomBoutique) {
        this.nomBoutique = nomBoutique;
    }

    public String getSellerWilaya() {
        return sellerWilaya;
    }

    public void setSellerWilaya(String sellerWilaya) {
        this.sellerWilaya = sellerWilaya;
    }

    public double getNoteMoyenne() {
        return noteMoyenne;
    }

    public void setNoteMoyenne(double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
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

    public String getImagePrincipale() {
        return imagePrincipale;
    }

    public void setImagePrincipale(String imagePrincipale) {
        this.imagePrincipale = imagePrincipale;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }
}
