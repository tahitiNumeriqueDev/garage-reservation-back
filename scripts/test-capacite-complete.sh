#!/bin/bash

# ============================================================================
# Script de test pour vérifier qu'un créneau devient indisponible 
# quand sa capacité est complètement occupée
# ============================================================================
#
# Ce script teste que :
# 1. Un créneau avec capacité=2 reste disponible après 1 réservation
# 2. Le même créneau devient indisponible après 2 réservations
# 3. Le créneau redevient disponible après annulation d'une réservation
#
# Usage:
#   ./test-capacite-complete.sh
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

# Variables globales pour les réservations
RESERVATION_ID_1=""
RESERVATION_ID_2=""
SLOT_ID=""
SLOT_CAPACITY=0

# Fonction de récupération d'un créneau avec capacité >= 2
get_slot_with_capacity_2() {
    log_info "Recherche d'un créneau avec capacité >= 2..."
    
    AVAILABLE_SLOTS=$(curl -s "${API_BASE_URL}/api/creneaux/jour/${DATE_TEST}?disponiblesOnly=true")
    
    # Filtrer les créneaux avec capacité >= 2
    SLOT_WITH_CAPACITY=$(echo "$AVAILABLE_SLOTS" | jq '.[] | select(.capaciteTotale >= 2) | select(.nombreReservations == 0) | .id' | head -1)
    
    if [ -z "$SLOT_WITH_CAPACITY" ] || [ "$SLOT_WITH_CAPACITY" = "null" ]; then
        log_error "Aucun créneau avec capacité >= 2 et sans réservation trouvé"
        return 1
    fi
    
    SLOT_ID=$SLOT_WITH_CAPACITY
    
    # Récupérer les détails du créneau
    SLOT_DETAILS=$(curl -s "${API_BASE_URL}/api/creneaux/${SLOT_ID}")
    SLOT_CAPACITY=$(echo "$SLOT_DETAILS" | jq -r '.capaciteTotale')
    
    log_success "Créneau trouvé - ID: $SLOT_ID, Capacité: $SLOT_CAPACITY"
    return 0
}

