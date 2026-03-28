package com.emsi.marches_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token obligatoire")
        String refreshToken
) {
}
