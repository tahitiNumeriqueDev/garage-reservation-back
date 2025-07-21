package com.garage.reservation.service;

import com.garage.reservation.dto.CreationReservationDTO;
import com.garage.reservation.dto.ReservationDTO;
import com.garage.reservation.mapper.ReservationMapper;
import com.garage.reservation.model.Creneau;
import com.garage.reservation.model.Reservation;
import com.garage.reservation.model.StatutReservation;
import com.garage.reservation.repository.CreneauRepository;
import com.garage.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final CreneauRepository creneauRepository;
    
    /**
     * Récupère toutes les réservations
     */
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère une réservation par son ID
     */
    public Optional<ReservationDTO> getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toDTO);
    }
    
    /**
     * Récupère les réservations par immatriculation
     */
    public List<ReservationDTO> getReservationsByImmatriculation(String immatriculation) {
        return reservationRepository.findByImmatriculation(immatriculation)
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations par numéro de téléphone
     */
    public List<ReservationDTO> getReservationsByPhone(String numeroTelephone) {
        return reservationRepository.findByNumeroTelephone(numeroTelephone)
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations d'un jour donné
     */
    public List<ReservationDTO> getReservationsByDate(Instant date) {
        return reservationRepository.findReservationsByDate(date)
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations d'une semaine donnée
     */
    public List<ReservationDTO> getReservationsByWeek(Instant date) {
        return reservationRepository.findReservationsByWeek(date)
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations futures
     */
    public List<ReservationDTO> getReservationsFutures() {
        return reservationRepository.findReservationsFutures(Instant.now())
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations passées
     */
    public List<ReservationDTO> getReservationsPassees() {
        return reservationRepository.findReservationsPassees(Instant.now())
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Crée une nouvelle réservation
     */
    public ReservationDTO createReservation(CreationReservationDTO creationDTO) {
        // Vérifier que le créneau existe
        Optional<Creneau> creneauOpt = creneauRepository.findById(creationDTO.getCreneauId());
        if (creneauOpt.isEmpty()) {
            throw new IllegalArgumentException("Le créneau spécifié n'existe pas");
        }
        
        Creneau creneau = creneauOpt.get();
        
        // Vérifier que le créneau est disponible
        if (!creneau.estDisponible()) {
            throw new IllegalStateException("Le créneau n'est plus disponible");
        }
        
        // Créer la réservation
        Reservation reservation = reservationMapper.toEntity(creationDTO);
        reservation.setCreneau(creneau);
        reservation.setStatut(StatutReservation.RESERVEE);
        
        // Sauvegarder
        reservation = reservationRepository.save(reservation);
        
        return reservationMapper.toDTO(reservation);
    }
    
    /**
     * Met à jour le statut d'une réservation
     */
    public Optional<ReservationDTO> updateReservationStatut(Long id, StatutReservation nouveauStatut) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Reservation reservation = reservationOpt.get();
        reservation.setStatut(nouveauStatut);
        reservation = reservationRepository.save(reservation);
        
        return Optional.of(reservationMapper.toDTO(reservation));
    }
    
    /**
     * Supprime une réservation
     */
    public boolean deleteReservation(Long id) {
        if (reservationRepository.existsById(id)) {
            reservationRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 