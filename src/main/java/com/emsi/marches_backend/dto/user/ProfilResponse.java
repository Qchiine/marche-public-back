package com.emsi.marches_backend.dto.user;

import com.emsi.marches_backend.model.enums.NotificationFrequence;

import java.util.List;

public record ProfilResponse(
        List<String> motsClesInteret,
        List<String> secteursChoisis,
        String localisation,
        NotificationFrequence frequenceNotification
) {
}
