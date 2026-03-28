package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.offre.OffreFilter;
import com.emsi.marches_backend.dto.offre.OffreResponse;
import com.emsi.marches_backend.service.OffreService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/offres")
public class OffreController {

    private final OffreService offreService;

    public OffreController(OffreService offreService) {
        this.offreService = offreService;
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
}
