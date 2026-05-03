package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.ScraperLogDocument;
import com.emsi.marches_backend.repository.OffreRepository;
import com.emsi.marches_backend.dto.offre.OffreFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingService {

    private final ScraperLogService scraperLogService;
    private final NotificationService notificationService;
    private final OffreRepository offreRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.scraping.api-url:http://localhost:8000/scrape}")
    private String scrapingApiUrl;

    public Map<String, Object> scrapeOffres() {
        return executeScrape("""
                {"max_pages":1,"headless":true,"timeout_ms":30000}
                """);
    }

    public Map<String, Object> scrapeOffresForSearch(OffreFilter filter) {
        String requestBody = buildSearchRequestBody(filter);
        return executeScrape(requestBody);
    }

    private Map<String, Object> executeScrape(String requestBody) {
        LocalDateTime dateDebut = LocalDateTime.now();
        long startTime = System.currentTimeMillis();

        try {
            log.info("Appel API scraping: {}", scrapingApiUrl);
            HttpRequest request = HttpRequest.newBuilder(URI.create(scrapingApiUrl))
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, Object> responseBody = objectMapper.readValue(
                        response.body(),
                        new TypeReference<Map<String, Object>>() {
                        }
                );
                ScrapeProcessingResult processingResult = processScrapedPayload(responseBody);
                long duree = System.currentTimeMillis() - startTime;

                if (!processingResult.newOffers().isEmpty()) {
                    triggerNotificationsForNewOffers(processingResult.newOffers());
                }

                ScraperLogDocument logDoc = ScraperLogDocument.builder()
                        .dateDebut(dateDebut)
                        .dateFin(LocalDateTime.now())
                        .statut("SUCCES")
                        .nbOffres(processingResult.totalOffers())
                        .message("Scraping reussi, " + processingResult.newOffers().size() + " nouvelles offres")
                        .duree(duree)
                        .build();
                scraperLogService.save(logDoc);

                log.info("Scraping reussi: {} offres traitees, {} nouvelles", processingResult.totalOffers(), processingResult.newOffers().size());
                return responseBody;
            } else {
                long duree = System.currentTimeMillis() - startTime;
                String erreur = "Erreur HTTP: " + response.statusCode() + " - " + response.body();

                ScraperLogDocument scraperLog = ScraperLogDocument.builder()
                        .dateDebut(dateDebut)
                        .dateFin(LocalDateTime.now())
                        .statut("ERREUR")
                        .message("Erreur lors du scraping")
                        .erreur(erreur)
                        .duree(duree)
                        .build();
                scraperLogService.save(scraperLog);

                throw new RuntimeException("Erreur lors du scraping: " + response.statusCode());
            }

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            long duree = System.currentTimeMillis() - startTime;

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
            throw new RuntimeException("Echec de l'appel API scraping", e);
        }
    }

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
                        log.warn("Tentative {} echouee, retry dans {} ms", attempt, delayMs);
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Scraping echoue apres {} tentatives", maxRetries);
        throw new RuntimeException("Scraping impossible apres " + maxRetries + " tentatives", lastException);
    }

    private String buildSearchRequestBody(OffreFilter filter) {
        String keyword = escapeJson(firstNonBlank(filter.motCle()));
        String localisation = escapeJson(firstNonBlank(filter.localisation()));
        String dateDebut = filter.dateMin() == null
                ? "null"
                : "\"" + filter.dateMin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\"";

        return """
                {"keyword":%s,"date_debut":%s,"date_fin":null,"region":%s,"max_pages":1,"headless":true,"timeout_ms":30000}
                """.formatted(
                keyword == null ? "null" : "\"" + keyword + "\"",
                dateDebut,
                localisation == null ? "null" : "\"" + localisation + "\""
        );
    }

    private ScrapeProcessingResult processScrapedPayload(Map<String, Object> responseBody) {
        List<Map<String, Object>> rawOffers = extractOfferMaps(responseBody);
        List<OffreMarcheDocument> newOffers = new ArrayList<>();
        int totalOffers = 0;

        for (Map<String, Object> rawOffer : rawOffers) {
            OffreMarcheDocument mapped = mapToOffreDocument(rawOffer);
            if (mapped == null || mapped.getReference() == null || mapped.getReference().isBlank()) {
                continue;
            }

            totalOffers++;
            Optional<OffreMarcheDocument> existing = offreRepository.findByReference(mapped.getReference());
            if (existing.isPresent()) {
                continue;
            }

            OffreMarcheDocument saved = offreRepository.save(mapped);
            newOffers.add(saved);
        }

        return new ScrapeProcessingResult(totalOffers, newOffers);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractOfferMaps(Map<String, Object> responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return List.of();
        }

        Object candidates = firstNonNull(
                responseBody.get("offres"),
                responseBody.get("offers"),
                responseBody.get("data"),
                responseBody.get("results"),
                responseBody.get("items"),
                responseBody.get("rows")
        );

        if (candidates instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }

        if (responseBody.values().stream().allMatch(Map.class::isInstance)) {
            return responseBody.values().stream()
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }

        return List.of();
    }

    private OffreMarcheDocument mapToOffreDocument(Map<String, Object> rawOffer) {
        String reference = firstNonBlank(
                value(rawOffer, "reference"),
                value(rawOffer, "referencePortail"),
                value(rawOffer, "ref"),
                value(rawOffer, "id")
        );

        if (reference == null) {
            return null;
        }

        OffreMarcheDocument offre = new OffreMarcheDocument();
        offre.setReference(reference);
        offre.setIntitule(firstNonBlank(
                value(rawOffer, "intitule"),
                value(rawOffer, "titre"),
                value(rawOffer, "objet"),
                "Sans titre"
        ));
        offre.setDescription(firstNonBlank(
                value(rawOffer, "description"),
                value(rawOffer, "details"),
                value(rawOffer, "resume")
        ));
        offre.setOrganisme(firstNonBlank(
                value(rawOffer, "organisme"),
                value(rawOffer, "acheteur"),
                value(rawOffer, "maitreOuvrage"),
                value(rawOffer, "acheteurPublic")
        ));
        offre.setSecteur(firstNonBlank(
                value(rawOffer, "secteur"),
                value(rawOffer, "categorie"),
                value(rawOffer, "typeMarche"),
                value(rawOffer, "procedure")
        ));
        offre.setLocalisation(firstNonBlank(
                value(rawOffer, "localisation"),
                value(rawOffer, "region"),
                value(rawOffer, "ville")
        ));
        offre.setEmailContact(firstNonBlank(
                value(rawOffer, "emailContact"),
                value(rawOffer, "email"),
                value(rawOffer, "contactEmail")
        ));
        offre.setUrlOfficielle(firstNonBlank(
                value(rawOffer, "urlOfficielle"),
                value(rawOffer, "sourceUrl"),
                value(rawOffer, "url")
        ));
        offre.setDatePublication(parseDate(firstNonBlank(
                value(rawOffer, "datePublication"),
                value(rawOffer, "publicationDate"),
                value(rawOffer, "date_publication"),
                value(rawOffer, "datePublicationPortail")
        )));
        offre.setDateCloture(parseDate(firstNonBlank(
                value(rawOffer, "dateCloture"),
                value(rawOffer, "dateLimiteSoumission"),
                value(rawOffer, "closingDate"),
                value(rawOffer, "date_cloture"),
                value(rawOffer, "date_limite"),
                value(rawOffer, "deadline")
        )));
        offre.setDateCollecte(LocalDateTime.now());
        return offre;
    }

    private void triggerNotificationsForNewOffers(List<OffreMarcheDocument> newOffers) {
        log.info("Declenchement des notifications pour {} nouvelles offres", newOffers.size());
        for (OffreMarcheDocument offre : newOffers) {
            try {
                notificationService.createNotificationsForOffer(offre);
            } catch (Exception e) {
                log.error("Erreur notification pour offre {}: {}", offre.getReference(), e.getMessage());
            }
        }
    }

    private String value(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(raw.trim(), formatter);
            } catch (Exception ignored) {
            }
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() >= 10) {
            String candidate = normalized.substring(0, 10).replace('/', '-');
            try {
                return LocalDate.parse(candidate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception ignored) {
            }
        }

        log.warn("Date non parsee: {}", raw);
        return null;
    }

    private record ScrapeProcessingResult(int totalOffers, List<OffreMarcheDocument> newOffers) {
    }
}
