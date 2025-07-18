# API de Réservation de Garage

Cette API Java Spring Boot permet de gérer les réservations de créneaux pour un garage automobile.

## Fonctionnalités

- **Gestion des créneaux** : Lister les disponibilités par jour et par semaine
- **Gestion des réservations** : Créer, modifier et supprimer des réservations
- **Validation des données** : Validation automatique des informations de réservation
- **Documentation OpenAPI/Swagger** : Documentation interactive de l'API
- **Base de données PostgreSQL** : Base de données de production

## Technologies

- **Java 21** (dernière version LTS)
- **Spring Boot 3.3.0**
- **Spring Data JPA**
- **PostgreSQL** (base de données de production)
- **Liquibase** (migrations de base de données)
- **SpringDoc OpenAPI** (documentation Swagger)
- **Maven** pour la gestion des dépendances

## Installation et lancement

### Prérequis
- Java 21 ou supérieur
- Maven 3.6 ou supérieur
- PostgreSQL (pour le développement local)

### Démarrage local
```bash
# Compiler le projet
mvn clean compile

# Lancer l'application
mvn spring-boot:run
```

L'API sera disponible sur `http://localhost:8080`

### Documentation API (Swagger)

Une fois l'application démarrée, la documentation interactive Swagger est accessible à :
- **Interface Swagger UI** : `http://localhost:8080/swagger-ui.html`
- **JSON OpenAPI** : `http://localhost:8080/api-docs`
- **YAML OpenAPI** : `http://localhost:8080/api-docs.yaml`

### Configuration base de données

#### Variables d'environnement
L'application utilise les variables d'environnement suivantes :
- `DATABASE_URL` : URL complète de la base de données PostgreSQL
- `DATABASE_USER` : Nom d'utilisateur de la base de données
- `DATABASE_PASSWORD` : Mot de passe de la base de données

#### Développement local
Pour le développement local, vous pouvez utiliser une base PostgreSQL locale :
```bash
# Créer une base de données locale
createdb garage_db

# Ou utiliser Docker
docker run --name postgres-garage -e POSTGRES_DB=garage_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres
```

### Déploiement sur Railway

#### 1. Prérequis Railway
- Compte Railway créé
- Base de données PostgreSQL provisionnée sur Railway

#### 2. Configuration
Railway injecte automatiquement les variables d'environnement de la base de données :
- `DATABASE_URL` (ou `POSTGRES_URL`)
- `DATABASE_USER` (ou `POSTGRES_USER`)
- `DATABASE_PASSWORD` (ou `POSTGRES_PASSWORD`)

#### 3. Déploiement
```bash
# Connecter le repository à Railway
railway login
railway link

# Déployer
railway deploy
```

### Migrations Liquibase
Les migrations de base de données sont gérées automatiquement par Liquibase au démarrage de l'application.

Les fichiers de migration se trouvent dans :
- `src/main/resources/db/changelog/`

## API Endpoints

### Documentation

#### Documentation OpenAPI/Swagger
```http
GET /swagger-ui.html    # Interface Swagger UI
GET /api-docs           # JSON OpenAPI
GET /api-docs.yaml      # YAML OpenAPI
```

### Créneaux

#### Lister tous les créneaux
```http
GET /api/creneaux
```

#### Lister les créneaux d'un jour
```http
GET /api/creneaux/jour/2024-12-23
GET /api/creneaux/jour/2024-12-23?disponiblesOnly=true
```

#### Lister les créneaux d'une semaine
```http
GET /api/creneaux/semaine/2024-12-23
GET /api/creneaux/semaine/2024-12-23?disponiblesOnly=true
```

#### Lister les créneaux entre deux dates
```http
GET /api/creneaux/periode?dateDebut=2024-12-23T08:00:00&dateFin=2024-12-23T18:00:00
```

#### Créer un nouveau créneau
```http
POST /api/creneaux?heureDebut=2024-12-25T08:00:00&heureFin=2024-12-25T09:00:00&capacite=2
```

#### Générer des créneaux pour une journée
```http
POST /api/creneaux/generer-jour?date=2024-12-25&heureDebut=08:00&heureFin=18:00&dureeMinutes=60
```

