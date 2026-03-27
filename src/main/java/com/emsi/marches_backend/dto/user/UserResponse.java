package com.emsi.marches_backend.dto.user;

import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        String id,
        String email,
        String nom,
        String prenom,
        RoleEnum role,
        StatutCompteEnum statut,
        List<String> motsClesInteret,
        List<String> secteursChoisis,
        String localisation,
        LocalDateTime dateInscription
) {}
