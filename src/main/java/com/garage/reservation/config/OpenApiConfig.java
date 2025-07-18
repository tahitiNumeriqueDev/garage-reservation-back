package com.garage.reservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI garageReservationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Réservation de Garage")
                        .description("API REST pour la gestion des réservations de créneaux dans un garage automobile. " +
                                "Permet de lister les disponibilités par jour et par semaine, " +
                                "et de créer des réservations avec les informations du véhicule.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Garage API")
                                .email("contact@garage-api.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Serveur de développement"),
                        new Server()
                                .url("https://your-app.railway.app")
                                .description("Serveur de production (Railway)")
                ));
    }
} 