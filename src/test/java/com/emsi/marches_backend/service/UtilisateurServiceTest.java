package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.user.ProfilRequest;
import com.emsi.marches_backend.dto.user.ProfilResponse;
import com.emsi.marches_backend.model.ProfilRecherche;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.NotificationFrequence;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UtilisateurService utilisateurService;

    @Test
    void validerQuestionnaire_shouldUpdateProfileAndActivateAccount() {
        String email = "qchiine@gmail.com";
        ProfilRequest request = new ProfilRequest(
                List.of("java", "spring"),
                List.of("IT"),
                "Casablanca",
                null,
                null,
                NotificationFrequence.DAILY
        );

        UtilisateurDocument user = new UtilisateurDocument();
        user.setEmail(email);
        user.setStatut(StatutCompteEnum.PROFIL_INCOMPLET);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(utilisateurRepository.save(any(UtilisateurDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfilResponse response = utilisateurService.validerQuestionnaire(email, request);

        assertThat(response.motsClesInteret()).containsExactly("java", "spring");
        assertThat(response.secteursChoisis()).containsExactly("IT");
        assertThat(response.localisation()).isEqualTo("Casablanca");
        assertThat(response.frequenceNotification()).isEqualTo(NotificationFrequence.DAILY);
        assertThat(user.getStatut()).isEqualTo(StatutCompteEnum.ACTIF);
        assertThat(user.getProfil()).isNotNull();
    }

    @Test
    void getProfil_shouldThrowNotFound_whenProfileMissing() {
        String email = "qchiine@gmail.com";
        UtilisateurDocument user = new UtilisateurDocument();
        user.setEmail(email);
        user.setProfil(null);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> utilisateurService.getProfil(email))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    void getProfil_shouldReturnStoredProfile() {
        String email = "qchiine@gmail.com";
        UtilisateurDocument user = new UtilisateurDocument();
        user.setEmail(email);
        user.setProfil(new ProfilRecherche(
                List.of("reseau"),
                List.of("IT"),
                "Rabat",
                null,
                null,
                NotificationFrequence.WEEKLY
        ));

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ProfilResponse response = utilisateurService.getProfil(email);
        assertThat(response.motsClesInteret()).containsExactly("reseau");
        assertThat(response.frequenceNotification()).isEqualTo(NotificationFrequence.WEEKLY);
    }
}
