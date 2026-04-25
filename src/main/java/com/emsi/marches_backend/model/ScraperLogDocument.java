package com.emsi.marches_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "scraper_logs")
public class ScraperLogDocument {

    @Id
    private String id;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String statut; // SUCCÈS, ERREUR, EN_COURS
    private Integer nbOffres;
    private String message;
    private String erreur; // Details de l'erreur si applicable
    private Long duree; // en ms
}
