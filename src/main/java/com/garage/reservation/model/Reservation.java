package com.garage.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import java.time.Instant;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "creneau")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 20)
    @Column(name = "immatriculation", nullable = false)
    private String immatriculation;
    
    @NotNull
    @Min(0)
    @Column(name = "kilometrage", nullable = false)
    private Integer kilometrage;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type_vehicule", nullable = false)
    private TypeVehicule typeVehicule;
    
    @Builder.Default
    @Column(name = "poids_lourd")
    private Boolean poidsLourd = false;
    
    @NotBlank
    @Size(max = 15)
    @Column(name = "numero_telephone", nullable = false)
    private String numeroTelephone;
    
    @Email
    @Size(max = 100)
    @Column(name = "email")
    private String email;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutReservation statut = StatutReservation.RESERVEE;
    
    @Column(name = "date_creation")
    private Instant dateCreation;
    
    @Column(name = "date_modification")
    private Instant dateModification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creneau_id", nullable = false)
    private Creneau creneau;
    
    // Hooks JPA pour gérer automatiquement les dates
    @PrePersist
    public void prePersist() {
        this.dateCreation = Instant.now();
        this.dateModification = Instant.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.dateModification = Instant.now();
    }
} 