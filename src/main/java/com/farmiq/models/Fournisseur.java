package com.farmiq.models;

import java.time.LocalDateTime;

public class Fournisseur {
    private int id;
    private String nom;
    private String contact;
    private String telephone;
    private String email;
    private String adresse;
    private String statut;
    private LocalDateTime createdAt;

    public Fournisseur() {
        this.statut = "ACTIF";
    }

    public Fournisseur(String nom, String contact, String telephone, String email) {
        this.nom = nom;
        this.contact = contact;
        this.telephone = telephone;
        this.email = email;
        this.statut = "ACTIF";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
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
}
