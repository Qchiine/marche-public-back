package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.ia.AnalyseResultDTO;
import com.emsi.marches_backend.dto.ia.FiltreRechercheDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();

    private static final String PROMPT_SYSTEME = """
        Tu es un expert en marchés publics marocains.
        Analyse cette offre et retourne UNIQUEMENT un JSON valide avec ces champs :
        {
          "score_pertinence": <float entre 0.0 et 1.0>,
          "resume": "<résumé en 2-3 phrases>",
          "points_cles": ["point1", "point2", "point3"],
          "recommandation": "<conseil concret pour soumissionner>",
          "correspondance_profil": "<explication de la correspondance avec le profil>"
        }
        Barème du score :
        0.0-0.3 = peu pertinent | 0.3-0.6 = moyen | 0.6-0.8 = pertinent | 0.8-1.0 = très pertinent
        """;

    // ── Analyse d'une offre avec Gemini ──────────────────────────────────────
    public AnalyseResultDTO analyser(AnalyseResultDTO offre, FiltreRechercheDTO filtre) {
        try {
            String prompt  = construirePrompt(offre, filtre);
            String reponse = appelGemini(prompt);
            return enrichirOffre(offre, reponse);
        } catch (Exception e) {
            log.error("Erreur Gemini pour '{}' : {}", offre.getObjet(), e.getMessage());
            return fallback(offre);
        }
    }

    // ── Construction du prompt ────────────────────────────────────────────────
    private String construirePrompt(AnalyseResultDTO offre, FiltreRechercheDTO filtre) {
        String profil = filtre.getProfilEntreprise() != null
                ? filtre.getProfilEntreprise() : "Entreprise générale";
        String mots = filtre.getMotsCles() != null
                ? String.join(", ", filtre.getMotsCles()) : "Non spécifiés";

        return PROMPT_SYSTEME + """

            OFFRE À ANALYSER :
            - Référence   : %s
            - Objet       : %s
            - Organisme   : %s
            - Type        : %s
            - Statut      : %s
            - Date limite : %s
            - Budget      : %s
            - Région      : %s

            PROFIL DE L'ENTREPRISE : %s
            MOTS-CLÉS RECHERCHÉS   : %s
            """.formatted(
                nvl(offre.getReferencePortail()),
                offre.getObjet(),
                nvl(offre.getMaitreDOuvrage()),
                nvl(offre.getTypeMarche()),
                nvl(offre.getStatut()),
                nvl(offre.getDateLimite()),
                nvl(offre.getBudgetEstime()),
                nvl(offre.getRegion()),
                profil,
                mots
        );
    }

    // ── Appel API Gemini ──────────────────────────────────────────────────────
    private String appelGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "responseMimeType", "application/json"
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url, new HttpEntity<>(body, headers), Map.class
        );

        // Extraction du texte depuis la réponse Gemini
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) response.getBody().get("candidates");
        @SuppressWarnings("unchecked")
        Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");

        return (String) parts.get(0).get("text");
    }

    // ── Parse JSON Gemini → DTO ───────────────────────────────────────────────
    private AnalyseResultDTO enrichirOffre(AnalyseResultDTO offre, String json) {
        try {
            String cleaned = json
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            List<String> pointsCles = new ArrayList<>();
            if (node.has("points_cles") && node.get("points_cles").isArray())
                node.get("points_cles").forEach(n -> pointsCles.add(n.asText()));

            offre.setScorePertinence(node.path("score_pertinence").asDouble(0.5));
            offre.setResume(node.path("resume").asText("Résumé non disponible"));
            offre.setPointsCles(pointsCles);
            offre.setRecommandation(node.path("recommandation").asText(""));
            offre.setCorrespondanceProfil(node.path("correspondance_profil").asText(null));

        } catch (Exception e) {
            log.warn("Erreur parsing JSON Gemini : {}", e.getMessage());
            return fallback(offre);
        }
        return offre;
    }

    // ── Fallback si Gemini échoue ─────────────────────────────────────────────
    private AnalyseResultDTO fallback(AnalyseResultDTO offre) {
        offre.setScorePertinence(0.5);
        offre.setResume(offre.getObjet());
        offre.setPointsCles(List.of("Analyse Gemini indisponible"));
        offre.setRecommandation("Consultez le détail sur marchespublics.gov.ma");
        return offre;
    }

    private String nvl(String s) { return s != null ? s : "N/A"; }
}