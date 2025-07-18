package com.garage.reservation.dto;

import com.garage.reservation.model.TypeVehicule;
import jakarta.validation.constraints.*;

public class CreationReservationDTO {
    
    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;
    
    @NotNull(message = "Le kilométrage est obligatoire")
    @Min(value = 0, message = "Le kilométrage doit être positif")
    private Integer kilometrage;
    
    @NotNull(message = "Le type de véhicule est obligatoire")
    private TypeVehicule typeVehicule;
    
    private Boolean poidsLourd = false;
    
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{10,20}$", message = "Format de numéro de téléphone invalide")
    private String numeroTelephone;
    
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotNull(message = "L'ID du créneau est obligatoire")
    private Long creneauId;
    
    public CreationReservationDTO() {}
    
    public CreationReservationDTO(String immatriculation, Integer kilometrage, TypeVehicule typeVehicule, 
                                 Boolean poidsLourd, String numeroTelephone, String email, Long creneauId) {
        this.immatriculation = immatriculation;
        this.kilometrage = kilometrage;
        this.typeVehicule = typeVehicule;
        this.poidsLourd = poidsLourd;
        this.numeroTelephone = numeroTelephone;
        this.email = email;
        this.creneauId = creneauId;
    }
    
    // Getters et Setters
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
    
    public Long getCreneauId() {
        return creneauId;
    }
    
    public void setCreneauId(Long creneauId) {
        this.creneauId = creneauId;
    }
} 