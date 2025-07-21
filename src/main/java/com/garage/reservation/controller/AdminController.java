package com.garage.reservation.controller;

import com.garage.reservation.service.CreneauGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Endpoints d'administration pour la gestion des créneaux")
public class AdminController {
    
    private final CreneauGenerationService creneauGenerationService;
    
    /**
     * Génère les créneaux pour juillet et août 2025
     * POST /api/admin/creneaux/generate/juillet-aout-2025
     */
    @PostMapping("/creneaux/generate/juillet-aout-2025")
    @Operation(summary = "Génère les créneaux pour juillet-août 2025", 
               description = "Crée automatiquement tous les créneaux pour les mois de juillet et août 2025 (lundi-samedi, 8h-18h, hors jours fériés)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Créneaux générés avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la génération")
    })
    public ResponseEntity<Map<String, Object>> generateCreneauxJuilletAout2025() {
        try {
            int nombreCreneaux = creneauGenerationService.generateCreneauxJuilletAout2025();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Créneaux générés avec succès pour juillet-août 2025",
                    "nombreCreneaux", nombreCreneaux,
                    "periode", "Juillet-Août 2025",
                    "horaires", "8h-18h (lundi-samedi, hors pause déjeuner 12h-14h)",
                    "capacite", "2 véhicules par créneau"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la génération des créneaux",
                    "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Génère les créneaux pour un mois donné
     * POST /api/admin/creneaux/generate/month?year=2025&month=7
     */
    @PostMapping("/creneaux/generate/month")
    @Operation(summary = "Génère les créneaux pour un mois", 
               description = "Crée automatiquement tous les créneaux pour un mois donné")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Créneaux générés avec succès"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la génération")
    })
    public ResponseEntity<Map<String, Object>> generateCreneauxForMonth(
            @Parameter(description = "Année (ex: 2025)", example = "2025")
            @RequestParam int year,
            @Parameter(description = "Mois (1-12)", example = "7")
            @RequestParam int month) {
        
        if (year < 2024 || year > 2030) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Année invalide. Doit être entre 2024 et 2030"
            ));
        }
        
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Mois invalide. Doit être entre 1 et 12"
            ));
        }
        
        try {
            int nombreCreneaux = creneauGenerationService.generateCreneauxForMonth(year, month);
            
            String[] moisNoms = {
                    "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
            };
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("Créneaux générés avec succès pour %s %d", moisNoms[month-1], year),
                    "nombreCreneaux", nombreCreneaux,
                    "periode", String.format("%s %d", moisNoms[month-1], year),
                    "year", year,
                    "month", month
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la génération des créneaux",
                    "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime tous les créneaux futurs sans réservations
     * DELETE /api/admin/creneaux/clean-future
     */
    @DeleteMapping("/creneaux/clean-future")
    @Operation(summary = "Nettoie les créneaux futurs", 
               description = "Supprime tous les créneaux futurs qui n'ont aucune réservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nettoyage effectué avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur lors du nettoyage")
    })
    public ResponseEntity<Map<String, Object>> cleanCreneauxFuturs() {
        try {
            int nombreSupprimes = creneauGenerationService.cleanCreneauxFuturs();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Nettoyage des créneaux futurs effectué avec succès",
                    "nombreSupprimes", nombreSupprimes
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur lors du nettoyage",
                    "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Workflow complet : nettoie puis génère juillet-août 2025
     * POST /api/admin/creneaux/reset-juillet-aout-2025
     */
    @PostMapping("/creneaux/reset-juillet-aout-2025")
    @Operation(summary = "Remet à zéro et génère juillet-août 2025", 
               description = "Supprime les créneaux futurs puis génère ceux de juillet-août 2025")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération effectuée avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de l'opération")
    })
    public ResponseEntity<Map<String, Object>> resetAndGenerateJuilletAout2025() {
        try {
            // Étape 1 : Nettoyage
            int nombreSupprimes = creneauGenerationService.cleanCreneauxFuturs();
            
            // Étape 2 : Génération
            int nombreCrees = creneauGenerationService.generateCreneauxJuilletAout2025();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Remise à zéro et génération effectuées avec succès",
                    "nombreSupprimes", nombreSupprimes,
                    "nombreCrees", nombreCrees,
                    "periode", "Juillet-Août 2025"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de l'opération",
                    "error", e.getMessage()
            ));
        }
    }
} 