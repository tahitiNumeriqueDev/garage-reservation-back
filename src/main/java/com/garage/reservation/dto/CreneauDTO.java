package com.garage.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreneauDTO {
    
    private Long id;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant heureDebut;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant heureFin;
    
    private Boolean disponible;
    
    private Integer capaciteTotale;
    
    private Integer nombreReservations;
} 