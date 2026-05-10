package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.auth.AuthResponse;
import com.emsi.marches_backend.dto.auth.LoginRequest;
import com.emsi.marches_backend.dto.auth.RegisterRequest;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import com.emsi.marches_backend.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailNotificationService emailNotificationService;

    public AuthService(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailNotificationService emailNotificationService
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailNotificationService = emailNotificationService;
    }

    public AuthResponse inscrireNouveau(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (utilisateurRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email deja utilise");
        }

        UtilisateurDocument utilisateur = new UtilisateurDocument();
        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(normalizedEmail);
        utilisateur.setMotDePasseHash(passwordEncoder.encode(request.password()));
        utilisateur.setRole(RoleEnum.USER);
        utilisateur.setStatut(StatutCompteEnum.PROFIL_INCOMPLET);

        UtilisateurDocument savedUser = utilisateurRepository.save(utilisateur);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse authentifier(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        UtilisateurDocument utilisateur = utilisateurRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe invalide"));

        if (!passwordEncoder.matches(request.password(), utilisateur.getMotDePasseHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe invalide");
        }

        emailNotificationService.sendLoginNotification(utilisateur);

        return buildAuthResponse(utilisateur);
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }

        String email = jwtTokenProvider.extractEmail(refreshToken);
        UtilisateurDocument utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));

        return buildAuthResponse(utilisateur);
    }

    private AuthResponse buildAuthResponse(UtilisateurDocument utilisateur) {
        String accessToken = jwtTokenProvider.generateAccessToken(utilisateur);
        String refreshToken = jwtTokenProvider.generateRefreshToken(utilisateur);

        return new AuthResponse(
                accessToken,
                refreshToken,
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getRole()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
