package com.insureflow.claimintakeservice.config;

import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    // ── Niveau de log des appels Feign
    // NONE    → aucun log
    // BASIC   → méthode + URL + statut
    // HEADERS → + headers
    // FULL    → + body complet
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    // ── Timeouts des appels Feign
    // connectTimeout → temps max pour établir connexion
    // readTimeout    → temps max pour recevoir réponse
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5000,  TimeUnit.MILLISECONDS,   // connectTimeout
                10000, TimeUnit.MILLISECONDS,   // readTimeout
                true                            // followRedirects
        );
    }

    // ── Décodeur d'erreurs personnalisé
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            switch (response.status()) {
                case 404:
                    return new RuntimeException(
                            "Ressource non trouvée : " + methodKey);
                case 500:
                    return new RuntimeException(
                            "Erreur serveur distant : " + methodKey);
                default:
                    return new RuntimeException(
                            "Erreur Feign [" + response.status() +
                                    "] : " + methodKey);
            }
        };
    }
}