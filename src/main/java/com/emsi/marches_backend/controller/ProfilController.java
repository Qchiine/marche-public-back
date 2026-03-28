package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.user.ProfilRequest;
import com.emsi.marches_backend.dto.user.ProfilResponse;
import com.emsi.marches_backend.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profil")
public class ProfilController {

    private final UtilisateurService utilisateurService;

    public ProfilController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PutMapping
    public ResponseEntity<ProfilResponse> soumettreQuestionnaire(
            Authentication authentication,
            @Valid @RequestBody ProfilRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(utilisateurService.validerQuestionnaire(email, request));
    }

    @GetMapping
    public ResponseEntity<ProfilResponse> getProfil(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(utilisateurService.getProfil(email));
    }
}
