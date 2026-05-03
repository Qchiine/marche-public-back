package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.offre.OffreFilter;
import com.emsi.marches_backend.dto.offre.OffreFilterOptionsResponse;
import com.emsi.marches_backend.dto.offre.OffreResponse;
import com.emsi.marches_backend.service.OffreService;
import com.emsi.marches_backend.service.ScrapingService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/offres")
public class OffreController {

    private final OffreService offreService;
    private final ScrapingService scrapingService;

    public OffreController(OffreService offreService, ScrapingService scrapingService) {
        this.offreService = offreService;
        this.scrapingService = scrapingService;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OffreResponse>> searchOffres(
            @RequestParam(name = "q", required = false) String motCle,
            @RequestParam(name = "secteur", required = false) String secteur,
            @RequestParam(name = "localisation", required = false) String localisation,
            @RequestParam(name = "statut", required = false) String statut,
            @RequestParam(name = "dateMin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateMin,
            @RequestParam(name = "dateLimiteMax", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateLimiteMax,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "date_desc") String sort
    ) {
        OffreFilter filter = new OffreFilter(motCle, secteur, localisation, statut, dateMin, dateLimiteMax, page, size, sort);
        Page<OffreResponse> result = offreService.searchByFilters(filter);

        boolean shouldTriggerSearchScraping = motCle != null
                && !motCle.isBlank()
                && page == 0
                && result.getTotalElements() == 0;

        if (shouldTriggerSearchScraping) {
            scrapingService.scrapeOffresForSearch(filter);
            result = offreService.searchByFilters(filter);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/filters")
    public ResponseEntity<OffreFilterOptionsResponse> getFilterOptions() {
        return ResponseEntity.ok(offreService.getFilterOptions());
    }

    @PostMapping("/scrape")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> scrapeOffres() {
        Map<String, Object> result = scrapingService.scrapeOffres();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scrape-with-retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> scrapeOffresWithRetry(
            @RequestParam(name = "maxRetries", defaultValue = "3") int maxRetries
    ) {
        Map<String, Object> result = scrapingService.scrapeOffresWithRetry(maxRetries);
        return ResponseEntity.ok(result);
    }
}
