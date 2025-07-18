package com.garage.reservation.service;

import com.garage.reservation.dto.CreationReservationDTO;
import com.garage.reservation.dto.CreneauDTO;
import com.garage.reservation.dto.ReservationDTO;
import com.garage.reservation.model.Creneau;
import com.garage.reservation.model.Reservation;
import com.garage.reservation.model.StatutReservation;
import com.garage.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private CreneauService creneauService;
    
    /**
     * Convertit une Reservation en ReservationDTO
     */
    private ReservationDTO convertToDTO(Reservation reservation) {
        CreneauDTO creneauDTO = new CreneauDTO(
            reservation.getCreneau().getId(),
            reservation.getCreneau().getHeureDebut(),
            reservation.getCreneau().getHeureFin(),
            reservation.getCreneau().estDisponible(),
            reservation.getCreneau().getCapaciteTotale(),
            reservation.getCreneau().getNombreReservations()
        );
        
        return new ReservationDTO(
            reservation.getId(),
            reservation.getImmatriculation(),
            reservation.getKilometrage(),
            reservation.getTypeVehicule(),
            reservation.getPoidsLourd(),
            reservation.getNumeroTelephone(),
            reservation.getEmail(),
            reservation.getStatut(),
            reservation.getDateCreation(),
            reservation.getDateModification(),
            creneauDTO
        );
    }
    
    /**
     * Récupère toutes les réservations
     */
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère une réservation par ID
     */
    public Optional<ReservationDTO> getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les réservations par immatriculation
     */
    public List<ReservationDTO> getReservationsByImmatriculation(String immatriculation) {
        return reservationRepository.findByImmatriculation(immatriculation)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations par numéro de téléphone
     */
    public List<ReservationDTO> getReservationsByNumeroTelephone(String numeroTelephone) {
        return reservationRepository.findByNumeroTelephone(numeroTelephone)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations d'un jour donné
     */
    public List<ReservationDTO> getReservationsByDate(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return reservationRepository.findReservationsByDate(dateTime)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations d'une semaine donnée
     */
    public List<ReservationDTO> getReservationsByWeek(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return reservationRepository.findReservationsByWeek(dateTime)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations futures
     */
    public List<ReservationDTO> getReservationsFutures() {
        return reservationRepository.findReservationsFutures(LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations passées
     */
    public List<ReservationDTO> getReservationsPassees() {
        return reservationRepository.findReservationsPassees(LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Crée une nouvelle réservation
     */
    @Transactional
    public ReservationDTO createReservation(CreationReservationDTO creationDTO) {
        // Vérifier si le créneau existe et est disponible
        Optional<Creneau> creneauOpt = creneauService.getCreneauEntityById(creationDTO.getCreneauId());
        if (creneauOpt.isEmpty()) {
            throw new IllegalArgumentException("Créneau non trouvé avec l'ID: " + creationDTO.getCreneauId());
        }
        
        Creneau creneau = creneauOpt.get();
        
        // Vérifier si le créneau est disponible
        if (!creneauService.isCreneauDisponible(creationDTO.getCreneauId())) {
            throw new IllegalStateException("Le créneau n'est pas disponible");
        }
        
        // Vérifier si une réservation existe déjà pour cette immatriculation et ce créneau
        Optional<Reservation> existingReservation = reservationRepository
                .findByImmatriculationAndCreneauId(creationDTO.getImmatriculation(), creationDTO.getCreneauId());
        
        if (existingReservation.isPresent()) {
            throw new IllegalStateException("Une réservation existe déjà pour cette immatriculation sur ce créneau");
        }
        
        // Créer la réservation
        Reservation reservation = new Reservation(
            creationDTO.getImmatriculation(),
            creationDTO.getKilometrage(),
            creationDTO.getTypeVehicule(),
            creationDTO.getPoidsLourd(),
            creationDTO.getNumeroTelephone(),
            creationDTO.getEmail(),
            creneau
        );
        
        reservation = reservationRepository.save(reservation);
        return convertToDTO(reservation);
    }
    
    /**
     * Met à jour le statut d'une réservation
     */
    @Transactional
    public ReservationDTO updateStatutReservation(Long id, StatutReservation nouveauStatut) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Réservation non trouvée avec l'ID: " + id);
        }
        
        Reservation reservation = reservationOpt.get();
        reservation.setStatut(nouveauStatut);
        reservation = reservationRepository.save(reservation);
        
        return convertToDTO(reservation);
    }
    
    /**
     * Annule une réservation
     */
    @Transactional
    public ReservationDTO annulerReservation(Long id) {
        return updateStatutReservation(id, StatutReservation.ANNULEE);
    }
    
    /**
     * Confirme une réservation
     */
    @Transactional
    public ReservationDTO confirmerReservation(Long id) {
        return updateStatutReservation(id, StatutReservation.CONFIRMEE);
    }
    
    /**
     * Marque une réservation comme terminée
     */
    @Transactional
    public ReservationDTO terminerReservation(Long id) {
        return updateStatutReservation(id, StatutReservation.TERMINEE);
    }
    
    /**
     * Supprime une réservation
     */
    @Transactional
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new IllegalArgumentException("Réservation non trouvée avec l'ID: " + id);
        }
        reservationRepository.deleteById(id);
    }
} 