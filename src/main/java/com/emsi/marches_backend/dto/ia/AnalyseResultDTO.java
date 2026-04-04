package com.emsi.marches_backend.dto.ia;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyseResultDTO {

    private String referencePortail;
    private String objet;
    private String maitreDOuvrage;
    private String typeMarche;
    private String statut;
    private String datePublication;
    private String dateLimite;
    private String budgetEstime;
    private String region;
    private String urlDetail;
    private String source;

    // Résultat Gemini
    private double scorePertinence;
    private String resume;
    private List<String> pointsCles;
    private String recommandation;
    private String correspondanceProfil;
}