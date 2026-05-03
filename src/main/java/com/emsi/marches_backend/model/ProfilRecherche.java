package com.emsi.marches_backend.model;

import com.emsi.marches_backend.model.enums.NotificationFrequence;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profil de veille personnalise de l'utilisateur")
public class ProfilRecherche {

    @Schema(description = "Mots-cles d'interet", example = "[\"informatique\", \"reseau\"]")
    private List<String> motsCles;

    @Schema(description = "Secteurs d'activite suivis", example = "[\"IT\", \"BTP\"]")
    private List<String> secteurs;

    @Schema(description = "Localisation ciblee", example = "Rabat")
    private String localisation;

    @Schema(description = "Organismes publics suivis", example = "[\"Ministere de l'Education\"]")
    private List<String> organismes;

    @Schema(description = "Date limite maximale acceptee", example = "2026-06-30")
    private LocalDate dateLimiteMax;

    @Schema(description = "Frequence des notifications", example = "DAILY")
    private NotificationFrequence frequenceNotification = NotificationFrequence.DAILY;
}
