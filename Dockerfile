# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-21 AS builder

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances (mis en cache si pom.xml n'a pas changé)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Compiler l'application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring

# Installer les dépendances système nécessaires
RUN apk add --no-cache curl

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR depuis le stage de build
COPY --from=builder /app/target/garage-reservation-1.0.0.jar app.jar

# Changer le propriétaire du fichier
RUN chown spring:spring app.jar

# Utiliser l'utilisateur non-root
USER spring

# Exposer le port (Railway utilise la variable PORT)
EXPOSE 8080

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8080

# Commande de démarrage
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"] 