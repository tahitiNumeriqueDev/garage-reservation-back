package com.garage.reservation.repository;

import com.garage.reservation.model.Reservation;
import com.garage.reservation.model.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    List<Reservation> findReservationsBetweenDates(@Param("dateDebut") LocalDateTime dateDebut, 
                                                   @Param("dateFin") LocalDateTime dateFin);
    
    /**
     * Trouve toutes les réservations d'un jour donné
     */
    @Query("SELECT r FROM Reservation r WHERE DATE(r.creneau.heureDebut) = DATE(:date) ORDER BY r.creneau.heureDebut")
    List<Reservation> findReservationsByDate(@Param("date") LocalDateTime date);
    
    /**
     * Trouve toutes les réservations d'une semaine donnée
     */
    @Query("SELECT r FROM Reservation r WHERE WEEK(r.creneau.heureDebut) = WEEK(:date) AND YEAR(r.creneau.heureDebut) = YEAR(:date) ORDER BY r.creneau.heureDebut")
    List<Reservation> findReservationsByWeek(@Param("date") LocalDateTime date);
    
    /**
     * Compte le nombre de réservations pour un créneau donné
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.creneau.id = :creneauId")
    Long countReservationsByCreneauId(@Param("creneauId") Long creneauId);
    
    /**
     * Vérifie si une réservation existe déjà pour une immatriculation et un créneau donné
     */
    Optional<Reservation> findByImmatriculationAndCreneauId(String immatriculation, Long creneauId);
    
    /**
     * Trouve toutes les réservations futures (créneaux non passés)
     */
    @Query("SELECT r FROM Reservation r WHERE r.creneau.heureDebut > :now ORDER BY r.creneau.heureDebut")
    List<Reservation> findReservationsFutures(@Param("now") LocalDateTime now);
    
    /**
     * Trouve toutes les réservations passées (créneaux terminés)
     */
    @Query("SELECT r FROM Reservation r WHERE r.creneau.heureFin < :now ORDER BY r.creneau.heureDebut DESC")
    List<Reservation> findReservationsPassees(@Param("now") LocalDateTime now);
} 