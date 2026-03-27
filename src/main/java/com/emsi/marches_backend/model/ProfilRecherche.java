package com.emsi.marches_backend.model;

import com.emsi.marches_backend.model.enums.NotificationFrequence;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profil de veille personnalisé de l'utilisateur")
public class ProfilRecherche {

    @Schema(description = "Mots-clés d'intérêt", example = "[\"informatique\", \"réseau\"]")
    private List<String> motsCles;

    @Schema(description = "Secteurs d'activité suivis", example = "[\"IT\", \"BTP\"]")
    private List<String> secteurs;

    @Schema(description = "Localisation ciblée", example = "Rabat")
    private String localisation;

    @Schema(description = "Fréquence des notifications", example = "DAILY")
    private NotificationFrequence frequenceNotification = NotificationFrequence.DAILY;
}
