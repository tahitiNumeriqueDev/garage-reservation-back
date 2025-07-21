# 🛠️ Scripts de Génération de Créneaux

Ce dossier contient des scripts pour générer automatiquement les créneaux de disponibilité pour **juillet et août 2025**.

## 📋 Vue d'ensemble

### 🎯 **Objectif**
Créer automatiquement tous les créneaux de réservation pour la période estivale 2025 avec les règles métier du garage.

### 📅 **Règles appliquées**
- **Période** : Juillet et Août 2025
- **Jours ouvrables** : Lundi à Samedi (dimanches fermés)
- **Horaires** : 8h00 - 18h00 
- **Créneaux** : 1 heure chacun
- **Pause déjeuner** : 12h-14h (pas de créneaux)
- **Jours fériés** : 14 juillet et 15 août (fermés)
- **Capacité** : 2 véhicules par créneau
- **Statut** : Tous disponibles par défaut

### 📊 **Résultats attendus**
- **Juillet 2025** : ~216 créneaux (27 jours ouvrables × 8 créneaux/jour)
- **Août 2025** : ~200 créneaux (25 jours ouvrables × 8 créneaux/jour)
- **Total** : ~416 créneaux

## 🚀 Méthodes de génération

### 1️⃣ **Script Bash (Recommandé)**

**Fichier :** `generate-creneaux.sh`

```bash
# Rendre le script exécutable
chmod +x scripts/generate-creneaux.sh

# Générer juillet-août 2025 (nettoie les anciens créneaux puis génère)
./scripts/generate-creneaux.sh --reset

# Générer seulement juillet 2025
./scripts/generate-creneaux.sh --month 2025 7

# Générer seulement août 2025  
./scripts/generate-creneaux.sh --month 2025 8

# Nettoyer les créneaux futurs sans réservations
./scripts/generate-creneaux.sh --clean-only

# Afficher l'aide
./scripts/generate-creneaux.sh --help
```

**Avantages :**
- ✅ Validation et gestion d'erreur automatique
- ✅ Affichage couleur et logs détaillés
- ✅ Vérification de l'API avant exécution
- ✅ Support de jq pour affichage JSON

### 2️⃣ **API REST (Manuel)**

**Endpoint :** `/api/admin/creneaux/...`

```bash
# Démarrer l'application
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Générer juillet-août 2025
curl -X POST http://localhost:8080/api/admin/creneaux/generate/juillet-aout-2025

# Générer un mois spécifique
curl -X POST "http://localhost:8080/api/admin/creneaux/generate/month?year=2025&month=7"

# Nettoyer les créneaux futurs
curl -X DELETE http://localhost:8080/api/admin/creneaux/clean-future

# Reset complet (nettoie + génère)
curl -X POST http://localhost:8080/api/admin/creneaux/reset-juillet-aout-2025
```

### 3️⃣ **Script SQL (Direct en base)**

**Fichier :** `generate-creneaux-juillet-aout-2025.sql`

```bash
# PostgreSQL
psql -d garage_db -f scripts/generate-creneaux-juillet-aout-2025.sql

# H2 Console
# Copier-coller le contenu du script dans l'interface web
```

**Quand l'utiliser :**
- ✅ Application Spring Boot non disponible
- ✅ Accès direct à la base de données
- ✅ Déploiement en production avec scripts de migration

## 📖 Guide d'utilisation

### **Étape 1 : Prérequis**

```bash
# 1. Application démarrée
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 2. Vérifier l'API (optionnel)
curl http://localhost:8080/api/creneaux

# 3. Installer jq pour meilleur affichage (optionnel)
sudo apt-get install jq  # Ubuntu/Debian
brew install jq          # macOS
```

### **Étape 2 : Génération (Méthode recommandée)**

```bash
# Une seule commande pour tout faire !
./scripts/generate-creneaux.sh --reset
```

**Sortie attendue :**
```
🏗️  Générateur de créneaux - Garage Réservation
==============================================

ℹ️  Vérification de l'API...
✅ API accessible
ℹ️  🔄 Reset et génération juillet-août 2025...
✅ 🔄 Reset et génération juillet-août 2025 réussie
Remise à zéro et génération effectuées avec succès
  nombreSupprimes: 0
  nombreCrees: 416
✅ 🎉 Opération terminée avec succès !
```

### **Étape 3 : Vérification**

```bash
# Vérifier via API
curl "http://localhost:8080/api/creneaux/semaine/2025-07-01T00:00:00Z" | jq length

# Ou via interface Swagger
# http://localhost:8080/swagger-ui.html
```

## 🔧 Personnalisation

### **Modifier les horaires**

