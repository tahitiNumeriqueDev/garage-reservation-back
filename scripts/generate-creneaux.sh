#!/bin/bash

# ============================================================================
# Script de génération automatique des créneaux juillet-août 2025
# ============================================================================
# 
# Ce script utilise l'API REST pour générer les créneaux automatiquement
# 
# Usage:
#   ./generate-creneaux.sh [OPTION]
#
# Options:
#   --clean-only        Nettoie seulement les créneaux futurs
#   --generate-only     Génère seulement les créneaux (sans nettoyage)
#   --reset            Nettoie puis génère (défaut)
#   --month YYYY MM    Génère pour un mois spécifique
#   --help             Affiche cette aide
#
# Exemples:
#   ./generate-creneaux.sh --reset
#   ./generate-creneaux.sh --month 2025 7
#   ./generate-creneaux.sh --month 2025 8
# ============================================================================

set -e  # Arrêt en cas d'erreur

# Configuration
API_BASE_URL="http://localhost:8080/api/admin"
CURL_OPTS="-s -w %{http_code}"

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

# Fonction d'aide
show_help() {
    echo "🔧 Générateur de créneaux pour garage-reservation"
    echo
    echo "USAGE:"
    echo "  $0 [OPTION]"
    echo
    echo "OPTIONS:"
    echo "  --clean-only         Supprime les créneaux futurs sans réservations"
    echo "  --generate-only      Génère les créneaux juillet-août 2025 uniquement"  
    echo "  --reset              Nettoie puis génère (défaut)"
    echo "  --month YYYY MM      Génère pour un mois spécifique (ex: --month 2025 7)"
    echo "  --help               Affiche cette aide"
    echo
    echo "EXEMPLES:"
    echo "  $0 --reset                    # Remet à zéro et génère juillet-août 2025"
    echo "  $0 --month 2025 7             # Génère juillet 2025 uniquement"
    echo "  $0 --month 2025 8             # Génère août 2025 uniquement"
    echo "  $0 --clean-only               # Nettoie les créneaux futurs"
    echo "  $0 --generate-only            # Génère sans nettoyer"
    echo
    echo "PRÉREQUIS:"
    echo "  - Application Spring Boot démarrée sur localhost:8080"
    echo "  - Profil dev ou production configuré"
    echo "  - curl installé"
    echo
}

# Vérifie si l'API est accessible
check_api() {
    log_info "Vérification de l'API..."
    
    if ! curl -s "$API_BASE_URL/../creneaux" >/dev/null 2>&1; then
        log_error "Impossible d'accéder à l'API sur $API_BASE_URL"
        log_error "Vérifiez que l'application Spring Boot est démarrée"
        log_info "Démarrage : mvn spring-boot:run -Dspring-boot.run.profiles=dev"
        exit 1
    fi
    
    log_success "API accessible"
}

# Appel API avec gestion d'erreur
call_api() {
    local method=$1
    local endpoint=$2
    local description=$3
    
    log_info "$description..."
    
    local response=$(curl $CURL_OPTS -X $method "$API_BASE_URL$endpoint")
    local http_code=${response: -3}
    local body=${response%???}
    
    if [[ $http_code -eq 200 ]]; then
        log_success "$description réussie"
        echo "$body" | jq -r '.message // "Opération réussie"' 2>/dev/null || echo "$body"
        if echo "$body" | jq -e '.nombreCreneaux // .nombreSupprimes // .nombreCrees' >/dev/null 2>&1; then
            echo "$body" | jq -r 'to_entries[] | select(.key | test("nombre")) | "  \(.key): \(.value)"' 2>/dev/null
        fi
        echo
        return 0
    else
        log_error "$description échouée (HTTP $http_code)"
        if command -v jq >/dev/null 2>&1 && echo "$body" | jq empty 2>/dev/null; then
            echo "$body" | jq -r '.message // .error // "Erreur inconnue"'
        else
            echo "$body"
        fi
        echo
        return 1
    fi
}

# Nettoyage des créneaux futurs
clean_creneaux() {
    call_api "DELETE" "/creneaux/clean-future" "🧹 Suppression des créneaux futurs"
}

# Génération juillet-août 2025
generate_juillet_aout() {
    call_api "POST" "/creneaux/generate/juillet-aout-2025" "🚀 Génération des créneaux juillet-août 2025"
}

# Génération pour un mois spécifique
generate_month() {
    local year=$1
    local month=$2
    
    call_api "POST" "/creneaux/generate/month?year=$year&month=$month" "🚀 Génération des créneaux pour $month/$year"
}

# Reset complet (nettoie + génère)
reset_juillet_aout() {
    call_api "POST" "/creneaux/reset-juillet-aout-2025" "🔄 Reset et génération juillet-août 2025"
}

# Programme principal
main() {
    echo "🏗️  Générateur de créneaux - Garage Réservation"
    echo "=============================================="
    echo
    
    # Vérifier la disponibilité de jq (optionnel mais recommandé)
    if ! command -v jq >/dev/null 2>&1; then
        log_warning "jq n'est pas installé. L'affichage sera moins lisible."
        log_info "Installation : sudo apt-get install jq (Ubuntu/Debian)"
    fi
    
    check_api
    
    case ${1:-"--reset"} in
        --help|-h)
            show_help
            ;;
        --clean-only)
            clean_creneaux
            ;;
        --generate-only)
            generate_juillet_aout
            ;;
        --reset)
            reset_juillet_aout
            ;;
        --month)
            if [[ $# -ne 3 ]]; then
                log_error "Usage: $0 --month YYYY MM"
                log_error "Exemple: $0 --month 2025 7"
                exit 1
            fi
            
            year=$2
            month=$3
            
            if [[ ! $year =~ ^[0-9]{4}$ ]] || [[ $year -lt 2024 ]] || [[ $year -gt 2030 ]]; then
                log_error "Année invalide: $year (doit être entre 2024 et 2030)"
                exit 1
            fi
            
            if [[ ! $month =~ ^[0-9]+$ ]] || [[ $month -lt 1 ]] || [[ $month -gt 12 ]]; then
                log_error "Mois invalide: $month (doit être entre 1 et 12)"
                exit 1
            fi
            
            generate_month $year $month
            ;;
        *)
            log_error "Option inconnue: $1"
            echo
            show_help
            exit 1
            ;;
    esac
    
    log_success "🎉 Opération terminée avec succès !"
    log_info "Vérifiez les créneaux via : curl http://localhost:8080/api/creneaux"
    log_info "Ou dans Swagger UI : http://localhost:8080/swagger-ui.html"
}

# Point d'entrée
main "$@" 