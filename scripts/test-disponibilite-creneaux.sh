#!/bin/bash

# ============================================================================
# Script de test pour vérifier la disponibilité des créneaux après réservation
# ============================================================================
#
# Ce script teste que :
# 1. Les créneaux disponibles sont bien listés
# 2. Après une réservation, le créneau n'apparaît plus comme disponible 
#    (ou sa capacité diminue si > 1)
# 3. Après annulation, le créneau redevient disponible
#
# Usage:
#   ./test-disponibilite-creneaux.sh
# ============================================================================

set -e  # Arrêt en cas d'erreur

# Configuration
API_BASE_URL="http://localhost:8080"
DATE_TEST="2025-07-01T00:00:00Z"

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction d'affichage avec couleur
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Fonction de test de l'API
test_api_ready() {
    local max_attempts=10
    local attempt=1
    
    log_info "Vérification de la disponibilité de l'API..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "${API_BASE_URL}/api/creneaux" >/dev/null 2>&1; then
            log_success "API prête !"
            return 0
        fi
        log_warning "Tentative $attempt/$max_attempts - API pas encore prête..."
        sleep 3
        attempt=$((attempt + 1))
    done
    
    log_error "L'API n'est pas accessible après $max_attempts tentatives"
    exit 1
}

# Étape 1 : Récupérer les créneaux disponibles d'une journée
get_available_slots() {
    log_info "Récupération des créneaux disponibles pour le $DATE_TEST..."
    
    AVAILABLE_SLOTS=$(curl -s "${API_BASE_URL}/api/creneaux/jour/${DATE_TEST}?disponiblesOnly=true" | jq .)
    AVAILABLE_COUNT=$(echo "$AVAILABLE_SLOTS" | jq 'length')
    
    log_success "$AVAILABLE_COUNT créneaux disponibles trouvés"
    
    if [ "$AVAILABLE_COUNT" -eq 0 ]; then
        log_error "Aucun créneau disponible pour faire le test"
        exit 1
    fi
    
    # Prendre le premier créneau disponible
    SLOT_ID=$(echo "$AVAILABLE_SLOTS" | jq -r '.[0].id')
    SLOT_CAPACITY=$(echo "$AVAILABLE_SLOTS" | jq -r '.[0].capaciteTotale')
    SLOT_RESERVATIONS=$(echo "$AVAILABLE_SLOTS" | jq -r '.[0].nombreReservations')
    
    log_info "Créneau sélectionné pour le test:"
    log_info "  - ID: $SLOT_ID"
    log_info "  - Capacité totale: $SLOT_CAPACITY"
    log_info "  - Réservations actuelles: $SLOT_RESERVATIONS"
    
    return 0
}

# Étape 2 : Créer une réservation
create_reservation() {
    log_info "Création d'une réservation de test..."
    
    RESERVATION_JSON=$(cat <<EOF
{
  "immatriculation": "TEST-001",
  "kilometrage": 50000,
  "typeVehicule": "AUTO",
  "poidsLourd": false,
  "numeroTelephone": "0123456789",
  "email": "test@example.com",
  "creneauId": $SLOT_ID
}
EOF
)
    
    RESERVATION_RESPONSE=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "$RESERVATION_JSON" \
        "${API_BASE_URL}/api/reservations")
    
    RESERVATION_ID=$(echo "$RESERVATION_RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$RESERVATION_ID" ]; then
        log_error "Échec de la création de la réservation"
        echo "$RESERVATION_RESPONSE" | jq .
        exit 1
    fi
    
    log_success "Réservation créée avec l'ID: $RESERVATION_ID"
}

# Étape 3 : Vérifier que la disponibilité a changé
check_availability_after_reservation() {
    log_info "Vérification de la disponibilité après réservation..."
    
    # Récupérer à nouveau les créneaux disponibles
    AVAILABLE_SLOTS_AFTER=$(curl -s "${API_BASE_URL}/api/creneaux/jour/${DATE_TEST}?disponiblesOnly=true")
    AVAILABLE_COUNT_AFTER=$(echo "$AVAILABLE_SLOTS_AFTER" | jq 'length')
    
    # Récupérer les détails du créneau spécifique
    SLOT_DETAILS=$(curl -s "${API_BASE_URL}/api/creneaux/${SLOT_ID}")
    SLOT_RESERVATIONS_AFTER=$(echo "$SLOT_DETAILS" | jq -r '.nombreReservations')
    
    log_info "État après réservation:"
    log_info "  - Créneaux disponibles: $AVAILABLE_COUNT_AFTER"
    log_info "  - Réservations sur le créneau: $SLOT_RESERVATIONS_AFTER"
    
    # Vérifications
    if [ "$SLOT_CAPACITY" -eq 1 ]; then
        # Capacité 1 : le créneau ne doit plus être disponible
        SLOT_STILL_AVAILABLE=$(echo "$AVAILABLE_SLOTS_AFTER" | jq --arg slot_id "$SLOT_ID" 'any(.[]; .id == ($slot_id | tonumber))')
        
        if [ "$SLOT_STILL_AVAILABLE" = "false" ]; then
            log_success "✅ Créneau correctement retiré des disponibilités (capacité=1)"
        else
            log_error "❌ Le créneau devrait être indisponible mais il apparaît encore"
            return 1
        fi
    else
        # Capacité > 1 : vérifier que le nombre de réservations a augmenté
        if [ "$SLOT_RESERVATIONS_AFTER" -gt "$SLOT_RESERVATIONS" ]; then
            log_success "✅ Nombre de réservations correctement incrémenté"
        else
            log_error "❌ Le nombre de réservations n'a pas augmenté"
            return 1
        fi
        
        # Si le créneau est maintenant complet, il ne doit plus apparaître
        if [ "$SLOT_RESERVATIONS_AFTER" -ge "$SLOT_CAPACITY" ]; then
            SLOT_STILL_AVAILABLE=$(echo "$AVAILABLE_SLOTS_AFTER" | jq --arg slot_id "$SLOT_ID" 'any(.[]; .id == ($slot_id | tonumber))')
            if [ "$SLOT_STILL_AVAILABLE" = "false" ]; then
                log_success "✅ Créneau complet correctement retiré des disponibilités"
            else
                log_error "❌ Le créneau complet devrait être indisponible"
                return 1
            fi
        fi
    fi
    
    return 0
}

