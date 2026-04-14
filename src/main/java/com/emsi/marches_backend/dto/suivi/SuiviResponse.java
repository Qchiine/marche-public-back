package com.emsi.marches_backend.dto.suivi;

import com.emsi.marches_backend.model.enums.SuiviStatut;

import java.time.LocalDateTime;

public record SuiviResponse(
        String id,
        String userId,
        String offreId,
        SuiviStatut statut,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
