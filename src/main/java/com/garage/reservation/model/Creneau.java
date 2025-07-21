package com.garage.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "creneaux")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "reservations")
public class Creneau {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "heure_debut", nullable = false)
    private Instant heureDebut;
    
    @NotNull
    @Column(name = "heure_fin", nullable = false)
    private Instant heureFin;
    
    @Builder.Default
    @Column(name = "disponible")
    private Boolean disponible = true;
    
    @Builder.Default
    @Column(name = "capacite_totale")
    private Integer capaciteTotale = 1;
    
    @OneToMany(mappedBy = "creneau", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;
    
    // Méthodes métier (non générées par Lombok)
    public boolean estDisponible() {
        return disponible && (reservations == null || reservations.size() < capaciteTotale);
    }
    
    public int getNombreReservations() {
        return reservations != null ? reservations.size() : 0;
    }
} 