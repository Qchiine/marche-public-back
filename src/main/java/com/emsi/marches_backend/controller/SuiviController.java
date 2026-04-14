package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.suivi.SuiviRequest;
import com.emsi.marches_backend.dto.suivi.SuiviResponse;
import com.emsi.marches_backend.model.enums.SuiviStatut;
import com.emsi.marches_backend.service.SuiviService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suivi")
public class SuiviController {

    private final SuiviService suiviService;

    public SuiviController(SuiviService suiviService) {
        this.suiviService = suiviService;
    }

    @PostMapping("/{offreId}")
    public ResponseEntity<SuiviResponse> create(
            @PathVariable String offreId,
            @RequestBody(required = false) SuiviRequest request,
            Authentication authentication
    ) {
        SuiviResponse created = suiviService.create(authentication.getName(), offreId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SuiviResponse>> list(
            @RequestParam(required = false) SuiviStatut statut,
            Authentication authentication
    ) {
        return ResponseEntity.ok(suiviService.list(authentication.getName(), statut));
    }

    @PatchMapping("/{suiviId}")
    public ResponseEntity<SuiviResponse> update(
            @PathVariable String suiviId,
            @RequestBody SuiviRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(suiviService.update(authentication.getName(), suiviId, request));
    }

    @DeleteMapping("/{suiviId}")
    public ResponseEntity<Void> delete(@PathVariable String suiviId, Authentication authentication) {
        suiviService.delete(authentication.getName(), suiviId);
        return ResponseEntity.noContent().build();
    }
}
