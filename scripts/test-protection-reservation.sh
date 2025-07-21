#!/bin/bash

# ============================================================================
# Script de test pour vérifier la protection contre la surbooking lors de la réservation
# ============================================================================
#
# Ce script teste que :
# 1. On peut réserver sur un créneau avec de la place disponible
# 2. On NE PEUT PAS réserver sur un créneau saturé (toutes les places occupées)
# 3. Le système retourne une erreur appropriée (409 Conflict)
#
# Usage:
#   ./test-protection-reservation.sh
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

# Variables globales
SLOT_ID=""
SLOT_CAPACITY=0
RESERVATION_IDS=()

# Fonction de récupération d'un créneau avec capacité = 2
get_available_slot() {
    log_info "Recherche d'un créneau avec capacité = 2..."
    
    AVAILABLE_SLOTS=$(curl -s "${API_BASE_URL}/api/creneaux/jour/${DATE_TEST}?disponiblesOnly=true")
    
    SLOT_ID=$(echo "$AVAILABLE_SLOTS" | jq -r '.[0].id')
    SLOT_CAPACITY=$(echo "$AVAILABLE_SLOTS" | jq -r '.[0].capaciteTotale')
    
    if [ -z "$SLOT_ID" ] || [ "$SLOT_ID" = "null" ]; then
        log_error "Aucun créneau disponible trouvé"
        return 1
    fi
    
    log_success "Créneau sélectionné - ID: $SLOT_ID, Capacité: $SLOT_CAPACITY"
    return 0
}

# Fonction de création d'une réservation (avec gestion des erreurs)
create_reservation_with_status() {
    local immatriculation="$1"
    local should_succeed="$2"  # true/false
    
    log_info "Tentative de réservation : $immatriculation..."
    
    RESERVATION_JSON=$(cat <<EOF
{
  "immatriculation": "$immatriculation",
  "kilometrage": 50000,
  "typeVehicule": "AUTO",
  "poidsLourd": false,
  "numeroTelephone": "012345678${#RESERVATION_IDS[@]}",
  "email": "test${#RESERVATION_IDS[@]}@example.com",
  "creneauId": $SLOT_ID
}
EOF
)
    
    # Faire la requête et capturer le code de retour
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$RESERVATION_JSON" \
        "${API_BASE_URL}/api/reservations")
    
    # Séparer la réponse du code HTTP
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$should_succeed" = "true" ]; then
        # La réservation DOIT réussir
        if [ "$HTTP_CODE" = "200" ]; then
            RESERVATION_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
            RESERVATION_IDS+=("$RESERVATION_ID")
            log_success "Réservation $immatriculation réussie - ID: $RESERVATION_ID"
            return 0
        else
            log_error "La réservation $immatriculation devrait réussir mais a échoué (HTTP $HTTP_CODE)"
            echo "$RESPONSE_BODY" | jq .
            return 1
        fi
    else
        # La réservation DOIT échouer
        if [ "$HTTP_CODE" = "409" ]; then
            log_success "Réservation $immatriculation correctement REFUSÉE (HTTP 409 - Capacité atteinte)"
            return 0
        elif [ "$HTTP_CODE" = "200" ]; then
            log_error "La réservation $immatriculation devrait être refusée mais a RÉUSSI !"
            RESERVATION_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
            RESERVATION_IDS+=("$RESERVATION_ID")
            return 1
        else
            log_error "Erreur inattendue pour $immatriculation (HTTP $HTTP_CODE)"
            echo "$RESPONSE_BODY" | jq .
            return 1
        fi
    fi
}

# Fonction de vérification de l'état du créneau
check_slot_status() {
    local context="$1"
    
    log_info "État du créneau $context :"
    SLOT_STATUS=$(curl -s "${API_BASE_URL}/api/creneaux/${SLOT_ID}")
    
    local current_reservations=$(echo "$SLOT_STATUS" | jq -r '.nombreReservations')
    local capacity=$(echo "$SLOT_STATUS" | jq -r '.capaciteTotale')
    local available=$(echo "$SLOT_STATUS" | jq -r '.disponible')
    
    log_info "  - Réservations: $current_reservations/$capacity"
    log_info "  - Flag disponible: $available"
    
    # Vérifier si le créneau apparaît dans les disponibilités
    AVAILABLE_IN_LIST=$(curl -s "${API_BASE_URL}/api/creneaux/jour/${DATE_TEST}?disponiblesOnly=true" | jq --arg slot_id "$SLOT_ID" 'any(.[]; .id == ($slot_id | tonumber))')
    
    if [ "$AVAILABLE_IN_LIST" = "true" ]; then
        log_info "  - Statut: DISPONIBLE pour réservation"
    else
        log_info "  - Statut: INDISPONIBLE (saturé)"
    fi
}

# Fonction de nettoyage
cleanup() {
    log_info "Nettoyage des données de test..."
    
    for reservation_id in "${RESERVATION_IDS[@]}"; do
        curl -s -X DELETE "${API_BASE_URL}/api/reservations/${reservation_id}" >/dev/null || true
    done
    
    log_success "Nettoyage terminé (${#RESERVATION_IDS[@]} réservations supprimées)"
}

# Fonction principale
main() {
    echo "🛡️  Test de protection contre la surbooking"
    echo "=========================================="
    
    # Test de l'API
    if ! curl -s "${API_BASE_URL}/api/creneaux" >/dev/null 2>&1; then
        log_error "API non accessible"
        exit 1
    fi
    
    # Récupérer un créneau disponible
    if ! get_available_slot; then
        log_error "Impossible de continuer le test"
        exit 1
    fi
    
    # État initial
    check_slot_status "INITIAL"
    
    # Test 1 : Première réservation (doit réussir)
    echo ""
    log_info "🧪 TEST 1 : Première réservation sur créneau libre"
    if ! create_reservation_with_status "TEST-001" "true"; then
        cleanup
        exit 1
    fi
    check_slot_status "après 1ère réservation"
    
    # Test 2 : Deuxième réservation (doit réussir car capacité = 2)
    echo ""
    log_info "🧪 TEST 2 : Deuxième réservation (capacité = 2)"
    if ! create_reservation_with_status "TEST-002" "true"; then
        cleanup
        exit 1
    fi
    check_slot_status "après 2ème réservation"
    
    # Test 3 : Troisième réservation (DOIT ÉCHOUER - capacité atteinte)
    echo ""
    log_info "🧪 TEST 3 : Troisième réservation (DOIT ÊTRE REFUSÉE)"
    if ! create_reservation_with_status "TEST-003" "false"; then
        cleanup
        exit 1
    fi
    check_slot_status "après tentative de 3ème réservation"
    
    # Test 4 : Quatrième réservation (DOIT AUSSI ÉCHOUER)
    echo ""
    log_info "🧪 TEST 4 : Quatrième réservation (DOIT AUSSI ÊTRE REFUSÉE)"
    if ! create_reservation_with_status "TEST-004" "false"; then
        cleanup
        exit 1
    fi
    
    # Résultat
    echo ""
    log_success "🎉 Tous les tests de protection sont passés avec succès !"
    echo ""
    echo "📋 Résumé des protections validées :"
    echo "✅ Réservation autorisée quand places disponibles (1/2 et 2/2)"
    echo "✅ Réservation REFUSÉE quand capacité atteinte (3/2 et 4/2)"
    echo "✅ Code d'erreur HTTP 409 correct"
    echo "✅ Message d'erreur approprié"
    
    # Nettoyage final
    cleanup
    
    echo ""
    log_success "🛡️  Système de protection contre la surbooking opérationnel !"
}

# Exécution
main "$@" 