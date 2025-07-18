package com.garage.reservation.controller;

import com.garage.reservation.dto.CreneauDTO;
import com.garage.reservation.service.CreneauService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/creneaux")
@CrossOrigin(origins = "*")
@Tag(name = "Créneaux", description = "Gestion des créneaux horaires disponibles")
public class CreneauController {
    
    @Autowired
    private CreneauService creneauService;
    
    /**
     * Récupère tous les créneaux
     * GET /api/creneaux
     */
    @GetMapping
    @Operation(summary = "Récupère tous les créneaux", description = "Retourne la liste complète de tous les créneaux")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des créneaux récupérée avec succès")
    })
    public ResponseEntity<List<CreneauDTO>> getAllCreneaux() {
        List<CreneauDTO> creneaux = creneauService.getAllCreneaux();
        return ResponseEntity.ok(creneaux);
    }
    
    /**
     * Récupère un créneau par ID
     * GET /api/creneaux/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreneauDTO> getCreneauById(@PathVariable Long id) {
        Optional<CreneauDTO> creneau = creneauService.getCreneauById(id);
        return creneau.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Récupère les créneaux d'un jour donné
     * GET /api/creneaux/jour/{date}
     */
    @GetMapping("/jour/{date}")
    @Operation(summary = "Récupère les créneaux d'un jour", description = "Retourne tous les créneaux ou seulement les disponibles pour une date donnée")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des créneaux du jour récupérée avec succès")
    })
    public ResponseEntity<List<CreneauDTO>> getCreneauxByDate(
            @Parameter(description = "Date au format YYYY-MM-DD", example = "2024-12-23")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Filtrer seulement les créneaux disponibles", example = "true")
            @RequestParam(defaultValue = "false") boolean disponiblesOnly) {
        
        List<CreneauDTO> creneaux;
        if (disponiblesOnly) {
            creneaux = creneauService.getCreneauxDisponiblesByDate(date);
        } else {
            creneaux = creneauService.getCreneauxByDate(date);
        }
        return ResponseEntity.ok(creneaux);
    }
    
    /**
     * Récupère les créneaux d'une semaine donnée
     * GET /api/creneaux/semaine/{date}
     */
    @GetMapping("/semaine/{date}")
    public ResponseEntity<List<CreneauDTO>> getCreneauxByWeek(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "false") boolean disponiblesOnly) {
        
        List<CreneauDTO> creneaux;
        if (disponiblesOnly) {
            creneaux = creneauService.getCreneauxDisponiblesByWeek(date);
        } else {
            creneaux = creneauService.getCreneauxByWeek(date);
        }
        return ResponseEntity.ok(creneaux);
    }
    
    /**
     * Récupère les créneaux entre deux dates
     * GET /api/creneaux/periode?dateDebut=2024-01-01T08:00:00&dateFin=2024-01-01T18:00:00
     */
    @GetMapping("/periode")
    public ResponseEntity<List<CreneauDTO>> getCreneauxBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @RequestParam(defaultValue = "false") boolean disponiblesOnly) {
        
        List<CreneauDTO> creneaux;
        if (disponiblesOnly) {
            creneaux = creneauService.getCreneauxDisponiblesBetweenDates(dateDebut, dateFin);
        } else {
            creneaux = creneauService.getCreneauxBetweenDates(dateDebut, dateFin);
        }
        return ResponseEntity.ok(creneaux);
    }
    
    /**
     * Récupère les créneaux disponibles seulement
     * GET /api/creneaux/disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<CreneauDTO>> getCreneauxDisponibles() {
        List<CreneauDTO> creneaux = creneauService.getAllCreneaux()
                .stream()
                .filter(CreneauDTO::getDisponible)
                .toList();
        return ResponseEntity.ok(creneaux);
    }
    
    /**
     * Génère des créneaux pour une journée
     * POST /api/creneaux/generer-jour
     */
    @PostMapping("/generer-jour")
    public ResponseEntity<List<CreneauDTO>> generateCreneauxForDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureFin,
            @RequestParam int dureeMinutes) {
        
        try {
            List<CreneauDTO> creneaux = creneauService.generateCreneauxForDay(date, heureDebut, heureFin, dureeMinutes);
            return ResponseEntity.ok(creneaux);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Crée un nouveau créneau
     * POST /api/creneaux
     */
    @PostMapping
    public ResponseEntity<CreneauDTO> createCreneau(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime heureDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime heureFin,
            @RequestParam(defaultValue = "1") Integer capacite) {
        
        try {
            CreneauDTO creneau = creneauService.createCreneau(heureDebut, heureFin, capacite);
            return ResponseEntity.ok(creneau);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Vérifie si un créneau est disponible
     * GET /api/creneaux/{id}/disponible
     */
    @GetMapping("/{id}/disponible")
    public ResponseEntity<Boolean> isCreneauDisponible(@PathVariable Long id) {
        boolean disponible = creneauService.isCreneauDisponible(id);
        return ResponseEntity.ok(disponible);
    }
} 