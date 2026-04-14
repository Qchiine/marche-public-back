package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.suivi.SuiviRequest;
import com.emsi.marches_backend.dto.suivi.SuiviResponse;
import com.emsi.marches_backend.model.SuiviDocument;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.SuiviStatut;
import com.emsi.marches_backend.repository.SuiviRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuiviServiceTest {

    @Mock
    private SuiviRepository suiviRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private SuiviService suiviService;

    @Test
    void create_shouldPersistSuiviWithDefaultStatut() {
        String email = "user@example.com";
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail(email);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(suiviRepository.findByUserIdAndOffreId("u1", "offre-1")).thenReturn(Optional.empty());
        when(suiviRepository.save(any(SuiviDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuiviResponse response = suiviService.create(email, "offre-1", new SuiviRequest(null, "note"));

        assertThat(response.userId()).isEqualTo("u1");
        assertThat(response.statut()).isEqualTo(SuiviStatut.INTERESSE);
        assertThat(response.offreId()).isEqualTo("offre-1");
    }

    @Test
    void create_shouldThrowConflictWhenDuplicate() {
        String email = "user@example.com";
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail(email);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(suiviRepository.findByUserIdAndOffreId("u1", "offre-1"))
                .thenReturn(Optional.of(new SuiviDocument()));

        assertThatThrownBy(() -> suiviService.create(email, "offre-1", new SuiviRequest(SuiviStatut.INTERESSE, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void list_shouldApplyStatutFilter() {
        String email = "user@example.com";
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail(email);

        SuiviDocument suivi = SuiviDocument.builder()
                .id("s1")
                .userId("u1")
                .offreId("offre-1")
                .statut(SuiviStatut.INTERESSE)
                .build();

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(suiviRepository.findByUserIdAndStatutOrderByUpdatedAtDesc("u1", SuiviStatut.INTERESSE))
                .thenReturn(List.of(suivi));

        List<SuiviResponse> result = suiviService.list(email, SuiviStatut.INTERESSE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).statut()).isEqualTo(SuiviStatut.INTERESSE);
    }

    @Test
    void update_shouldRejectWhenSuiviNotOwnedByUser() {
        String email = "user@example.com";
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail(email);

        SuiviDocument suivi = SuiviDocument.builder()
                .id("s1")
                .userId("other-user")
                .offreId("offre-1")
                .statut(SuiviStatut.INTERESSE)
                .build();

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(suiviRepository.findById("s1")).thenReturn(Optional.of(suivi));

        assertThatThrownBy(() -> suiviService.update(email, "s1", new SuiviRequest(SuiviStatut.POSTULE, "ok")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_shouldDeleteOwnedSuivi() {
        String email = "user@example.com";
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail(email);

        SuiviDocument suivi = SuiviDocument.builder()
                .id("s1")
                .userId("u1")
                .offreId("offre-1")
                .statut(SuiviStatut.INTERESSE)
                .build();

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(suiviRepository.findById("s1")).thenReturn(Optional.of(suivi));

        suiviService.delete(email, "s1");

        ArgumentCaptor<SuiviDocument> captor = ArgumentCaptor.forClass(SuiviDocument.class);
        verify(suiviRepository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("s1");
    }
}
