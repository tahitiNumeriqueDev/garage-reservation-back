package com.garage.reservation.mapper;

import com.garage.reservation.dto.CreationReservationDTO;
import com.garage.reservation.dto.ReservationDTO;
import com.garage.reservation.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {CreneauMapper.class})
public interface ReservationMapper {
    
    ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);
    
    ReservationDTO toDTO(Reservation reservation);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "creneau", ignore = true)
    Reservation toEntity(CreationReservationDTO creationReservationDTO);
} 