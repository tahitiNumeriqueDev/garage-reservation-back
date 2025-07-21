package com.garage.reservation.service;

import com.garage.reservation.dto.CreneauDTO;
import com.garage.reservation.mapper.CreneauMapper;
import com.garage.reservation.model.Creneau;
import com.garage.reservation.repository.CreneauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreneauService {
    
    private final CreneauRepository creneauRepository;
    private final CreneauMapper creneauMapper;
    
    /**
     * Récupère tous les créneaux
     */
    public List<CreneauDTO> getAllCreneaux() {
        return creneauRepository.findAll()
                .stream()
                .map(creneauMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère un créneau par son ID
     */
    public Optional<CreneauDTO> getCreneauById(Long id) {
        return creneauRepository.findById(id)
                .map(creneauMapper::toDTO);
    }
    
    /**
     * Récupère les créneaux d'un jour donné
     */
    public List<CreneauDTO> getCreneauxByDate(Instant date) {
        return creneauRepository.findCreneauxByDate(date)
                .stream()
                .map(creneauMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux disponibles d'un jour donné
     */
    public List<CreneauDTO> getCreneauxDisponiblesByDate(Instant date) {
        return creneauRepository.findCreneauxDisponiblesByDate(date)
                .stream()
                .filter(creneau -> creneau.estDisponible())
                .map(creneauMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux d'une semaine donnée
     */
    public List<CreneauDTO> getCreneauxByWeek(Instant date) {
        return creneauRepository.findCreneauxByWeek(date)
                .stream()
                .map(creneauMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux disponibles d'une semaine donnée
     */
    public List<CreneauDTO> getCreneauxDisponiblesByWeek(Instant date) {
        return creneauRepository.findCreneauxDisponiblesByWeek(date)
                .stream()
                .filter(creneau -> creneau.estDisponible())
                .map(creneauMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux entre deux dates
     */
    public List<CreneauDTO> getCreneauxBetweenDates(Instant dateDebut, Instant dateFin) {
        return creneauRepository.findCreneauxBetweenDates(dateDebut, dateFin)
                .stream()
                .map(creneauMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux disponibles entre deux dates
     */
    public List<CreneauDTO> getCreneauxDisponiblesBetweenDates(Instant dateDebut, Instant dateFin) {
        return creneauRepository.findCreneauxDisponiblesBetweenDates(dateDebut, dateFin)
                .stream()
                .filter(creneau -> creneau.estDisponible())
                .map(creneauMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Crée un nouveau créneau
     */
    public CreneauDTO createCreneau(Instant heureDebut, Instant heureFin, Integer capacite) {
        Creneau creneau = Creneau.builder()
                .heureDebut(heureDebut)
                .heureFin(heureFin)
                .capaciteTotale(capacite != null ? capacite : 1)
                .build();
        creneau = creneauRepository.save(creneau);
        return creneauMapper.toDTO(creneau);
    }
    
    /**
     * Génère des créneaux entre deux instants donnés
     */
    public List<CreneauDTO> generateCreneauxBetweenInstants(Instant debut, Instant fin, long dureeMinutes) {
        List<CreneauDTO> creneaux = new java.util.ArrayList<>();
        
        Instant current = debut;
        
        while (current.plusSeconds(dureeMinutes * 60).isBefore(fin) || 
               current.plusSeconds(dureeMinutes * 60).equals(fin)) {
            Instant finCreneau = current.plusSeconds(dureeMinutes * 60);
            CreneauDTO creneau = createCreneau(current, finCreneau, 1);
            creneaux.add(creneau);
            current = finCreneau;
        }
        
        return creneaux;
    }
} 