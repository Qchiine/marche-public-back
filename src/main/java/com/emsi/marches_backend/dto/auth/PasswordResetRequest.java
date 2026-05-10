package com.emsi.marches_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email
) {}
