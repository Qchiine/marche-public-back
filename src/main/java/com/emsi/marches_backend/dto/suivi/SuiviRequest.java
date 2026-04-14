package com.emsi.marches_backend.dto.suivi;

import com.emsi.marches_backend.model.enums.SuiviStatut;

public record SuiviRequest(
        SuiviStatut statut,
        String note
) {
}
