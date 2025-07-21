package com.garage.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.garage.reservation.model.StatutReservation;
import com.garage.reservation.model.TypeVehicule;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {
    
    private Long id;
    
    private String immatriculation;
    
    private Integer kilometrage;
    
    private TypeVehicule typeVehicule;
    
    private Boolean poidsLourd;
    
    private String numeroTelephone;
    
    private String email;
    
    private StatutReservation statut;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant dateCreation;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant dateModification;
    
    private CreneauDTO creneau;
} 