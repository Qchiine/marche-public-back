package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.model.ScraperLogDocument;
import com.emsi.marches_backend.service.ScraperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/scraper")
@RequiredArgsConstructor
public class AdminScraperController {

    private final ScraperLogService scraperLogService;

    @GetMapping("/logs")
    public ResponseEntity<Page<ScraperLogDocument>> getLogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ScraperLogDocument> logs = scraperLogService.findAllLogs(pageable);
        return ResponseEntity.ok(logs);
    }
}
