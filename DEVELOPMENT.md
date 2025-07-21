# Guide de Développement - Garage Reservation API

## 🚀 Démarrage Rapide

### Option 1 : Développement avec H2 (Recommandé)
```bash
# Lancer avec le profil dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Avantages :**
- ✅ Pas de PostgreSQL requis
- ✅ Démarrage en moins de 30 secondes
- ✅ Données de test automatiquement créées
- ✅ Console H2 intégrée

**Accès :**
- API : http://localhost:8080/api/creneaux
- Swagger : http://localhost:8080/swagger-ui.html
- Console H2 : http://localhost:8080/h2-console

### Option 2 : Développement avec PostgreSQL
```bash
# Démarrer PostgreSQL
docker run -d -p 5432:5432 -e POSTGRES_DB=garage_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password postgres

# Démarrer l'application
mvn spring-boot:run
```

### Option 3 : Développement avec Docker
```bash
# Tout-en-un avec Docker Compose
docker-compose up -d

# Voir les logs
docker-compose logs -f app
```

## 🔧 Configuration des Profils

### Profil `dev` (H2)
- **Fichier** : `application-dev.properties`
- **Base** : H2 en mémoire
- **Migrations** : Liquibase activé
- **Console** : H2 Console activée

### Profil `default` (PostgreSQL)
- **Fichier** : `application.properties`
- **Base** : PostgreSQL (locale ou Railway)
- **Migrations** : Liquibase activé
- **Variables** : Configurables via env

## 📊 Outils de Développement

### Console H2 (Profil dev uniquement)
- **URL** : http://localhost:8080/h2-console
- **JDBC URL** : `jdbc:h2:mem:garage_db`
- **Utilisateur** : `sa`
- **Mot de passe** : (vide)

### API Documentation
- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **JSON OpenAPI** : http://localhost:8080/api-docs
- **YAML OpenAPI** : http://localhost:8080/api-docs.yaml

### Endpoints Principaux
- **Créneaux** : `GET /api/creneaux`
- **Créneaux du jour** : `GET /api/creneaux/jour/2024-12-23`
- **Créneaux de la semaine** : `GET /api/creneaux/semaine/2024-12-23`
- **Créer réservation** : `POST /api/reservations`
- **Lister réservations** : `GET /api/reservations`

## 🧪 Tests Rapides

### Tester les créneaux
```bash
curl http://localhost:8080/api/creneaux
```

### Tester une réservation
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "immatriculation": "TEST-123",
    "kilometrage": 50000,
    "typeVehicule": "AUTO",
    "poidsLourd": false,
    "numeroTelephone": "0123456789",
    "email": "test@example.com",
    "creneauId": 1
  }'
```

## 🔄 Rechargement des Données

### Avec H2 (redémarrage)
```bash
# Arrêter l'application (Ctrl+C)
# Redémarrer
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Avec PostgreSQL (reset base)
```bash
# Supprimer et recréer la base
docker exec -it postgres-garage psql -U postgres -c "DROP DATABASE IF EXISTS garage_db; CREATE DATABASE garage_db;"

# Redémarrer l'application
mvn spring-boot:run
```

## 📝 Développement

### Ajouter une nouvelle migration
1. Créer un nouveau fichier dans `src/main/resources/db/changelog/`
2. Ajouter la référence dans `db.changelog-master.xml`
3. Redémarrer l'application

### Modifier la configuration
- **H2** : Modifier `application-dev.properties`
- **PostgreSQL** : Modifier `application.properties`

### Logs utiles
```bash
# Voir les requêtes SQL
logging.level.org.hibernate.SQL=DEBUG

# Voir les paramètres SQL
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
``` 