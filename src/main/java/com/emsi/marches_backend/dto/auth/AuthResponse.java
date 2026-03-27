package com.emsi.marches_backend.dto.auth;

import com.emsi.marches_backend.model.enums.RoleEnum;

public record AuthResponse(
        String token,
        String type,
        String userId,
        String email,
        String nom,
        String prenom,
        RoleEnum role
) {
    public AuthResponse(String token, String userId, String email, String nom, String prenom, RoleEnum role) {
        this(token, "Bearer", userId, email, nom, prenom, role);
    }
}
