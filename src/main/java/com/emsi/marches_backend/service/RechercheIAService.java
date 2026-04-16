package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.ia.AnalyseResultDTO;
import com.emsi.marches_backend.dto.ia.FiltreRechercheDTO;
import com.emsi.marches_backend.dto.ia.RechercheResultatDTO;
import com.emsi.marches_backend.event.OffreCollectedEvent;
import com.emsi.marches_backend.model.OffreMarche;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.RechercheIADocument;
import com.emsi.marches_backend.repository.OffreMarcheRepository;
import com.emsi.marches_backend.repository.OffreRepository;
import com.emsi.marches_backend.repository.RechercheIADocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechercheIAService {
    private final GeminiService geminiService;
    private final OffreMarcheRepository offreMarcheRepository;
    private final OffreRepository offreRepository;
    private final RechercheIADocumentRepository rechercheIADocumentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Méthode principale ────────────────────────────────────────────────────
    public RechercheResultatDTO rechercherEtAnalyser(FiltreRechercheDTO filtre, String userId) {

        // 1. Récupération des offres depuis la base de données
        log.info("Récupération des offres depuis la base de données");
        List<OffreMarcheDocument> offresMarcheDoc = offreRepository.findAll();
        List<AnalyseResultDTO> offres = offresMarcheDoc.stream()
                .map(this::convertToAnalyseResultDTO)
                .toList();
        log.info("Offres récupérées : {}", offres.size());

        if (offres.isEmpty()) {
            return RechercheResultatDTO.builder()
                    .succes(true).totalScrape(0).totalFiltres(0)
                    .offres(List.of())
                    .message("Aucune offre trouvée pour ces critères.")
                    .build();
        }

        // 2. Analyse Gemini offre par offre
        List<AnalyseResultDTO> analysees = new ArrayList<>();
        for (AnalyseResultDTO offre : offres) {
            AnalyseResultDTO result = geminiService.analyser(offre, filtre);
            analysees.add(result);
            // Pause courte pour respecter le rate limit Gemini
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        // 3. Tri par score décroissant
        analysees.sort(Comparator.comparingDouble(AnalyseResultDTO::getScorePertinence).reversed());

        // 4. Persistance en BDD
        persisterOffres(analysees, filtre);
        publierCollectePourMatching(analysees);
        sauvegarderHistorique(filtre, userId, analysees.size());

        return RechercheResultatDTO.builder()
                .succes(true)
                .totalScrape(offres.size())
                .totalFiltres(analysees.size())
                .offres(analysees)
                .build();
    }

    // ── Historique ────────────────────────────────────────────────────────────
    public List<RechercheIADocument> getHistorique(String userId) {
        if (userId == null) return List.of();
        return rechercheIADocumentRepository.findTop10ByUserIdOrderByDateRechercheDesc(userId);
    }

    public RechercheIADocument getRechercheById(String id, String userId) {
        Optional<RechercheIADocument> recherche = rechercheIADocumentRepository.findById(id);
        if (recherche.isEmpty()) return null;
        
        RechercheIADocument doc = recherche.get();
        if (!doc.getUserId().equals(userId)) return null;
        
        return doc;
    }

    // ── Persistance offres ────────────────────────────────────────────────────
    private void persisterOffres(List<AnalyseResultDTO> offres, FiltreRechercheDTO filtre) {
        for (AnalyseResultDTO dto : offres) {
            try {
                Optional<OffreMarche> existante = dto.getReferencePortail() != null
                        ? offreMarcheRepository.findByReferencePortail(dto.getReferencePortail())
                        : Optional.empty();

                OffreMarche offre = existante.orElseGet(OffreMarche::new);
                offre.setReferencePortail(dto.getReferencePortail());
                offre.setObjet(dto.getObjet());
                offre.setMaitreDOuvrage(dto.getMaitreDOuvrage());
                offre.setDatePublication(dto.getDatePublication());
                offre.setDateLimite(dto.getDateLimite());
                offre.setBudgetEstime(dto.getBudgetEstime());
                offre.setRegion(dto.getRegion());
                offre.setUrlDetail(dto.getUrlDetail());
                offre.setSource(dto.getSource());
                offre.setScorePertinence(dto.getScorePertinence());
                offre.setResumeIA(dto.getResume());
                offre.setRecommandationIA(dto.getRecommandation());
                offre.setCorrespondanceProfilIA(dto.getCorrespondanceProfil());

                if (dto.getPointsCles() != null)
                    offre.setPointsClesIA(objectMapper.writeValueAsString(dto.getPointsCles()));

                offreMarcheRepository.save(offre);
            } catch (Exception e) {
                log.warn("Erreur persistance offre : {}", e.getMessage());
            }
        }
    }

    private void publierCollectePourMatching(List<AnalyseResultDTO> offresAnalysees) {
        List<OffreMarcheDocument> offresCollectees = new ArrayList<>();
        for (AnalyseResultDTO dto : offresAnalysees) {
            try {
                String reference = construireReference(dto);
                OffreMarcheDocument offre = offreRepository.findByReference(reference)
                        .orElseGet(OffreMarcheDocument::new);

                offre.setReference(reference);
                offre.setIntitule(dto.getObjet());
                offre.setDescription(dto.getResume());
                offre.setOrganisme(dto.getMaitreDOuvrage());
                offre.setSecteur(dto.getTypeMarche());
                offre.setLocalisation(dto.getRegion());
                offre.setUrlOfficielle(dto.getUrlDetail());
                offre.setDatePublication(parseDate(dto.getDatePublication()));
                offre.setDateCloture(parseDate(dto.getDateLimite()));

                OffreMarcheDocument saved = offreRepository.save(offre);
                offresCollectees.add(saved);
            } catch (Exception e) {
                log.warn("Erreur persistance offre pour matching: {}", e.getMessage());
            }
        }

        if (!offresCollectees.isEmpty()) {
            applicationEventPublisher.publishEvent(new OffreCollectedEvent(offresCollectees));
        }
    }

    private String construireReference(AnalyseResultDTO dto) {
        if (hasText(dto.getReferencePortail())) {
            return dto.getReferencePortail().trim();
        }
        if (hasText(dto.getUrlDetail())) {
            return "URL-" + Integer.toHexString(dto.getUrlDetail().trim().toLowerCase(Locale.ROOT).hashCode());
        }
        if (hasText(dto.getObjet())) {
            return "OBJ-" + Integer.toHexString(dto.getObjet().trim().toLowerCase(Locale.ROOT).hashCode());
        }
        return "AUTO-" + UUID.randomUUID();
    }

    private LocalDate parseDate(String value) {
        if (!hasText(value)) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );

        String trimmed = value.trim();
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
                // Essai format suivant
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // ── Sauvegarde historique ─────────────────────────────────────────────────
    private void sauvegarderHistorique(FiltreRechercheDTO filtre, String userId, int total) {
        if (userId == null) return;
        rechercheIADocumentRepository.save(RechercheIADocument.builder()
                .userId(userId)
                .motsCles(filtre.getMotsCles() != null ? String.join(",", filtre.getMotsCles()) : null)
                .typeMarche(filtre.getTypeMarche() != null ? filtre.getTypeMarche().name() : null)
                .statut(filtre.getStatut() != null ? filtre.getStatut().name() : null)
                .region(filtre.getRegion())
                .maitreDOuvrage(filtre.getMaitreDOuvrage())
                .budgetMin(filtre.getBudgetMin())
                .budgetMax(filtre.getBudgetMax())
                .profilEntreprise(filtre.getProfilEntreprise())
                .totalResultats(total)
                .build());
    }

    // ── Conversion OffreMarcheDocument vers AnalyseResultDTO ─────────────────
    private AnalyseResultDTO convertToAnalyseResultDTO(OffreMarcheDocument doc) {
        return AnalyseResultDTO.builder()
                .objet(doc.getIntitule())
                .maitreDOuvrage(doc.getOrganisme())
                .typeMarche(doc.getSecteur())
                .region(doc.getLocalisation())
                .urlDetail(doc.getUrlOfficielle())
                .datePublication(doc.getDatePublication() != null ? doc.getDatePublication().toString() : null)
                .dateLimite(doc.getDateCloture() != null ? doc.getDateCloture().toString() : null)
                .source("database")
                .build();
    }
}
