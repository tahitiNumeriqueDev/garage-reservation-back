package com.garage.reservation.dto;

import com.garage.reservation.model.TypeVehicule;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreationReservationDTO {
    
    @NotBlank(message = "L'immatriculation est obligatoire")
    @Size(max = 20, message = "L'immatriculation ne peut pas dépasser 20 caractères")
    private String immatriculation;
    
    @NotNull(message = "Le kilométrage est obligatoire")
    @Min(value = 0, message = "Le kilométrage doit être positif")
    private Integer kilometrage;
    
    @NotNull(message = "Le type de véhicule est obligatoire")
    private TypeVehicule typeVehicule;
    
    @Builder.Default
    private Boolean poidsLourd = false;
    
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Size(max = 15, message = "Le numéro de téléphone ne peut pas dépasser 15 caractères")
    private String numeroTelephone;
    
    @Email(message = "L'adresse email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;
    
    @NotNull(message = "L'ID du créneau est obligatoire")
    private Long creneauId;
} 