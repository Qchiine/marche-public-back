package com.emsi.marches_backend.dto.admin;

import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;

import java.time.LocalDateTime;

public record AdminUserResponse(
        String id,
        String email,
        String nom,
        String prenom,
        RoleEnum role,
        StatutCompteEnum statut,
        LocalDateTime dateInscription
) {
}
