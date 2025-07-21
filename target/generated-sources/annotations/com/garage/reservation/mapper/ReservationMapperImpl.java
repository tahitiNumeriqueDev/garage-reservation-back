package com.garage.reservation.mapper;

import com.garage.reservation.dto.CreationReservationDTO;
import com.garage.reservation.dto.ReservationDTO;
import com.garage.reservation.model.Reservation;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T08:34:44-1000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Eclipse Adoptium)"
)
@Component
public class ReservationMapperImpl implements ReservationMapper {

    @Autowired
    private CreneauMapper creneauMapper;

    @Override
    public ReservationDTO toDTO(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }

        ReservationDTO.ReservationDTOBuilder reservationDTO = ReservationDTO.builder();

        reservationDTO.id( reservation.getId() );
        reservationDTO.immatriculation( reservation.getImmatriculation() );
        reservationDTO.kilometrage( reservation.getKilometrage() );
        reservationDTO.typeVehicule( reservation.getTypeVehicule() );
        reservationDTO.poidsLourd( reservation.getPoidsLourd() );
        reservationDTO.numeroTelephone( reservation.getNumeroTelephone() );
        reservationDTO.email( reservation.getEmail() );
        reservationDTO.statut( reservation.getStatut() );
        reservationDTO.dateCreation( reservation.getDateCreation() );
        reservationDTO.dateModification( reservation.getDateModification() );
        reservationDTO.creneau( creneauMapper.toDTO( reservation.getCreneau() ) );

        return reservationDTO.build();
    }

    @Override
    public Reservation toEntity(CreationReservationDTO creationReservationDTO) {
        if ( creationReservationDTO == null ) {
            return null;
        }

        Reservation.ReservationBuilder reservation = Reservation.builder();

        reservation.immatriculation( creationReservationDTO.getImmatriculation() );
        reservation.kilometrage( creationReservationDTO.getKilometrage() );
        reservation.typeVehicule( creationReservationDTO.getTypeVehicule() );
        reservation.poidsLourd( creationReservationDTO.getPoidsLourd() );
        reservation.numeroTelephone( creationReservationDTO.getNumeroTelephone() );
        reservation.email( creationReservationDTO.getEmail() );

        return reservation.build();
    }
}
