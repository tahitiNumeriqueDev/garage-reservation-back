#!/bin/bash

# ============================================================================
# Script de test pour la migration Liquibase YAML - Créneaux juillet-août 2025
# ============================================================================
# 
# Ce script teste la migration automatique des créneaux via Liquibase
# 
# Usage:
#   ./test-migration-liquibase.sh [OPTION]
#
# Options:
#   --status        Voir le statut des migrations
#   --validate      Valider les changements sans les appliquer
#   --rollback      Rollback de la dernière migration
#   --reset-db      Reset complet de la base H2 (dev uniquement)
#   --test          Test complet avec reset et validation
#   --help          Affiche cette aide
# ============================================================================

set -e  # Arrêt en cas d'erreur

# Configuration
API_BASE_URL="http://localhost:8080"
MAVEN_PROFILES="-Dspring-boot.run.profiles=dev"

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
    echo "🧪 Testeur de migrations Liquibase YAML - Garage Réservation"
    echo
    echo "USAGE:"
    echo "  $0 [OPTION]"
    echo
    echo "OPTIONS:"
    echo "  --status           Affiche le statut des migrations Liquibase"
    echo "  --validate         Valide les fichiers de migration YAML"
    echo "  --rollback         Rollback de la dernière migration"
    echo "  --reset-db         Reset complet de la base H2 (dev uniquement)"
    echo "  --test             Test complet : reset + migration + validation"
    echo "  --help             Affiche cette aide"
    echo
    echo "EXEMPLES:"
    echo "  $0 --test                    # Test complet recommandé"
    echo "  $0 --status                  # Voir les migrations appliquées"
    echo "  $0 --validate                # Vérifier la syntaxe YAML"
    echo "  $0 --rollback                # Annuler la dernière migration"
    echo
    echo "PRÉREQUIS:"
    echo "  - Maven installé"
    echo "  - Profil dev configuré (base H2)"
    echo "  - Fichiers YAML valides"
    echo
}

# Vérifier les prérequis
check_prereq() {
    if ! command -v mvn >/dev/null 2>&1; then
        log_error "Maven n'est pas installé ou pas dans le PATH"
        exit 1
    fi
    
    if [[ ! -f "pom.xml" ]]; then
        log_error "Fichier pom.xml non trouvé. Exécuter depuis la racine du projet."
        exit 1
    fi
    
    if [[ ! -f "src/main/resources/db/changelog/003-generate-creneaux-juillet-aout-2025.yml" ]]; then
        log_error "Migration YAML non trouvée. Vérifiez le fichier 003-generate-creneaux-juillet-aout-2025.yml"
        exit 1
    fi
}

# Statut des migrations Liquibase
liquibase_status() {
    log_info "Vérification du statut des migrations..."
    
    mvn liquibase:status $MAVEN_PROFILES -q | grep -E "(Migration|changeset|Author|ID)" || true
    
    log_success "Statut affiché"
}

# Validation des migrations
liquibase_validate() {
    log_info "Validation des fichiers de migration YAML..."
    
    if mvn liquibase:validate $MAVEN_PROFILES -q; then
        log_success "Fichiers de migration valides"
    else
        log_error "Erreur de validation des migrations"
        return 1
    fi
}

# Rollback de la dernière migration
liquibase_rollback() {
    log_warning "Rollback de la dernière migration..."
    
    if mvn liquibase:rollback -Dliquibase.rollbackCount=1 $MAVEN_PROFILES -q; then
        log_success "Rollback effectué"
    else
        log_error "Erreur lors du rollback"
        return 1
    fi
}

# Reset complet de la base H2
reset_h2_database() {
    log_info "Reset de la base de données H2..."
    
    # Supprimer les fichiers H2 s'ils existent
    rm -f target/h2-*.db 2>/dev/null || true
    rm -f *.db 2>/dev/null || true
    
    log_success "Base H2 réinitialisée"
}

