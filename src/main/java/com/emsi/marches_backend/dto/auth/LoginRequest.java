package com.emsi.marches_backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email,

        @JsonAlias({"password", "motDePasse"})
        @NotBlank(message = "Mot de passe obligatoire")
        @Size(min = 8, message = "Mot de passe minimum 8 caracteres")
        String password
) {}
