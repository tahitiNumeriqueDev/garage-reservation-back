package com.garage.reservation.controller;

import com.garage.reservation.dto.CreationReservationDTO;
import com.garage.reservation.dto.ReservationDTO;
import com.garage.reservation.model.StatutReservation;
import com.garage.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
@Tag(name = "Réservations", description = "Gestion des réservations de créneaux")
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    /**
     * Récupère toutes les réservations
     * GET /api/reservations
     */
    @GetMapping
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<ReservationDTO> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Récupère une réservation par ID
     * GET /api/reservations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        Optional<ReservationDTO> reservation = reservationService.getReservationById(id);
        return reservation.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Récupère les réservations par immatriculation
     * GET /api/reservations/immatriculation/{immatriculation}
     */
    @GetMapping("/immatriculation/{immatriculation}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByImmatriculation(
            @PathVariable String immatriculation) {
        List<ReservationDTO> reservations = reservationService.getReservationsByImmatriculation(immatriculation);
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Récupère les réservations par numéro de téléphone
     * GET /api/reservations/telephone/{numeroTelephone}
     */
    @GetMapping("/telephone/{numeroTelephone}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByNumeroTelephone(
            @PathVariable String numeroTelephone) {
        List<ReservationDTO> reservations = reservationService.getReservationsByPhone(numeroTelephone);
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Récupère les réservations d'un jour donné
     * GET /api/reservations/jour/{date}
     */
    @GetMapping("/jour/{date}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByDate(
            @PathVariable Instant date) {
        List<ReservationDTO> reservations = reservationService.getReservationsByDate(date);
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Récupère les réservations d'une semaine donnée
     * GET /api/reservations/semaine/{date}
     */
    @GetMapping("/semaine/{date}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByWeek(
            @PathVariable Instant date) {
        List<ReservationDTO> reservations = reservationService.getReservationsByWeek(date);
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Récupère les réservations futures
     * GET /api/reservations/futures
     */
    @GetMapping("/futures")
    public ResponseEntity<List<ReservationDTO>> getReservationsFutures() {
        List<ReservationDTO> reservations = reservationService.getReservationsFutures();
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Récupère les réservations passées
     * GET /api/reservations/passees
     */
    @GetMapping("/passees")
    public ResponseEntity<List<ReservationDTO>> getReservationsPassees() {
        List<ReservationDTO> reservations = reservationService.getReservationsPassees();
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Crée une nouvelle réservation
     * POST /api/reservations
     */
    @PostMapping
    @Operation(summary = "Crée une nouvelle réservation", 
               description = "Crée une réservation pour un créneau donné avec les informations du véhicule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réservation créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Créneau non disponible ou réservation existante")
    })
    public ResponseEntity<ReservationDTO> createReservation(
            @Parameter(description = "Informations de la réservation à créer")
            @Valid @RequestBody CreationReservationDTO creationDTO) {
        try {
            ReservationDTO reservation = reservationService.createReservation(creationDTO);
            return ResponseEntity.ok(reservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build(); // Conflict
        }
    }
    
    /**
     * Met à jour le statut d'une réservation
     * PUT /api/reservations/{id}/statut
     */
    @PutMapping("/{id}/statut")
    public ResponseEntity<ReservationDTO> updateStatutReservation(
            @PathVariable Long id,
            @RequestParam StatutReservation statut) {
        Optional<ReservationDTO> reservation = reservationService.updateReservationStatut(id, statut);
        return reservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Confirme une réservation
     * PUT /api/reservations/{id}/confirmer
     */
    @PutMapping("/{id}/confirmer")
    public ResponseEntity<ReservationDTO> confirmerReservation(@PathVariable Long id) {
        Optional<ReservationDTO> reservation = reservationService.updateReservationStatut(id, StatutReservation.CONFIRMEE);
        return reservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Annule une réservation
     * PUT /api/reservations/{id}/annuler
     */
    @PutMapping("/{id}/annuler")
    public ResponseEntity<ReservationDTO> annulerReservation(@PathVariable Long id) {
        Optional<ReservationDTO> reservation = reservationService.updateReservationStatut(id, StatutReservation.ANNULEE);
        return reservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Marque une réservation comme terminée
     * PUT /api/reservations/{id}/terminer
     */
    @PutMapping("/{id}/terminer")
    public ResponseEntity<ReservationDTO> terminerReservation(@PathVariable Long id) {
        Optional<ReservationDTO> reservation = reservationService.updateReservationStatut(id, StatutReservation.TERMINEE);
        return reservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Supprime une réservation
     * DELETE /api/reservations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        try {
            reservationService.deleteReservation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 