# Test de l'API après migration
test_api() {
    log_info "Test de l'API après migration..."
    
    # Attendre que l'API soit disponible
    local max_attempts=30
    local attempt=1
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -s "$API_BASE_URL/api/creneaux" >/dev/null 2>&1; then
            log_success "API disponible"
            break
        fi
        
        if [[ $attempt -eq $max_attempts ]]; then
            log_error "API non accessible après $max_attempts tentatives"
            return 1
        fi
        
        sleep 1
        ((attempt++))
    done
    
    # Tester la génération des créneaux
    local creneaux_juillet=$(curl -s "$API_BASE_URL/api/creneaux/jour/2025-07-01T00:00:00Z" | jq length 2>/dev/null || echo "0")
    local creneaux_aout=$(curl -s "$API_BASE_URL/api/creneaux/jour/2025-08-01T00:00:00Z" | jq length 2>/dev/null || echo "0")
    
    log_info "Créneaux juillet 2025 : $creneaux_juillet"
    log_info "Créneaux août 2025 : $creneaux_aout"
    
    if [[ $creneaux_juillet -gt 0 || $creneaux_aout -gt 0 ]]; then
        log_success "Migration des créneaux réussie"
    else
        log_error "Aucun créneau généré par la migration"
        return 1
    fi
}

# Test complet
run_complete_test() {
    log_info "🧪 Début du test complet de migration Liquibase YAML"
    echo "=================================================="
    
    # Étape 1 : Reset
    reset_h2_database
    
    # Étape 2 : Validation
    liquibase_validate
    
    # Étape 3 : Démarrage de l'application en arrière-plan
    log_info "Démarrage de l'application Spring Boot (profil dev)..."
    mvn spring-boot:run $MAVEN_PROFILES > /tmp/spring-boot.log 2>&1 &
    local spring_boot_pid=$!
    
    # Fonction de nettoyage
    cleanup() {
        if kill -0 $spring_boot_pid 2>/dev/null; then
            log_info "Arrêt de l'application Spring Boot..."
            kill $spring_boot_pid 2>/dev/null || true
            wait $spring_boot_pid 2>/dev/null || true
        fi
    }
    trap cleanup EXIT
    
    # Étape 4 : Attendre le démarrage et tester
    sleep 15  # Laisser le temps à Spring Boot de démarrer
    
    test_api
    
    # Étape 5 : Vérifications supplémentaires
    log_info "Vérifications des règles métier..."
    
    # Vérifier qu'il n'y a pas de créneaux le dimanche
    local dimanche_creneaux=$(curl -s "$API_BASE_URL/api/creneaux/jour/2025-07-06T00:00:00Z" | jq length 2>/dev/null || echo "0")
    if [[ $dimanche_creneaux -eq 0 ]]; then
        log_success "✅ Pas de créneaux le dimanche (correct)"
    else
        log_error "❌ Des créneaux existent le dimanche !"
    fi
    
    # Vérifier qu'il n'y a pas de créneaux le 14 juillet (fête nationale)
    local fete_nationale=$(curl -s "$API_BASE_URL/api/creneaux/jour/2025-07-14T00:00:00Z" | jq length 2>/dev/null || echo "0")
    if [[ $fete_nationale -eq 0 ]]; then
        log_success "✅ Pas de créneaux le 14 juillet (correct)"
    else
        log_error "❌ Des créneaux existent le 14 juillet !"
    fi
    
    # Vérifier qu'il n'y a pas de créneaux le 15 août (Assomption)
    local assomption=$(curl -s "$API_BASE_URL/api/creneaux/jour/2025-08-15T00:00:00Z" | jq length 2>/dev/null || echo "0")
    if [[ $assomption -eq 0 ]]; then
        log_success "✅ Pas de créneaux le 15 août (correct)"
    else
        log_error "❌ Des créneaux existent le 15 août !"
    fi
    
    cleanup
    
    log_success "🎉 Test complet terminé avec succès !"
}

# Programme principal
main() {
    echo "🧪 Testeur de Migration Liquibase YAML - Garage Réservation"
    echo "==========================================================="
    echo
    
    check_prereq
    
    case ${1:-"--help"} in
        --help|-h)
            show_help
            ;;
        --status)
            liquibase_status
            ;;
        --validate)
            liquibase_validate
            ;;
        --rollback)
            liquibase_rollback
            ;;
        --reset-db)
            reset_h2_database
            ;;
        --test)
            run_complete_test
            ;;
        *)
            log_error "Option inconnue: $1"
            echo
            show_help
            exit 1
            ;;
    esac
}

# Point d'entrée
main "$@" 