package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.ia.FiltreRechercheDTO;
import com.emsi.marches_backend.dto.ia.RechercheResultatDTO;
import com.emsi.marches_backend.model.RechercheIADocument;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.service.RechercheIAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
public class RechercheIAController {

    private final RechercheIAService rechercheIAService;

    @PostMapping("/recherche")
    public ResponseEntity<RechercheResultatDTO> rechercher(
            @Valid @RequestBody FiltreRechercheDTO filtre,
            Authentication authentication) {
        String userId = extraireUserId(authentication);
        RechercheResultatDTO resultat = rechercheIAService.rechercherEtAnalyser(filtre, userId);
        return ResponseEntity.ok(resultat);
    }

    @GetMapping("/historique")
    public ResponseEntity<List<RechercheIADocument>> obtenirHistorique(Authentication authentication) {
        String userId = extraireUserId(authentication);
        List<RechercheIADocument> historique = rechercheIAService.getHistorique(userId);
        return ResponseEntity.ok(historique);
    }

    @GetMapping("/historique/{id}")
    public ResponseEntity<RechercheIADocument> obtenirRechercheDetail(
            @PathVariable String id,
            Authentication authentication) {
        String userId = extraireUserId(authentication);
        RechercheIADocument recherche = rechercheIAService.getRechercheById(id, userId);
        if (recherche == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(recherche);
    }

    /**
     * Extrait l'ID de l'utilisateur depuis l'authentification.
     * L'utilisateur authentifié est une instance de UtilisateurDocument
     * dont l'ID MongoDB est utilisé comme identifiant de l'utilisateur.
     */
    private String extraireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UtilisateurDocument) {
            UtilisateurDocument user = (UtilisateurDocument) principal;
            return user.getId();
        }
        return authentication.getName();
    }
}
