package com.garage.reservation.mapper;

import com.garage.reservation.dto.CreneauDTO;
import com.garage.reservation.model.Creneau;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CreneauMapper {
    
    CreneauMapper INSTANCE = Mappers.getMapper(CreneauMapper.class);
    
    @Mapping(target = "nombreReservations", expression = "java(creneau.getNombreReservations())")
    CreneauDTO toDTO(Creneau creneau);
    
    @Mapping(target = "reservations", ignore = true)
    Creneau toEntity(CreneauDTO creneauDTO);
} 