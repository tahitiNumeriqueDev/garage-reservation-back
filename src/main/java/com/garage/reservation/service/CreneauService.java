package com.garage.reservation.service;

import com.garage.reservation.dto.CreneauDTO;
import com.garage.reservation.model.Creneau;
import com.garage.reservation.repository.CreneauRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CreneauService {
    
    @Autowired
    private CreneauRepository creneauRepository;
    
    /**
     * Convertit un Creneau en CreneauDTO
     */
    private CreneauDTO convertToDTO(Creneau creneau) {
        return new CreneauDTO(
            creneau.getId(),
            creneau.getHeureDebut(),
            creneau.getHeureFin(),
            creneau.estDisponible(),
            creneau.getCapaciteTotale(),
            creneau.getNombreReservations()
        );
    }
    
    /**
     * Récupère tous les créneaux
     */
    public List<CreneauDTO> getAllCreneaux() {
        return creneauRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux d'un jour donné
     */
    public List<CreneauDTO> getCreneauxByDate(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return creneauRepository.findCreneauxByDate(dateTime)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux disponibles d'un jour donné
     */
    public List<CreneauDTO> getCreneauxDisponiblesByDate(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return creneauRepository.findCreneauxDisponiblesByDate(dateTime)
                .stream()
                .filter(creneau -> creneau.estDisponible())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux d'une semaine donnée
     */
    public List<CreneauDTO> getCreneauxByWeek(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return creneauRepository.findCreneauxByWeek(dateTime)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux disponibles d'une semaine donnée
     */
    public List<CreneauDTO> getCreneauxDisponiblesByWeek(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return creneauRepository.findCreneauxDisponiblesByWeek(dateTime)
                .stream()
                .filter(creneau -> creneau.estDisponible())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux entre deux dates
     */
    public List<CreneauDTO> getCreneauxBetweenDates(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return creneauRepository.findCreneauxBetweenDates(dateDebut, dateFin)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux disponibles entre deux dates
     */
    public List<CreneauDTO> getCreneauxDisponiblesBetweenDates(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return creneauRepository.findCreneauxDisponiblesBetweenDates(dateDebut, dateFin)
                .stream()
                .filter(creneau -> creneau.estDisponible())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère un créneau par ID
     */
    public Optional<CreneauDTO> getCreneauById(Long id) {
        return creneauRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère l'entité Creneau par ID (pour usage interne)
     */
    public Optional<Creneau> getCreneauEntityById(Long id) {
        return creneauRepository.findById(id);
    }
    
    /**
     * Vérifie si un créneau est disponible pour une nouvelle réservation
     */
    public boolean isCreneauDisponible(Long creneauId) {
        return !creneauRepository.findCreneauDisponible(creneauId).isEmpty();
    }
    
    /**
     * Crée un nouveau créneau
     */
    public CreneauDTO createCreneau(LocalDateTime heureDebut, LocalDateTime heureFin, Integer capacite) {
        Creneau creneau = new Creneau(heureDebut, heureFin);
        if (capacite != null) {
            creneau.setCapaciteTotale(capacite);
        }
        creneau = creneauRepository.save(creneau);
        return convertToDTO(creneau);
    }
    
    /**
     * Génère des créneaux pour une journée donnée
     */
    public List<CreneauDTO> generateCreneauxForDay(LocalDate date, LocalTime heureDebut, LocalTime heureFin, int dureeMinutes) {
        List<CreneauDTO> creneaux = new java.util.ArrayList<>();
        
        LocalDateTime current = LocalDateTime.of(date, heureDebut);
        LocalDateTime finJournee = LocalDateTime.of(date, heureFin);
        
        while (current.plusMinutes(dureeMinutes).isBefore(finJournee) || 
               current.plusMinutes(dureeMinutes).isEqual(finJournee)) {
            LocalDateTime finCreneau = current.plusMinutes(dureeMinutes);
            CreneauDTO creneau = createCreneau(current, finCreneau, 1);
            creneaux.add(creneau);
            current = finCreneau;
        }
        
        return creneaux;
    }
} 