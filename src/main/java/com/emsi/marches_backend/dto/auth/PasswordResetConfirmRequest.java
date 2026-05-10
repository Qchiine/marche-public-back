package com.emsi.marches_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email,

        @NotBlank(message = "Code obligatoire")
        @Pattern(regexp = "\\d{6}", message = "Le code doit contenir 6 chiffres")
        String code,

        @NotBlank(message = "Nouveau mot de passe obligatoire")
        @Size(min = 8, message = "Mot de passe minimum 8 caracteres")
        String newPassword
) {}
