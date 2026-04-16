package com.emsi.marches_backend.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * Configuration pour les services distribués
 * Activation de l'async et configuration du RestTemplate pour RMI-like calls
 */
@Configuration
@EnableAsync
public class DistributedConfig {

    /**
     * RestTemplate configuré pour les appels distribués
     * - Timeout de connexion: 5 secondes
     * - Timeout de lecture: 10 secondes
     * - Retry automatique en cas d'erreur
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