# Fonction de création d'une réservation
create_reservation() {
    local immatriculation="$1"
    local email="test${immatriculation}@example.com"
    
    log_info "Création de la réservation $immatriculation..."
    
    RESERVATION_JSON=$(cat <<EOF
{
  "immatriculation": "$immatriculation",
  "kilometrage": 50000,
  "typeVehicule": "AUTO",
  "poidsLourd": false,
  "numeroTelephone": "012345678${immatriculation: -1}",
  "email": "$email",
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
        log_error "Échec de la création de la réservation $immatriculation"
        echo "$RESERVATION_RESPONSE" | jq .
        return 1
    fi
    
    log_success "Réservation $immatriculation créée avec l'ID: $RESERVATION_ID"
    echo "$RESERVATION_ID"
}

# Fonction de vérification de disponibilité
check_availability() {
    local expected_available="$1"
    local context="$2"
    
    log_info "Vérification de la disponibilité ($context)..."
    
    # Récupérer les créneaux disponibles
    AVAILABLE_SLOTS=$(curl -s "${API_BASE_URL}/api/creneaux/jour/${DATE_TEST}?disponiblesOnly=true")
    
    # Vérifier si notre créneau est dans la liste
    SLOT_AVAILABLE=$(echo "$AVAILABLE_SLOTS" | jq --arg slot_id "$SLOT_ID" 'any(.[]; .id == ($slot_id | tonumber))')
    
    # Récupérer les détails du créneau
    SLOT_DETAILS=$(curl -s "${API_BASE_URL}/api/creneaux/${SLOT_ID}")
    CURRENT_RESERVATIONS=$(echo "$SLOT_DETAILS" | jq -r '.nombreReservations')
    
    log_info "État du créneau: $CURRENT_RESERVATIONS/$SLOT_CAPACITY réservations"
    
    if [ "$SLOT_AVAILABLE" = "$expected_available" ]; then
        if [ "$expected_available" = "true" ]; then
            log_success "✅ Créneau correctement disponible"
        else
            log_success "✅ Créneau correctement indisponible (capacité atteinte)"
        fi
        return 0
    else
        if [ "$expected_available" = "true" ]; then
            log_error "❌ Le créneau devrait être disponible mais ne l'est pas"
        else
            log_error "❌ Le créneau devrait être indisponible mais il apparaît comme disponible"
        fi
        return 1
    fi
}

# Fonction d'annulation d'une réservation
cancel_reservation() {
    local reservation_id="$1"
    
    log_info "Annulation de la réservation ID $reservation_id..."
    
    CANCEL_RESPONSE=$(curl -s -X PUT "${API_BASE_URL}/api/reservations/${reservation_id}/annuler")
    CANCEL_STATUS=$(echo "$CANCEL_RESPONSE" | jq -r '.statut // empty')
    
    if [ "$CANCEL_STATUS" = "ANNULEE" ]; then
        log_success "Réservation $reservation_id annulée avec succès"
        return 0
    else
        log_error "Échec de l'annulation de la réservation $reservation_id"
        echo "$CANCEL_RESPONSE" | jq .
        return 1
    fi
}

# Fonction de nettoyage
cleanup() {
    log_info "Nettoyage des données de test..."
    
    if [ ! -z "$RESERVATION_ID_1" ]; then
        curl -s -X DELETE "${API_BASE_URL}/api/reservations/${RESERVATION_ID_1}" >/dev/null || true
    fi
    
    if [ ! -z "$RESERVATION_ID_2" ]; then
        curl -s -X DELETE "${API_BASE_URL}/api/reservations/${RESERVATION_ID_2}" >/dev/null || true
    fi
    
    log_success "Nettoyage terminé"
}

# Fonction principale
main() {
    echo "🧪 Test de capacité complète des créneaux"
    echo "========================================="
    
    # Test de l'API
    if ! curl -s "${API_BASE_URL}/api/creneaux" >/dev/null 2>&1; then
        log_error "API non accessible"
        exit 1
    fi
    
    # Récupérer un créneau avec capacité >= 2
    if ! get_slot_with_capacity_2; then
        log_error "Impossible de continuer le test"
        exit 1
    fi
    
    # Vérifier que le créneau est initialement disponible
    if ! check_availability "true" "état initial"; then
        cleanup
        exit 1
    fi
    
    # Créer la première réservation
    RESERVATION_ID_1=$(create_reservation "TEST-001")
    if [ $? -ne 0 ]; then
        cleanup
        exit 1
    fi
    
    # Vérifier que le créneau est toujours disponible
    if ! check_availability "true" "après 1ère réservation"; then
        cleanup
        exit 1
    fi
    
    # Créer la deuxième réservation
    RESERVATION_ID_2=$(create_reservation "TEST-002")
    if [ $? -ne 0 ]; then
        cleanup
        exit 1
    fi
    
    # Vérifier que le créneau n'est plus disponible
    if ! check_availability "false" "après 2ème réservation (capacité atteinte)"; then
        cleanup
        exit 1
    fi
    
    # Annuler une réservation
    if ! cancel_reservation "$RESERVATION_ID_1"; then
        cleanup
        exit 1
    fi
    
    # Attendre un peu pour que les changements soient pris en compte
    sleep 2
    
    # Vérifier que le créneau est à nouveau disponible
    if ! check_availability "true" "après annulation d'une réservation"; then
        cleanup
        exit 1
    fi
    
    log_success "🎉 Tous les tests sont passés avec succès !"
    echo ""
    echo "📋 Résumé des fonctionnalités testées :"
    echo "✅ Créneau reste disponible si capacité non atteinte"
    echo "✅ Créneau devient indisponible quand capacité atteinte"
    echo "✅ Créneau redevient disponible après annulation"
    
    # Nettoyage final
    cleanup
    
    echo ""
    log_success "🚀 Système de gestion de capacité des créneaux opérationnel !"
}

# Exécution
main "$@" 