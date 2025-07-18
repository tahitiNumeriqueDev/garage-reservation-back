package com.garage.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class CreneauDTO {
    
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime heureDebut;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime heureFin;
    
    private Boolean disponible;
    
    private Integer capaciteTotale;
    
    private Integer nombreReservations;
    
    public CreneauDTO() {}
    
    public CreneauDTO(Long id, LocalDateTime heureDebut, LocalDateTime heureFin, 
                     Boolean disponible, Integer capaciteTotale, Integer nombreReservations) {
        this.id = id;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.disponible = disponible;
        this.capaciteTotale = capaciteTotale;
        this.nombreReservations = nombreReservations;
    }
    
    // Getters et Setters
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
    
    public Integer getNombreReservations() {
        return nombreReservations;
    }
    
    public void setNombreReservations(Integer nombreReservations) {
        this.nombreReservations = nombreReservations;
    }
} 