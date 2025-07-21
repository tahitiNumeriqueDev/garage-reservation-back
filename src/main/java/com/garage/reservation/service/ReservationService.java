package com.garage.reservation.service;

import com.garage.reservation.dto.CreationReservationDTO;
import com.garage.reservation.dto.ReservationDTO;
import com.garage.reservation.mapper.ReservationMapper;
import com.garage.reservation.model.Creneau;
import com.garage.reservation.model.Reservation;
import com.garage.reservation.model.StatutReservation;
import com.garage.reservation.repository.CreneauRepository;
import com.garage.reservation.repository.ReservationRepository;
import com.garage.reservation.util.DateTimeUtil;
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
        Instant debutJour = DateTimeUtil.getStartOfDay(date);
        Instant finJour = DateTimeUtil.getStartOfNextDay(date);
        return reservationRepository.findReservationsByDate(debutJour, finJour)
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations d'une semaine donnée (du lundi au dimanche)
     */
    public List<ReservationDTO> getReservationsByWeek(Instant date) {
        Instant debutSemaine = DateTimeUtil.getStartOfWeek(date);
        Instant finSemaine = DateTimeUtil.getStartOfNextWeek(date);
        return reservationRepository.findReservationsByWeek(debutSemaine, finSemaine)
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
        // Vérifier que le créneau existe et est réellement disponible
        // (vérification robuste qui prend en compte les réservations actives)
        Boolean creneauDisponible = creneauRepository.isCreneauReallyAvailable(creationDTO.getCreneauId());
        
        if (creneauDisponible == null || !creneauDisponible) {
            if (creneauDisponible == null) {
                throw new IllegalArgumentException("Le créneau spécifié n'existe pas");
            } else {
                throw new IllegalStateException("Le créneau n'est plus disponible (capacité atteinte)");
            }
        }
        
        // Récupérer le créneau (maintenant on sait qu'il existe et est disponible)
        Optional<Creneau> creneauOpt = creneauRepository.findById(creationDTO.getCreneauId());
        Creneau creneau = creneauOpt.get(); // Safe car on vient de vérifier
        
        // Créer la réservation
        Reservation reservation = reservationMapper.toEntity(creationDTO);
        reservation.setCreneau(creneau);
        reservation.setStatut(StatutReservation.RESERVEE);
        
        // Sauvegarder la réservation
        reservation = reservationRepository.save(reservation);
        
        // Mettre à jour le flag disponible du créneau si nécessaire
        updateCreneauDisponibilite(creneau);
        
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
        StatutReservation ancienStatut = reservation.getStatut();
        reservation.setStatut(nouveauStatut);
        reservation = reservationRepository.save(reservation);
        
        // Mettre à jour la disponibilité du créneau si le statut change entre actif/inactif
        if (isStatutChangeAffectingAvailability(ancienStatut, nouveauStatut)) {
            updateCreneauDisponibilite(reservation.getCreneau());
        }
        
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
    
    /**
     * Met à jour automatiquement le flag disponible d'un créneau selon sa capacité
     */
    private void updateCreneauDisponibilite(Creneau creneau) {
        // Recharger le créneau avec ses réservations pour avoir les données à jour
        Optional<Creneau> creneauWithReservations = creneauRepository.findByIdWithReservations(creneau.getId());
        
        if (creneauWithReservations.isPresent()) {
            Creneau creneauActuel = creneauWithReservations.get();
            
            // Compter les réservations actives
            int reservationsActives = creneauActuel.getNombreReservations();
            
            // Déterminer le nouveau statut disponible
            boolean nouvelleDisponibilite = reservationsActives < creneauActuel.getCapaciteTotale();
            
            // Mettre à jour seulement si nécessaire
            if (creneauActuel.getDisponible() != nouvelleDisponibilite) {
                creneauActuel.setDisponible(nouvelleDisponibilite);
                creneauRepository.save(creneauActuel);
                
                // Log pour traçabilité
                System.out.println(String.format(
                    "🔄 Créneau ID %d : disponible %s → %s (réservations actives: %d/%d)",
                    creneauActuel.getId(),
                    !nouvelleDisponibilite ? "true" : "false", 
                    nouvelleDisponibilite ? "true" : "false",
                    reservationsActives,
                    creneauActuel.getCapaciteTotale()
                ));
            }
        }
    }
    
    /**
     * Vérifie si un changement de statut affecte la disponibilité du créneau
     */
    private boolean isStatutChangeAffectingAvailability(StatutReservation ancienStatut, StatutReservation nouveauStatut) {
        // Les statuts qui comptent comme "actifs" (occupent une place)
        boolean ancienActif = ancienStatut != StatutReservation.ANNULEE;
        boolean nouveauActif = nouveauStatut != StatutReservation.ANNULEE;
        
        // Retourne true si le statut passe de actif à inactif ou vice versa
        return ancienActif != nouveauActif;
    }
} 