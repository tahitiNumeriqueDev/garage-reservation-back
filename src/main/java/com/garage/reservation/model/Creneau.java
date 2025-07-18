package com.garage.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "creneaux")
public class Creneau {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "heure_debut", nullable = false)
    private LocalDateTime heureDebut;
    
    @NotNull
    @Column(name = "heure_fin", nullable = false)
    private LocalDateTime heureFin;
    
    @Column(name = "disponible")
    private Boolean disponible = true;
    
    @Column(name = "capacite_totale")
    private Integer capaciteTotale = 1;
    
    @OneToMany(mappedBy = "creneau", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;
    
    public Creneau() {}
    
    public Creneau(LocalDateTime heureDebut, LocalDateTime heureFin) {
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getHeureDebut() {
        return heureDebut;
    }
    
    public void setHeureDebut(LocalDateTime heureDebut) {
        this.heureDebut = heureDebut;
    }
    
    public LocalDateTime getHeureFin() {
        return heureFin;
    }
    
    public void setHeureFin(LocalDateTime heureFin) {
        this.heureFin = heureFin;
    }
    
    public Boolean getDisponible() {
        return disponible;
    }
    
    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
    
    public Integer getCapaciteTotale() {
        return capaciteTotale;
    }
    
    public void setCapaciteTotale(Integer capaciteTotale) {
        this.capaciteTotale = capaciteTotale;
    }
    
    public List<Reservation> getReservations() {
        return reservations;
    }
    
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
    
    public boolean estDisponible() {
        return disponible && (reservations == null || reservations.size() < capaciteTotale);
    }
    
    public int getNombreReservations() {
        return reservations != null ? reservations.size() : 0;
    }
} 