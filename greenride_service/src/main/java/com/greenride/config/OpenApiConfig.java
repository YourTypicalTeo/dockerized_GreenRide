package com.greenride.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GreenRide API Service")
                        .version("1.0")
                        .description("API documentation for the Distributed Systems Semester Project. " +
                                "Handles Rides, Bookings, and Authentication.")
                        .contact(new Contact()
                                .name("GreenRide Team")
                                .email("support@greenride.com")))
                //Βάζουμε (JWT) Security στο to Swagger UI
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Bearer Authentication")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    //Βάζουμε ολα τα REST API endpoints σε ενα συγκεκριμένα API category στο Swagger UI
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("greenride-api")
                .packagesToScan("com.greenride.controller.api") //Σκανάρουμε μόνο το API package
                .pathsToMatch("/api/**") //Κανε match μόνο τα api paths
                .build();
    }
}