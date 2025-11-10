package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${product.service.api-key:}")
    private String apiKey;

    @Value("${product.service.timeout-seconds:10}")
    private int timeoutSeconds;

    @Bean
    public WebClient.Builder webClientBuilder() {
        WebClient.Builder builder = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json");

        // Agregar header de autenticación con API Key si está configurado
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            builder.defaultHeader("X-API-Key", apiKey);
            // Alternativa: usar Authorization header
            // builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }

        return builder;
    }
}

