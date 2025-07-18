package com.garage.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.garage.reservation.model.StatutReservation;
import com.garage.reservation.model.TypeVehicule;
import java.time.LocalDateTime;

public class ReservationDTO {
    
    private Long id;
    private String immatriculation;
    private Integer kilometrage;
    private TypeVehicule typeVehicule;
    private Boolean poidsLourd;
    private String numeroTelephone;
    private String email;
    private StatutReservation statut;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreation;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateModification;
    
    private CreneauDTO creneau;
    
    public ReservationDTO() {}
    
    public ReservationDTO(Long id, String immatriculation, Integer kilometrage, 
                         TypeVehicule typeVehicule, Boolean poidsLourd, String numeroTelephone, 
                         String email, StatutReservation statut, LocalDateTime dateCreation, 
                         LocalDateTime dateModification, CreneauDTO creneau) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.kilometrage = kilometrage;
        this.typeVehicule = typeVehicule;
        this.poidsLourd = poidsLourd;
        this.numeroTelephone = numeroTelephone;
        this.email = email;
        this.statut = statut;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.creneau = creneau;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getImmatriculation() {
        return immatriculation;
    }
    
    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }
    
    public Integer getKilometrage() {
        return kilometrage;
    }
    
    public void setKilometrage(Integer kilometrage) {
        this.kilometrage = kilometrage;
    }
    
    public TypeVehicule getTypeVehicule() {
        return typeVehicule;
    }
    
    public void setTypeVehicule(TypeVehicule typeVehicule) {
        this.typeVehicule = typeVehicule;
    }
    
    public Boolean getPoidsLourd() {
        return poidsLourd;
    }
    
    public void setPoidsLourd(Boolean poidsLourd) {
        this.poidsLourd = poidsLourd;
    }
    
    public String getNumeroTelephone() {
        return numeroTelephone;
    }
    
    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public StatutReservation getStatut() {
        return statut;
    }
    
    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDateTime getDateModification() {
        return dateModification;
    }
    
    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }
    
    public CreneauDTO getCreneau() {
        return creneau;
    }
    
    public void setCreneau(CreneauDTO creneau) {
        this.creneau = creneau;
    }
} 