### Réservations

#### Lister toutes les réservations
```http
GET /api/reservations
```

#### Lister les réservations d'un jour
```http
GET /api/reservations/jour/2024-12-23
```

#### Lister les réservations d'une semaine
```http
GET /api/reservations/semaine/2024-12-23
```

#### Rechercher par immatriculation
```http
GET /api/reservations/immatriculation/AB-123-CD
```

#### Rechercher par numéro de téléphone
```http
GET /api/reservations/telephone/0123456789
```

#### Créer une nouvelle réservation
```http
POST /api/reservations
Content-Type: application/json

{
  "immatriculation": "AB-123-CD",
  "kilometrage": 75000,
  "typeVehicule": "AUTO",
  "poidsLourd": false,
  "numeroTelephone": "0123456789",
  "email": "test@example.com",
  "creneauId": 1
}
```

#### Confirmer une réservation
```http
PUT /api/reservations/1/confirmer
```

#### Annuler une réservation
```http
PUT /api/reservations/1/annuler
```

#### Terminer une réservation
```http
PUT /api/reservations/1/terminer
```

## Modèle de données

### Informations de réservation
- **Immatriculation** : Obligatoire
- **Kilométrage** : Obligatoire, nombre positif
- **Type de véhicule** : AUTO ou MOTO
- **Poids lourd** : Booléen, optionnel (défaut: false)
- **Numéro de téléphone** : Obligatoire, format validé
- **Email** : Optionnel, format validé si fourni

### Statuts de réservation
- `RESERVEE` : Réservation initiale
- `CONFIRMEE` : Réservation confirmée
- `ANNULEE` : Réservation annulée
- `TERMINEE` : Réservation terminée

## Exemples d'utilisation

### Créer une réservation avec curl
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "immatriculation": "XY-789-AB",
    "kilometrage": 50000,
    "typeVehicule": "AUTO",
    "poidsLourd": false,
    "numeroTelephone": "0123456789",
    "email": "client@example.com",
    "creneauId": 3
  }'
```

### Lister les créneaux disponibles d'aujourd'hui
```bash
curl "http://localhost:8080/api/creneaux/jour/$(date +%Y-%m-%d)?disponiblesOnly=true"
```

## Données de test

L'application est livrée avec des données de test automatiquement insérées via Liquibase :
- Créneaux du 23 décembre 2024, 24 décembre 2024, et 30 décembre 2024
- Quelques réservations d'exemple
- Créneaux de 8h à 18h avec pause déjeuner (12h-14h)

Les données de test sont définies dans le fichier `src/main/resources/db/changelog/002-insert-sample-data.xml`.

## Développement

### Structure du projet
```
src/main/java/com/garage/reservation/
├── model/          # Entités JPA
├── dto/            # Objets de transfert de données
├── repository/     # Repositories JPA
├── service/        # Services métier
├── controller/     # Contrôleurs REST
├── exception/      # Gestionnaire d'erreurs
└── GarageReservationApplication.java

src/main/resources/
├── db/changelog/   # Migrations Liquibase
│   ├── db.changelog-master.xml
│   ├── 001-create-tables.xml
│   └── 002-insert-sample-data.xml
└── application.properties
```

### Tests
```bash
mvn test
```

### Packaging
```bash
mvn package
```

Le JAR exécutable sera généré dans `target/garage-reservation-1.0.0.jar`

### Commandes Liquibase utiles

```bash
# Voir le statut des migrations
mvn liquibase:status

# Voir les changements en attente
mvn liquibase:update-sql

# Appliquer les migrations manuellement
mvn liquibase:update

# Rollback de la dernière migration
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Générer un changelog à partir d'une base existante
mvn liquibase:generateChangeLog
```

### Variables d'environnement Railway

Lors du déploiement sur Railway, les variables suivantes sont automatiquement injectées :
- `DATABASE_URL` : URL complète de la base PostgreSQL
- `DATABASE_USER` : Nom d'utilisateur
- `DATABASE_PASSWORD` : Mot de passe
- `PORT` : Port d'écoute (Railway l'assigne automatiquement) # garage-reservation-back
