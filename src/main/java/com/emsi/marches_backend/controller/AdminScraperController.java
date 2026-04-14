package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.admin.ScraperConfigRequest;
import com.emsi.marches_backend.dto.admin.ScraperConfigResponse;
import com.emsi.marches_backend.dto.ia.AnalyseResultDTO;
import com.emsi.marches_backend.dto.ia.FiltreRechercheDTO;
import com.emsi.marches_backend.scraper.MarchePublicScraper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/scraper")
@PreAuthorize("hasRole('ADMIN')")
public class AdminScraperController {

    private final MarchePublicScraper marchePublicScraper;

    public AdminScraperController(MarchePublicScraper marchePublicScraper) {
        this.marchePublicScraper = marchePublicScraper;
    }

    @GetMapping("/config")
    public ResponseEntity<ScraperConfigResponse> getConfig() {
        return ResponseEntity.ok(currentConfig());
    }

    @PutMapping("/config")
    public ResponseEntity<ScraperConfigResponse> updateConfig(@RequestBody ScraperConfigRequest request) {
        if (request.delayMs() != null) {
            if (request.delayMs() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "delayMs doit etre >= 0");
            }
            marchePublicScraper.setDelayMs(request.delayMs());
        }
        if (request.timeoutMs() != null) {
            if (request.timeoutMs() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeoutMs doit etre > 0");
            }
            marchePublicScraper.setTimeoutMs(request.timeoutMs());
        }
        if (request.useMock() != null) {
            marchePublicScraper.setUseMock(request.useMock());
        }
        return ResponseEntity.ok(currentConfig());
    }

    @PostMapping("/run-now")
    public ResponseEntity<Map<String, Object>> runNow() {
        FiltreRechercheDTO filtre = new FiltreRechercheDTO();
        List<AnalyseResultDTO> offres = marchePublicScraper.scraperOffres(filtre);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "ok");
        payload.put("source", "manual-run");
        payload.put("count", offres.size());
        return ResponseEntity.ok(payload);
    }

    private ScraperConfigResponse currentConfig() {
        return new ScraperConfigResponse(
                marchePublicScraper.getDelayMs(),
                marchePublicScraper.getTimeoutMs(),
                marchePublicScraper.isUseMock()
        );
    }
}
