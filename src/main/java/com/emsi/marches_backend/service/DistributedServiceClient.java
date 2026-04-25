package com.emsi.marches_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

/**
 * Service Client pour appels RPC distribués (RMI-like Pattern)
 * Implémente la communication entre services distribués
 * dans une architecture microservices
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedServiceClient {

    private final RestTemplate restTemplate;

    public <T> T callRemoteService(String serviceUrl, Object requestBody, Class<T> responseType) {
        try {
            log.info("Appel service distant: {}", serviceUrl);

            // Préparation des headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

            // Appel synchrone au service distant
            ResponseEntity<T> response = restTemplate.postForEntity(serviceUrl, requestEntity, responseType);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Réponse service distant reçue avec succès");
                return response.getBody();
            } else {
                log.error("Erreur service distant: {}", response.getStatusCode());
                throw new RuntimeException("Service distant retourné erreur: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Erreur communication service distant: {}", e.getMessage());
            throw new RuntimeException("Communication avec service distant échouée", e);
        }
    }

    /**
     * Effectue un appel GET à un service distant
     */
    public <T> T getFromRemoteService(String serviceUrl, Class<T> responseType) {
        try {
            log.info("GET service distant: {}", serviceUrl);

            ResponseEntity<T> response = restTemplate.getForEntity(serviceUrl, responseType);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Données reçues du service distant");
                return response.getBody();
            } else {
                throw new RuntimeException("Service distant erreur: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Erreur appel GET service distant: {}", e.getMessage());
            throw new RuntimeException("Appel GET échoué", e);
        }
    }


    public <T> T callWithRetry(String serviceUrl, Object requestBody, Class<T> responseType, int maxRetries) {
        int attempt = 0;
        RestClientException lastException = null;

        while (attempt < maxRetries) {
            try {
                log.info("Tentative {} vers service distant: {}", attempt + 1, serviceUrl);
                return callRemoteService(serviceUrl, requestBody, responseType);
            } catch (RestClientException e) {
                lastException = e;
                attempt++;
                if (attempt < maxRetries) {
                    try {

                        long delayMs = (long) Math.pow(2, attempt - 1) * 1000;
                        log.warn("Erreur tentative {}, retry dans {}ms", attempt, delayMs);
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Tous les retry échoués après {} tentatives", maxRetries);
        throw new RuntimeException("Service distant inaccessible après " + maxRetries + " tentatives", lastException);
    }
}
