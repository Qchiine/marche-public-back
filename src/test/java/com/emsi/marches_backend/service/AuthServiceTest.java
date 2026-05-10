package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.auth.AuthResponse;
import com.emsi.marches_backend.dto.auth.LoginRequest;
import com.emsi.marches_backend.dto.auth.RegisterRequest;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import com.emsi.marches_backend.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private EmailNotificationService emailNotificationService;
    @InjectMocks
    private AuthService authService;

    @Test
    void inscrireNouveau_shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "JOHN@EXAMPLE.COM", "password123");
        when(utilisateurRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(utilisateurRepository.save(any(UtilisateurDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponse response = authService.inscrireNouveau(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(RoleEnum.USER);
    }

    @Test
    void inscrireNouveau_shouldThrowConflictWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "existing@example.com", "password");
        when(utilisateurRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.inscrireNouveau(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email deja utilise");
    }

    @Test
    void authentifier_shouldReturnResponseWhenCredentialsValid() {
        LoginRequest request = new LoginRequest("user@example.com", "password");
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail("user@example.com");
        user.setMotDePasseHash("encoded");
        user.setRole(RoleEnum.USER);

        when(utilisateurRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponse response = authService.authentifier(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("access-token");
        verify(emailNotificationService).sendLoginNotification(user);
    }

    @Test
    void authentifier_shouldThrowUnauthorizedWhenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password");
        when(utilisateurRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authentifier(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email ou mot de passe invalide");
    }
}
