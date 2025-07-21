package com.garage.reservation.repository;

import com.garage.reservation.model.Reservation;
import com.garage.reservation.model.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    /**
     * Trouve toutes les réservations d'un créneau donné
     */
    List<Reservation> findByCreneauId(Long creneauId);
    
    /**
     * Trouve toutes les réservations par statut
     */
    List<Reservation> findByStatut(StatutReservation statut);
    
    /**
     * Trouve toutes les réservations par immatriculation
     */
    List<Reservation> findByImmatriculation(String immatriculation);
    
    /**
     * Trouve toutes les réservations par numéro de téléphone
     */
    List<Reservation> findByNumeroTelephone(String numeroTelephone);
    
    /**
     * Trouve toutes les réservations entre deux dates
     */
    @Query("SELECT r FROM Reservation r WHERE r.creneau.heureDebut >= :dateDebut AND r.creneau.heureFin <= :dateFin ORDER BY r.creneau.heureDebut")
    List<Reservation> findReservationsBetweenDates(@Param("dateDebut") Instant dateDebut, 
                                                   @Param("dateFin") Instant dateFin);
    
    /**
     * Trouve toutes les réservations d'un jour donné (entre 00:00 et 23:59:59)
     */
    @Query("SELECT r FROM Reservation r WHERE r.creneau.heureDebut >= :debutJour AND r.creneau.heureDebut < :finJour ORDER BY r.creneau.heureDebut")
    List<Reservation> findReservationsByDate(@Param("debutJour") Instant debutJour, @Param("finJour") Instant finJour);
    
    /**
     * Trouve toutes les réservations d'une semaine (du lundi au dimanche)
     */
    @Query("SELECT r FROM Reservation r WHERE r.creneau.heureDebut >= :debutSemaine AND r.creneau.heureDebut < :finSemaine ORDER BY r.creneau.heureDebut")
    List<Reservation> findReservationsByWeek(@Param("debutSemaine") Instant debutSemaine, @Param("finSemaine") Instant finSemaine);
    
    /**
     * Compte le nombre de réservations pour un créneau donné
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.creneau.id = :creneauId")
    Long countReservationsByCreneauId(@Param("creneauId") Long creneauId);
    
    /**
     * Trouve une réservation par immatriculation et créneau
     */
    Optional<Reservation> findByImmatriculationAndCreneauId(String immatriculation, Long creneauId);
    
    /**
     * Trouve toutes les réservations futures (créneaux non passés)
     */
    @Query("SELECT r FROM Reservation r WHERE r.creneau.heureDebut > :now ORDER BY r.creneau.heureDebut")
    List<Reservation> findReservationsFutures(@Param("now") Instant now);
    
    /**
     * Trouve toutes les réservations passées (créneaux terminés)
     */
    @Query("SELECT r FROM Reservation r WHERE r.creneau.heureFin < :now ORDER BY r.creneau.heureDebut DESC")
    List<Reservation> findReservationsPassees(@Param("now") Instant now);
} 