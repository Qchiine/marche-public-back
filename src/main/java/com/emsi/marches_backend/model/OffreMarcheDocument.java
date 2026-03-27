package com.emsi.marches_backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "offres")
@Schema(description = "Offre de marché public collectée automatiquement")
public class OffreMarcheDocument {

    @Id
    @Schema(description = "Identifiant MongoDB", example = "64a1f2e3b5c9d80012cd7890")
    private String id;

    @Indexed(unique = true)
    @Schema(description = "Référence officielle de l'appel d'offres", example = "AO-2024-001")
    private String reference;

    @Schema(description = "Intitulé de l'appel d'offres", example = "Fourniture de matériel informatique")
    private String intitule;

    @Schema(description = "Description détaillée")
    private String description;

    @Schema(description = "Organisme émetteur", example = "Ministère de l'Éducation")
    private String organisme;

    @Schema(description = "Secteur d'activité", example = "Informatique")
    private String secteur;

    @Schema(description = "Localisation géographique", example = "Casablanca")
    private String localisation;

    @Schema(description = "Email de contact", example = "marches@ministere.ma")
    private String emailContact;

    @Schema(description = "URL officielle de l'appel d'offres")
    private String urlOfficielle;

    @Schema(description = "Date de publication", example = "2024-01-15")
    private LocalDate datePublication;

    @Schema(description = "Date de clôture", example = "2024-02-15")
    private LocalDate dateCloture;

    @CreatedDate
    @Schema(description = "Date de collecte automatique (auto)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dateCollecte;
}
