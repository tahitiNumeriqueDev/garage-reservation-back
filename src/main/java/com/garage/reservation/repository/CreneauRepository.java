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
     * Trouve le créneau disponible par ID
     */
    @Query("SELECT c FROM Creneau c WHERE c.id = :id AND c.disponible = true")
    List<Creneau> findCreneauDisponible(@Param("id") Long id);
    
    /**
     * Trouve tous les créneaux entre deux dates
     */
    @Query("SELECT c FROM Creneau c WHERE c.heureDebut >= :dateDebut AND c.heureFin <= :dateFin ORDER BY c.heureDebut")
    List<Creneau> findCreneauxBetweenDates(@Param("dateDebut") Instant dateDebut, 
                                           @Param("dateFin") Instant dateFin);
    
    /**
     * Trouve tous les créneaux disponibles entre deux dates (en considérant les réservations)
     */
    @Query("SELECT DISTINCT c FROM Creneau c LEFT JOIN c.reservations r " +
           "WHERE c.heureDebut >= :dateDebut AND c.heureFin <= :dateFin AND c.disponible = true " +
           "GROUP BY c.id " +
           "HAVING COUNT(CASE WHEN r.statut != 'ANNULEE' THEN 1 END) < c.capaciteTotale " +
           "ORDER BY c.heureDebut")
    List<Creneau> findCreneauxDisponiblesBetweenDates(@Param("dateDebut") Instant dateDebut, 
                                                      @Param("dateFin") Instant dateFin);
    
    /**
     * Trouve tous les créneaux d'un jour donné (entre 00:00 et 23:59:59)
     */
    @Query("SELECT c FROM Creneau c LEFT JOIN FETCH c.reservations WHERE c.heureDebut >= :debutJour AND c.heureDebut < :finJour ORDER BY c.heureDebut")
    List<Creneau> findCreneauxByDate(@Param("debutJour") Instant debutJour, @Param("finJour") Instant finJour);
    
    /**
     * Trouve tous les créneaux disponibles d'un jour donné (en considérant les réservations)
     */
    @Query("SELECT DISTINCT c FROM Creneau c LEFT JOIN c.reservations r " +
           "WHERE c.heureDebut >= :debutJour AND c.heureDebut < :finJour AND c.disponible = true " +
           "GROUP BY c.id " +
           "HAVING COUNT(CASE WHEN r.statut != 'ANNULEE' THEN 1 END) < c.capaciteTotale " +
           "ORDER BY c.heureDebut")
    List<Creneau> findCreneauxDisponiblesByDate(@Param("debutJour") Instant debutJour, @Param("finJour") Instant finJour);
    
    /**
     * Trouve tous les créneaux d'une semaine (du lundi au dimanche)
     */
    @Query("SELECT c FROM Creneau c LEFT JOIN FETCH c.reservations WHERE c.heureDebut >= :debutSemaine AND c.heureDebut < :finSemaine ORDER BY c.heureDebut")
    List<Creneau> findCreneauxByWeek(@Param("debutSemaine") Instant debutSemaine, @Param("finSemaine") Instant finSemaine);
    
    /**
     * Trouve tous les créneaux disponibles d'une semaine (en considérant les réservations)
     */
    @Query("SELECT DISTINCT c FROM Creneau c LEFT JOIN c.reservations r " +
           "WHERE c.heureDebut >= :debutSemaine AND c.heureDebut < :finSemaine AND c.disponible = true " +
           "GROUP BY c.id " +
           "HAVING COUNT(CASE WHEN r.statut != 'ANNULEE' THEN 1 END) < c.capaciteTotale " +
           "ORDER BY c.heureDebut")
    List<Creneau> findCreneauxDisponiblesByWeek(@Param("debutSemaine") Instant debutSemaine, @Param("finSemaine") Instant finSemaine);
} 