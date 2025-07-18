package com.garage.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "immatriculation", nullable = false, length = 20)
    private String immatriculation;
    
    @NotNull
    @Column(name = "kilometrage", nullable = false)
    private Integer kilometrage;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type_vehicule", nullable = false)
    private TypeVehicule typeVehicule;
    
    @Column(name = "poids_lourd")
    private Boolean poidsLourd = false;
    
    @NotBlank
    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{10,20}$")
    @Column(name = "numero_telephone", nullable = false, length = 20)
    private String numeroTelephone;
    
    @Email
    @Column(name = "email", length = 100)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutReservation statut = StatutReservation.RESERVEE;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creneau_id", nullable = false)
    private Creneau creneau;
    
    public Reservation() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }
    
    public Reservation(String immatriculation, Integer kilometrage, TypeVehicule typeVehicule, 
                      Boolean poidsLourd, String numeroTelephone, String email, Creneau creneau) {
        this();
        this.immatriculation = immatriculation;
        this.kilometrage = kilometrage;
        this.typeVehicule = typeVehicule;
        this.poidsLourd = poidsLourd;
        this.numeroTelephone = numeroTelephone;
        this.email = email;
        this.creneau = creneau;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
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
    
    public Creneau getCreneau() {
        return creneau;
    }
    
    public void setCreneau(Creneau creneau) {
        this.creneau = creneau;
    }
} 