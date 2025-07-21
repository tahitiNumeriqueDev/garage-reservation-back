package com.garage.reservation.repository;

import com.garage.reservation.model.Creneau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CreneauRepository extends JpaRepository<Creneau, Long> {
    
    /**
     * Trouve tous les créneaux entre deux dates
     */
    @Query("SELECT c FROM Creneau c WHERE c.heureDebut >= :dateDebut AND c.heureFin <= :dateFin ORDER BY c.heureDebut")
    List<Creneau> findCreneauxBetweenDates(@Param("dateDebut") Instant dateDebut, 
                                           @Param("dateFin") Instant dateFin);
    
    /**
     * Trouve tous les créneaux disponibles entre deux dates
     */
    @Query("SELECT c FROM Creneau c WHERE c.heureDebut >= :dateDebut AND c.heureFin <= :dateFin AND c.disponible = true ORDER BY c.heureDebut")
    List<Creneau> findCreneauxDisponiblesBetweenDates(@Param("dateDebut") Instant dateDebut, 
                                                      @Param("dateFin") Instant dateFin);
    
    /**
     * Trouve tous les créneaux d'un jour donné
     */
    @Query("SELECT c FROM Creneau c WHERE DATE(c.heureDebut) = DATE(:date) ORDER BY c.heureDebut")
    List<Creneau> findCreneauxByDate(@Param("date") Instant date);
    
    /**
     * Trouve tous les créneaux disponibles d'un jour donné
     */
    @Query("SELECT c FROM Creneau c WHERE DATE(c.heureDebut) = DATE(:date) AND c.disponible = true ORDER BY c.heureDebut")
    List<Creneau> findCreneauxDisponiblesByDate(@Param("date") Instant date);
    
    /**
     * Trouve tous les créneaux d'une semaine donnée
     */
    @Query("SELECT c FROM Creneau c WHERE WEEK(c.heureDebut) = WEEK(:date) AND YEAR(c.heureDebut) = YEAR(:date) ORDER BY c.heureDebut")
    List<Creneau> findCreneauxByWeek(@Param("date") Instant date);
    
    /**
     * Trouve tous les créneaux disponibles d'une semaine donnée
     */
    @Query("SELECT c FROM Creneau c WHERE WEEK(c.heureDebut) = WEEK(:date) AND YEAR(c.heureDebut) = YEAR(:date) AND c.disponible = true ORDER BY c.heureDebut")
    List<Creneau> findCreneauxDisponiblesByWeek(@Param("date") Instant date);
    
    /**
     * Vérifie si un créneau est disponible (moins de réservations que la capacité)
     */
    @Query("SELECT c FROM Creneau c LEFT JOIN c.reservations r WHERE c.id = :creneauId AND c.disponible = true GROUP BY c.id HAVING COUNT(r) < c.capaciteTotale")
    List<Creneau> findCreneauDisponible(@Param("creneauId") Long creneauId);
} 