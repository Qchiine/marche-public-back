package com.emsi.marches_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Nom obligatoire")
        String nom,

        @NotBlank(message = "Prénom obligatoire")
        String prenom,

        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email,

        @NotBlank(message = "Mot de passe obligatoire")
        @Size(min = 8, message = "Mot de passe minimum 8 caractères")
        String motDePasse
) {}
