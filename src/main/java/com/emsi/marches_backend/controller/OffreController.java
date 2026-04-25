package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.offre.OffreFilter;
import com.emsi.marches_backend.dto.offre.OffreResponse;
import com.emsi.marches_backend.service.OffreService;
import com.emsi.marches_backend.service.ScrapingService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(name = "dateMin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateMin,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "date_desc") String sort
    ) {
        OffreFilter filter = new OffreFilter(motCle, secteur, dateMin, page, size, sort);
        return ResponseEntity.ok(offreService.searchByFilters(filter));
    }

    @PostMapping("/scrape")
    public ResponseEntity<Map<String, Object>> scrapeOffres() {
        Map<String, Object> result = scrapingService.scrapeOffres();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scrape-with-retry")
    public ResponseEntity<Map<String, Object>> scrapeOffresWithRetry(
            @RequestParam(name = "maxRetries", defaultValue = "3") int maxRetries
    ) {
        Map<String, Object> result = scrapingService.scrapeOffresWithRetry(maxRetries);
        return ResponseEntity.ok(result);
    }
}
