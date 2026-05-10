package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.auth.AuthResponse;
import com.emsi.marches_backend.dto.auth.LoginRequest;
import com.emsi.marches_backend.dto.auth.PasswordResetConfirmRequest;
import com.emsi.marches_backend.dto.auth.PasswordResetRequest;
import com.emsi.marches_backend.dto.auth.PasswordResetVerifyRequest;
import com.emsi.marches_backend.dto.auth.RefreshTokenRequest;
import com.emsi.marches_backend.dto.auth.RegisterRequest;
import com.emsi.marches_backend.service.AuthService;
import com.emsi.marches_backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrireNouveau(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authentifier(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request.refreshToken()));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok(Map.of(
                "message",
                "Si un compte existe avec cet email, un code de récupération a été envoyé."
        ));
    }

    @PostMapping("/password-reset/verify")
    public ResponseEntity<Map<String, String>> verifyPasswordResetCode(@Valid @RequestBody PasswordResetVerifyRequest request) {
        passwordResetService.verifyCode(request);
        return ResponseEntity.ok(Map.of("message", "Code valide."));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour."));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {
        return ResponseEntity.ok(Map.of("email", authentication.getName()));
    }
}
