package com.emsi.marches_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetVerifyRequest(
        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email,

        @NotBlank(message = "Code obligatoire")
        @Pattern(regexp = "\\d{6}", message = "Le code doit contenir 6 chiffres")
        String code
) {}