# Étape 4 : Annuler la réservation
cancel_reservation() {
    log_info "Annulation de la réservation de test..."
    
    CANCEL_RESPONSE=$(curl -s -X PUT "${API_BASE_URL}/api/reservations/${RESERVATION_ID}/annuler")
    CANCEL_STATUS=$(echo "$CANCEL_RESPONSE" | jq -r '.statut // empty')
    
    if [ "$CANCEL_STATUS" = "ANNULEE" ]; then
        log_success "Réservation annulée avec succès"
    else
        log_error "Échec de l'annulation de la réservation"
        echo "$CANCEL_RESPONSE" | jq .
        return 1
    fi
}

# Étape 5 : Vérifier que la disponibilité est restaurée
check_availability_after_cancellation() {
    log_info "Vérification de la disponibilité après annulation..."
    
    # Attendre un peu pour que les changements soient pris en compte
    sleep 2
    
    # Récupérer à nouveau les créneaux disponibles
    AVAILABLE_SLOTS_FINAL=$(curl -s "${API_BASE_URL}/api/creneaux/jour/${DATE_TEST}?disponiblesOnly=true")
    AVAILABLE_COUNT_FINAL=$(echo "$AVAILABLE_SLOTS_FINAL" | jq 'length')
    
    # Récupérer les détails du créneau spécifique
    SLOT_DETAILS_FINAL=$(curl -s "${API_BASE_URL}/api/creneaux/${SLOT_ID}")
    SLOT_RESERVATIONS_FINAL=$(echo "$SLOT_DETAILS_FINAL" | jq -r '.nombreReservations')
    
    log_info "État après annulation:"
    log_info "  - Créneaux disponibles: $AVAILABLE_COUNT_FINAL"
    log_info "  - Réservations actives sur le créneau: $SLOT_RESERVATIONS_FINAL"
    
    # Vérifications
    if [ "$SLOT_RESERVATIONS_FINAL" -eq "$SLOT_RESERVATIONS" ]; then
        log_success "✅ Nombre de réservations restauré à l'état initial"
    else
        log_error "❌ Le nombre de réservations n'est pas revenu à l'état initial"
        return 1
    fi
    
    # Le créneau doit être à nouveau disponible
    SLOT_AVAILABLE_AGAIN=$(echo "$AVAILABLE_SLOTS_FINAL" | jq --arg slot_id "$SLOT_ID" 'any(.[]; .id == ($slot_id | tonumber))')
    
    if [ "$SLOT_AVAILABLE_AGAIN" = "true" ]; then
        log_success "✅ Créneau correctement restauré dans les disponibilités"
    else
        log_error "❌ Le créneau devrait être à nouveau disponible"
        return 1
    fi
    
    return 0
}

# Étape 6 : Nettoyage
cleanup() {
    log_info "Nettoyage des données de test..."
    
    # Supprimer la réservation de test si elle existe encore
    if [ ! -z "$RESERVATION_ID" ]; then
        curl -s -X DELETE "${API_BASE_URL}/api/reservations/${RESERVATION_ID}" >/dev/null || true
        log_success "Réservation de test supprimée"
    fi
}

# Fonction principale
main() {
    echo "🧪 Test de disponibilité des créneaux après réservation"
    echo "======================================================="
    
    # Test de l'API
    test_api_ready
    
    # Variables globales pour le test
    AVAILABLE_SLOTS=""
    AVAILABLE_COUNT=0
    SLOT_ID=""
    SLOT_CAPACITY=0
    SLOT_RESERVATIONS=0
    RESERVATION_ID=""
    
    # Exécution des tests
    if get_available_slots && \
       create_reservation && \
       check_availability_after_reservation && \
       cancel_reservation && \
       check_availability_after_cancellation; then
        
        log_success "🎉 Tous les tests sont passés avec succès !"
        echo ""
        echo "📋 Résumé des fonctionnalités testées :"
        echo "✅ Listage des créneaux disponibles"
        echo "✅ Création de réservation"
        echo "✅ Mise à jour de la disponibilité après réservation"
        echo "✅ Annulation de réservation"
        echo "✅ Restauration de la disponibilité après annulation"
        
    else
        log_error "❌ Au moins un test a échoué"
        cleanup
        exit 1
    fi
    
    # Nettoyage final
    cleanup
    
    echo ""
    log_success "🚀 Système de disponibilité des créneaux opérationnel !"
}

# Exécution
main "$@" 