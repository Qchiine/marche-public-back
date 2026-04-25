package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.ScraperLogDocument;
import com.emsi.marches_backend.repository.OffreMarcheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingService {

    private final RestTemplate restTemplate;
    private final ScraperLogService scraperLogService;
    private final NotificationService notificationService;
    private final OffreMarcheRepository offreMarcheRepository;
    private static final String SCRAPING_API_URL = "http://localhost:8000/scrape";

    /**
     * Appelle l'API de scraping pour récupérer les offres
     */
    public Map<String, Object> scrapeOffres() {
        LocalDateTime dateDebut = LocalDateTime.now();
        long startTime = System.currentTimeMillis();

        try {
            log.info("Appel API scraping: {}", SCRAPING_API_URL);

            // Préparation des headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            // Appel POST à l'API de scraping
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    SCRAPING_API_URL,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                long duree = System.currentTimeMillis() - startTime;
                int nbOffres = responseBody != null ? responseBody.size() : 0;

                // Créer les notifications pour les nouvelles offres
                if (responseBody != null && !responseBody.isEmpty()) {
                    try {
                        triggerNotificationsForNewOffers(responseBody);
                    } catch (Exception e) {
                        log.error("Erreur lors de la création des notifications: {}", e.getMessage());
                    }
                }

                // Log succès
                ScraperLogDocument logDoc = ScraperLogDocument.builder()
                        .dateDebut(dateDebut)
                        .dateFin(LocalDateTime.now())
                        .statut("SUCCÈS")
                        .nbOffres(nbOffres)
                        .message("Scraping réussi, notifications créées")
                        .duree(duree)
                        .build();
                scraperLogService.save(logDoc);

                log.info("Scraping réussi, offres récupérées");
                return responseBody;
            } else {
                long duree = System.currentTimeMillis() - startTime;
                String erreur = "Erreur HTTP: " + response.getStatusCode();

                // Log erreur
                ScraperLogDocument scraperLog = ScraperLogDocument.builder()
                        .dateDebut(dateDebut)
                        .dateFin(LocalDateTime.now())
                        .statut("ERREUR")
                        .message("Erreur lors du scraping")
                        .erreur(erreur)
                        .duree(duree)
                        .build();
                scraperLogService.save(scraperLog);

                throw new RuntimeException("Erreur lors du scraping: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            long duree = System.currentTimeMillis() - startTime;

            // Log erreur
            ScraperLogDocument logDoc = ScraperLogDocument.builder()
                    .dateDebut(dateDebut)
                    .dateFin(LocalDateTime.now())
                    .statut("ERREUR")
                    .message("Erreur communication API scraping")
                    .erreur(e.getMessage())
                    .duree(duree)
                    .build();
            scraperLogService.save(logDoc);

            log.error("Erreur communication API scraping: {}", e.getMessage());
            throw new RuntimeException("Échec de l'appel API scraping", e);
        }
    }

    /**
     * Appelle l'API de scraping avec retry
     */
    public Map<String, Object> scrapeOffresWithRetry(int maxRetries) {
        int attempt = 0;
        RuntimeException lastException = null;

        while (attempt < maxRetries) {
            try {
                log.info("Tentative {} de scraping", attempt + 1);
                return scrapeOffres();
            } catch (RuntimeException e) {
                lastException = e;
                attempt++;
                if (attempt < maxRetries) {
                    try {
                        long delayMs = (long) Math.pow(2, attempt - 1) * 1000;
                        log.warn("Tentative {} échouée, retry dans ms", attempt);
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Scraping échoué après tentatives");
        throw new RuntimeException("Scraping impossible après " + maxRetries + " tentatives", lastException);
    }

    /**
     * Déclenche les notifications pour les nouvelles offres scrapées
     * Note: Cette méthode suppose une structure spécifique des données
     */
    private void triggerNotificationsForNewOffers(Map<String, Object> offresData) {
        log.info("Déclenchement des notifications pour les nouvelles offres");

        // TODO: Mapper les données de l'API vers OffreMarcheDocument
        // et créer les notifications pour chaque offre
        // Exemple:
        // for (OffreMarcheDocument offre : convertedOffres) {
        //     notificationService.createNotificationsForOffer(offre);
        // }
    }
}
