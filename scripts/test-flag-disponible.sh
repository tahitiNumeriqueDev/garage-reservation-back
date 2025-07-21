#!/bin/bash

# ============================================================================
# Script de test pour vérifier la gestion automatique du flag 'disponible'
# ============================================================================
#
# Ce script teste que :
# 1. Le flag 'disponible' reste 'true' tant qu'il y a de la place
# 2. Le flag 'disponible' passe à 'false' quand la capacité est atteinte
# 3. Le flag 'disponible' repasse à 'true' quand une réservation est annulée
# 4. Le comportement est cohérent dans toutes les API
#
# Usage:
#   ./test-flag-disponible.sh
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

# Fonction de création d'une réservation
create_reservation() {
    local immatriculation="$1"
    
    log_info "Création de réservation : $immatriculation..."
    
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
    
    RESPONSE=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "$RESERVATION_JSON" \
        "${API_BASE_URL}/api/reservations")
    
    RESERVATION_ID=$(echo "$RESPONSE" | jq -r '.id')
    RESERVATION_IDS+=("$RESERVATION_ID")
    log_success "Réservation $immatriculation créée - ID: $RESERVATION_ID"
    
    return 0
}

# Fonction de vérification détaillée du flag disponible
check_disponible_flag() {
    local context="$1"
    local expected_disponible="$2"  # "true" ou "false"
    
    log_info "🔍 Vérification du flag disponible $context :"
    
    # 1. Vérification via API individuelle du créneau
    SLOT_DETAIL=$(curl -s "${API_BASE_URL}/api/creneaux/${SLOT_ID}")
    DISPONIBLE_FLAG=$(echo "$SLOT_DETAIL" | jq -r '.disponible')
    RESERVATIONS_COUNT=$(echo "$SLOT_DETAIL" | jq -r '.nombreReservations')
    CAPACITY=$(echo "$SLOT_DETAIL" | jq -r '.capaciteTotale')
    
    log_info "  📊 État du créneau :"
    log_info "    - Flag disponible: $DISPONIBLE_FLAG"
    log_info "    - Réservations: $RESERVATIONS_COUNT/$CAPACITY"
    
    # 2. Vérification via API de disponibilités
    AVAILABLE_IN_LIST=$(curl -s "${API_BASE_URL}/api/creneaux/jour/${DATE_TEST}?disponiblesOnly=true" | jq --arg slot_id "$SLOT_ID" 'any(.[]; .id == ($slot_id | tonumber))')
    log_info "    - Présent dans disponibilités: $AVAILABLE_IN_LIST"
    
    # 3. Vérifications de cohérence
    local tests_passed=0
    local total_tests=3
    
    # Test 1: Flag disponible correct
    if [ "$DISPONIBLE_FLAG" = "$expected_disponible" ]; then
        log_success "  ✅ Flag disponible correct: $DISPONIBLE_FLAG"
        tests_passed=$((tests_passed + 1))
    else
        log_error "  ❌ Flag disponible incorrect: attendu $expected_disponible, obtenu $DISPONIBLE_FLAG"
    fi
    
    # Test 2: Cohérence avec le listing des disponibilités
    if [ "$expected_disponible" = "true" ] && [ "$AVAILABLE_IN_LIST" = "true" ]; then
        log_success "  ✅ Cohérence disponibilités: présent dans la liste"
        tests_passed=$((tests_passed + 1))
    elif [ "$expected_disponible" = "false" ] && [ "$AVAILABLE_IN_LIST" = "false" ]; then
        log_success "  ✅ Cohérence disponibilités: absent de la liste"
        tests_passed=$((tests_passed + 1))
    else
        log_error "  ❌ Incohérence: flag=$DISPONIBLE_FLAG mais liste=$AVAILABLE_IN_LIST"
    fi
    
    # Test 3: Cohérence logique avec les réservations
    if [ "$RESERVATIONS_COUNT" -lt "$CAPACITY" ] && [ "$DISPONIBLE_FLAG" = "true" ]; then
        log_success "  ✅ Logique correcte: places disponibles → flag true"
        tests_passed=$((tests_passed + 1))
    elif [ "$RESERVATIONS_COUNT" -eq "$CAPACITY" ] && [ "$DISPONIBLE_FLAG" = "false" ]; then
        log_success "  ✅ Logique correcte: capacité atteinte → flag false"
        tests_passed=$((tests_passed + 1))
    else
        log_error "  ❌ Logique incohérente: $RESERVATIONS_COUNT/$CAPACITY mais flag=$DISPONIBLE_FLAG"
    fi
    
    # Résumé du test
    if [ $tests_passed -eq $total_tests ]; then
        log_success "  🎉 Tous les tests de cohérence passés ($tests_passed/$total_tests)"
        return 0
    else
        log_error "  💥 Échecs de cohérence ($tests_passed/$total_tests)"
        return 1
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
    echo "🔄 Test de gestion automatique du flag 'disponible'"
    echo "================================================"
    
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
    
    # État initial (doit être disponible)
    echo ""
    log_info "📋 ÉTAPE 1 : État initial (0/2 réservations)"
    if ! check_disponible_flag "INITIAL" "true"; then
        cleanup
        exit 1
    fi
    
    # Première réservation (doit rester disponible)
    echo ""
    log_info "📋 ÉTAPE 2 : Première réservation (1/2 réservations)"
    create_reservation "FLAG-01"
    sleep 1  # Petit délai pour la mise à jour
    if ! check_disponible_flag "après 1ère réservation" "true"; then
        cleanup
        exit 1
    fi
    
    # Deuxième réservation (doit passer à indisponible)
    echo ""
    log_info "📋 ÉTAPE 3 : Deuxième réservation (2/2 réservations - SATURATION)"
    create_reservation "FLAG-02"
    sleep 1  # Petit délai pour la mise à jour
    if ! check_disponible_flag "après 2ème réservation (saturé)" "false"; then
        cleanup
        exit 1
    fi
    
    # Annulation d'une réservation (doit redevenir disponible)
    echo ""
    log_info "📋 ÉTAPE 4 : Annulation d'une réservation (2→1/2 réservations)"
    FIRST_RESERVATION_ID=${RESERVATION_IDS[0]}
    log_info "Annulation de la réservation ID $FIRST_RESERVATION_ID..."
    curl -s -X PUT "${API_BASE_URL}/api/reservations/${FIRST_RESERVATION_ID}/annuler" >/dev/null
    sleep 1  # Petit délai pour la mise à jour
    if ! check_disponible_flag "après annulation (libéré)" "true"; then
        cleanup
        exit 1
    fi
    
    # Test de re-saturation
    echo ""
    log_info "📋 ÉTAPE 5 : Nouvelle réservation pour re-saturer (1→2/2 réservations)"
    create_reservation "FLAG-03"
    sleep 1  # Petit délai pour la mise à jour
    if ! check_disponible_flag "après re-saturation" "false"; then
        cleanup
        exit 1
    fi
    
    # Résultat
    echo ""
    log_success "🎉 Tous les tests de gestion du flag 'disponible' sont passés !"
    echo ""
    echo "📋 Fonctionnalités validées :"
    echo "✅ Flag 'disponible' reste 'true' tant qu'il y a de la place"
    echo "✅ Flag 'disponible' passe à 'false' à la saturation"
    echo "✅ Flag 'disponible' redevient 'true' après annulation"
    echo "✅ Cohérence parfaite entre flag et listes de disponibilités"
    echo "✅ Mise à jour automatique en temps réel"
    
    # Nettoyage final
    cleanup
    
    echo ""
    log_success "🔄 Gestion automatique du flag 'disponible' opérationnelle !"
}

# Exécution
main "$@" 