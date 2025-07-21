package com.garage.reservation.service;

import com.garage.reservation.model.Creneau;
import com.garage.reservation.repository.CreneauRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreneauGenerationService {
    
    private final CreneauRepository creneauRepository;
    
    // Jours fériés fixes pour 2025 (France)
    private static final Set<LocalDate> JOURS_FERIES_2025 = Set.of(
            LocalDate.of(2025, 1, 1),   // Nouvel An
            LocalDate.of(2025, 5, 1),   // Fête du Travail
            LocalDate.of(2025, 5, 8),   // Victoire 1945
            LocalDate.of(2025, 7, 14),  // Fête Nationale
            LocalDate.of(2025, 8, 15),  // Assomption
            LocalDate.of(2025, 11, 1),  // Toussaint
            LocalDate.of(2025, 11, 11), // Armistice
            LocalDate.of(2025, 12, 25)  // Noël
    );
    
    /**
     * Génère les créneaux pour juillet et août 2025
     */
    public int generateCreneauxJuilletAout2025() {
        log.info("🚀 Génération des créneaux pour juillet et août 2025...");
        
        List<Creneau> creneauxToSave = new ArrayList<>();
        
        // Juillet 2025
        LocalDate debutJuillet = LocalDate.of(2025, 7, 1);
        LocalDate finJuillet = LocalDate.of(2025, 7, 31);
        creneauxToSave.addAll(generateCreneauxForPeriod(debutJuillet, finJuillet));
        
        // Août 2025  
        LocalDate debutAout = LocalDate.of(2025, 8, 1);
        LocalDate finAout = LocalDate.of(2025, 8, 31);
        creneauxToSave.addAll(generateCreneauxForPeriod(debutAout, finAout));
        
        // Sauvegarde en base
        List<Creneau> savedCreneaux = creneauRepository.saveAll(creneauxToSave);
        
        log.info("✅ {} créneaux générés avec succès pour juillet-août 2025", savedCreneaux.size());
        return savedCreneaux.size();
    }
    
    /**
     * Génère les créneaux pour une période donnée
     */
    private List<Creneau> generateCreneauxForPeriod(LocalDate debut, LocalDate fin) {
        List<Creneau> creneaux = new ArrayList<>();
        
        LocalDate currentDate = debut;
        
        while (!currentDate.isAfter(fin)) {
            // Vérifier si c'est un jour ouvrable
            if (isJourOuvrable(currentDate)) {
                creneaux.addAll(generateCreneauxForDay(currentDate));
            } else {
                log.debug("⏭️  Jour non-ouvrable ignoré : {}", currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return creneaux;
    }
    
    /**
     * Génère les créneaux pour une journée (8h-18h, créneaux de 1h)
     */
    private List<Creneau> generateCreneauxForDay(LocalDate date) {
        List<Creneau> creneaux = new ArrayList<>();
        
        // Horaires du garage : 8h - 18h
        LocalTime heureDebut = LocalTime.of(8, 0);
        LocalTime heureFin = LocalTime.of(18, 0);
        
        LocalTime currentTime = heureDebut;
        
        while (currentTime.isBefore(heureFin)) {
            LocalTime nextTime = currentTime.plusHours(1);
            
            // Pause déjeuner 12h-14h - pas de créneaux
            if (!(currentTime.equals(LocalTime.of(12, 0)) || currentTime.equals(LocalTime.of(13, 0)))) {
                Instant instantDebut = date.atTime(currentTime).toInstant(ZoneOffset.UTC);
                Instant instantFin = date.atTime(nextTime).toInstant(ZoneOffset.UTC);
                
                Creneau creneau = Creneau.builder()
                        .heureDebut(instantDebut)
                        .heureFin(instantFin)
                        .disponible(true)
                        .capaciteTotale(2) // 2 véhicules par créneau
                        .build();
                
                creneaux.add(creneau);
            }
            
            currentTime = nextTime;
        }
        
        log.debug("📅 {} créneaux générés pour le {}", creneaux.size(), date);
        return creneaux;
    }
    
    /**
     * Vérifie si un jour est ouvrable (lundi-samedi, hors jours fériés)
     */
    private boolean isJourOuvrable(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // Dimanche fermé
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        
        // Jours fériés fermés
        if (JOURS_FERIES_2025.contains(date)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Génère les créneaux pour un mois donné
     */
    public int generateCreneauxForMonth(int year, int month) {
        log.info("🚀 Génération des créneaux pour {}/{}", month, year);
        
        LocalDate debut = LocalDate.of(year, month, 1);
        LocalDate fin = debut.withDayOfMonth(debut.lengthOfMonth());
        
        List<Creneau> creneaux = generateCreneauxForPeriod(debut, fin);
        List<Creneau> savedCreneaux = creneauRepository.saveAll(creneaux);
        
        log.info("✅ {} créneaux générés pour {}/{}", savedCreneaux.size(), month, year);
        return savedCreneaux.size();
    }
    
    /**
     * Supprime tous les créneaux futurs (pour nettoyage avant regénération)
     */
    @Transactional
    public int cleanCreneauxFuturs() {
        log.info("🧹 Suppression des créneaux futurs...");
        
        Instant maintenant = Instant.now();
        List<Creneau> creneauxFuturs = creneauRepository.findAll()
                .stream()
                .filter(c -> c.getHeureDebut().isAfter(maintenant))
                .filter(c -> c.getReservations() == null || c.getReservations().isEmpty())
                .toList();
        
        creneauRepository.deleteAll(creneauxFuturs);
        
        log.info("✅ {} créneaux futurs supprimés", creneauxFuturs.size());
        return creneauxFuturs.size();
    }
} 