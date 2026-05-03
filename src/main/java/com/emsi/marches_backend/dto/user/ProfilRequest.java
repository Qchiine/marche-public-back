package com.emsi.marches_backend.dto.user;

import com.emsi.marches_backend.model.enums.NotificationFrequence;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ProfilRequest(

        @NotEmpty(message = "Au moins un mot-cle requis")
        List<String> motsClesInteret,

        @NotEmpty(message = "Au moins un secteur requis")
        List<String> secteursChoisis,

        String localisation,

        List<String> organismes,

        LocalDate dateLimiteMax,

        @NotNull(message = "Frequence de notification obligatoire")
        NotificationFrequence frequenceNotification
) {}
