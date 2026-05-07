package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.user.ProfilRequest;
import com.emsi.marches_backend.dto.user.ProfilResponse;
import com.emsi.marches_backend.service.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfilControllerTest {

    @Mock
    private UtilisateurService utilisateurService;
    @InjectMocks
    private ProfilController profilController;

    @Test
    void soumettreQuestionnaire_shouldReturnProfil() {
        ProfilRequest request = new ProfilRequest(
                List.of("IT"), List.of("Casablanca"), null, null, null, null
        );
        ProfilResponse response = new ProfilResponse(
                List.of("IT"), List.of("IT"), "Casablanca", null, null, null
        );

        when(utilisateurService.validerQuestionnaire("john@example.com", request))
                .thenReturn(response);

        var auth = new UsernamePasswordAuthenticationToken("john@example.com", null);
        var result = profilController.soumettreQuestionnaire(auth, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().secteursChoisis()).contains("IT");
    }

    @Test
    void getProfil_shouldReturnProfil() {
        ProfilResponse response = new ProfilResponse(
                List.of("IT"), List.of("IT"), "Casablanca", null, null, null
        );

        when(utilisateurService.getProfil("john@example.com")).thenReturn(response);

        var auth = new UsernamePasswordAuthenticationToken("john@example.com", null);
        var result = profilController.getProfil(auth);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().secteursChoisis()).contains("IT");
    }
}
