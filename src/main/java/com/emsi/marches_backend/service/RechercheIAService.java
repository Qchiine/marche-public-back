package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.ia.AnalyseResultDTO;
import com.emsi.marches_backend.dto.ia.FiltreRechercheDTO;
import com.emsi.marches_backend.dto.ia.RechercheResultatDTO;
import com.emsi.marches_backend.model.OffreMarche;
import com.emsi.marches_backend.model.RechercheIADocument;
import com.emsi.marches_backend.repository.OffreMarcheRepository;
import com.emsi.marches_backend.repository.RechercheIADocumentRepository;
import com.emsi.marches_backend.scraper.MarchePublicScraper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechercheIAService {

    private final MarchePublicScraper scraper;
    private final GeminiService geminiService;
    private final OffreMarcheRepository offreMarcheRepository;
    private final RechercheIADocumentRepository rechercheIADocumentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Méthode principale ────────────────────────────────────────────────────
    public RechercheResultatDTO rechercherEtAnalyser(FiltreRechercheDTO filtre, String userId) {

        // 1. Scraping
        log.info("Démarrage scraping marchespublics.gov.ma");
        List<AnalyseResultDTO> offres = scraper.scraperOffres(filtre);
        log.info("Offres scrapées : {}", offres.size());

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
}