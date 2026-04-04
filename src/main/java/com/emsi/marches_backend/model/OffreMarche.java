package com.emsi.marches_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "offres_marche")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreMarche {

    @Id
    private String id;

    private String referencePortail;
    private String objet;
    private String maitreDOuvrage;
    private TypeMarche typeMarche;
    private StatutOffre statut;

    private String datePublication;
    private String dateLimite;
    private String budgetEstime;
    private String region;
    private String urlDetail;
    private String source;

    private LocalDateTime dateScrap;

    // Résultat Gemini
    private String resumeIA;
    private String pointsClesIA;
    private String recommandationIA;
    private String correspondanceProfilIA;
    private Double scorePertinence;

    public enum TypeMarche {
        TRAVAUX, FOURNITURES, SERVICES, ALL
    }

    public enum StatutOffre {
        EN_COURS, CLOTURE, PUBLIE, ALL
    }
}