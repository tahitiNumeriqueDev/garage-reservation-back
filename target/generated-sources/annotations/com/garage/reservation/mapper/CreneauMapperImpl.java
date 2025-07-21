package com.garage.reservation.mapper;

import com.garage.reservation.dto.CreneauDTO;
import com.garage.reservation.model.Creneau;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T10:50:32-1000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Eclipse Adoptium)"
)
@Component
public class CreneauMapperImpl implements CreneauMapper {

    @Override
    public CreneauDTO toDTO(Creneau creneau) {
        if ( creneau == null ) {
            return null;
        }

        CreneauDTO.CreneauDTOBuilder creneauDTO = CreneauDTO.builder();

        creneauDTO.id( creneau.getId() );
        creneauDTO.heureDebut( creneau.getHeureDebut() );
        creneauDTO.heureFin( creneau.getHeureFin() );
        creneauDTO.disponible( creneau.getDisponible() );
        creneauDTO.capaciteTotale( creneau.getCapaciteTotale() );

        creneauDTO.nombreReservations( creneau.getNombreReservations() );

        return creneauDTO.build();
    }

    @Override
    public Creneau toEntity(CreneauDTO creneauDTO) {
        if ( creneauDTO == null ) {
            return null;
        }

        Creneau.CreneauBuilder creneau = Creneau.builder();

        creneau.id( creneauDTO.getId() );
        creneau.heureDebut( creneauDTO.getHeureDebut() );
        creneau.heureFin( creneauDTO.getHeureFin() );
        creneau.disponible( creneauDTO.getDisponible() );
        creneau.capaciteTotale( creneauDTO.getCapaciteTotale() );

        return creneau.build();
    }
}
