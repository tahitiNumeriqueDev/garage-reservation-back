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
    
    /**
     * Vérifie si le créneau est disponible pour une nouvelle réservation
     * Un créneau est disponible si :
     * - Le flag disponible est à true
     * - Le nombre de réservations non-annulées est inférieur à la capacité totale
     */
    public boolean estDisponible() {
        if (!disponible) {
            return false;
        }
        
        if (reservations == null) {
            return true;
        }
        
        long reservationsActives = reservations.stream()
                .filter(reservation -> reservation.getStatut() != StatutReservation.ANNULEE)
                .count();
        
        return reservationsActives < capaciteTotale;
    }
    
    /**
     * Retourne le nombre de réservations actives (non-annulées)
     */
    public int getNombreReservations() {
        if (reservations == null) {
            return 0;
        }
        
        return (int) reservations.stream()
                .filter(reservation -> reservation.getStatut() != StatutReservation.ANNULEE)
                .count();
    }
    
    /**
     * Retourne le nombre total de réservations (y compris annulées)
     */
    public int getNombreTotalReservations() {
        return reservations != null ? reservations.size() : 0;
    }
    
    /**
     * Retourne le nombre de places encore disponibles
     */
    public int getNombrePlacesDisponibles() {
        return Math.max(0, capaciteTotale - getNombreReservations());
    }
} 