**Dans le service Java :**
```java
// CreneauGenerationService.java ligne ~95
LocalTime heureDebut = LocalTime.of(8, 0);   // Changer 8h
LocalTime heureFin = LocalTime.of(18, 0);    // Changer 18h
```

**Dans le script SQL :**
```sql
-- generate-creneaux-juillet-aout-2025.sql ligne ~54
current_time := '08:00:00';  -- Changer l'heure de début
WHILE current_time < '18:00:00' LOOP  -- Changer l'heure de fin
```

### **Modifier la capacité**

**Dans le service Java :**
```java
// CreneauGenerationService.java ligne ~107
.capaciteTotale(2) // Changer la capacité par créneau
```

**Dans le script SQL :**
```sql
-- generate-creneaux-juillet-aout-2025.sql ligne ~62
VALUES (creneau_debut, creneau_fin, true, 2);  -- Changer le 2
```

### **Ajouter des jours fériés**

**Dans le service Java :**
```java
// CreneauGenerationService.java ligne ~29
private static final Set<LocalDate> JOURS_FERIES_2025 = Set.of(
    LocalDate.of(2025, 7, 14),  // 14 juillet
    LocalDate.of(2025, 8, 15),  // 15 août
    LocalDate.of(2025, 12, 25)  // Ajouter d'autres jours
);
```

## 🆘 Dépannage

### **Erreur : API non accessible**
```
❌ Impossible d'accéder à l'API sur http://localhost:8080/api/admin
```

**Solutions :**
1. Vérifier que l'application est démarrée
2. Changer le port dans le script si différent
3. Vérifier les logs Spring Boot

### **Erreur : Créneaux déjà existants** 
```
ERROR: duplicate key value violates unique constraint
```

**Solutions :**
1. Utiliser `--reset` au lieu de `--generate-only`
2. Nettoyer manuellement : `--clean-only`
3. Vérifier la base avec les requêtes SQL de contrôle

### **Problème de fuseau horaire**
Les heures sont générées en **UTC**. Pour l'heure locale française :
- UTC+1 en hiver (novembre-mars)  
- UTC+2 en été (mars-octobre)

Les créneaux 8h-18h UTC correspondent à 9h-19h ou 10h-20h en France selon la saison.

## 📝 Logs et monitoring

### **Vérifier les créneaux générés**

```sql
-- Nombre de créneaux par mois
SELECT 
    EXTRACT(MONTH FROM heure_debut) as mois,
    COUNT(*) as nombre_creneaux
FROM creneaux 
WHERE heure_debut >= '2025-07-01' AND heure_debut < '2025-09-01'
GROUP BY EXTRACT(MONTH FROM heure_debut);

-- Créneaux par jour de la semaine
SELECT 
    CASE EXTRACT(DOW FROM heure_debut)
        WHEN 1 THEN 'Lundi'
        WHEN 2 THEN 'Mardi'
        WHEN 6 THEN 'Samedi'
        WHEN 0 THEN 'Dimanche (ne devrait pas exister)'
    END as jour_semaine,
    COUNT(*) as nombre_creneaux
FROM creneaux 
WHERE heure_debut >= '2025-07-01' AND heure_debut < '2025-09-01'
GROUP BY EXTRACT(DOW FROM heure_debut);
```

### **Surveiller les performances**

```bash
# Temps d'exécution du script
time ./scripts/generate-creneaux.sh --reset

# Taille de la base après génération  
curl -s http://localhost:8080/api/creneaux | jq length
```

## 🎯 Recommandations

### **Pour la production :**
1. ✅ Utiliser le **script SQL** dans les migrations Liquibase
2. ✅ Programmer l'exécution via **cron** ou **scheduled jobs**
3. ✅ Surveiller les **logs d'application**
4. ✅ Sauvegarder avant génération massive

### **Pour le développement :**
1. ✅ Utiliser le **script bash** pour des tests rapides
2. ✅ Utiliser l'**API REST** via Swagger UI pour tester
3. ✅ Profil `dev` avec base H2 pour éviter impacts

### **Maintenance :**
1. 🔄 Régénérer chaque trimestre/semestre
2. 📊 Surveiller l'occupation des créneaux
3. 🧹 Nettoyer périodiquement les créneaux expirés
4. ⚙️ Ajuster capacité selon la demande

## 🏆 Avantages de cette approche

- ⚡ **Performance** : Génération en lot optimisée
- 🔄 **Flexibilité** : Multiple méthodes d'exécution
- 🛡️ **Robustesse** : Gestion d'erreur et validation
- 📊 **Traçabilité** : Logs détaillés et vérification
- 🎛️ **Contrôle** : Nettoyage et regénération facile
- 🔧 **Maintenance** : Configuration centralisée et modifiable 