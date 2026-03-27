package com.emsi.marches_backend.dto.user;

import com.emsi.marches_backend.model.enums.NotificationFrequence;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProfilRequest(

        @NotEmpty(message = "Au moins un mot-clé requis")
        List<String> motsClesInteret,

        @NotEmpty(message = "Au moins un secteur requis")
        List<String> secteursChoisis,

        String localisation,

        @NotNull(message = "Fréquence de notification obligatoire")
        NotificationFrequence frequenceNotification
) {}
