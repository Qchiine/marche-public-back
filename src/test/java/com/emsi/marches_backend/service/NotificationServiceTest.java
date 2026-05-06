package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.ProfilRecherche;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.NotificationFrequence;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.NotificationRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private EmailNotificationService emailNotificationService;
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getUserNotifications_shouldReturnNotifications() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        when(utilisateurRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        NotificationDocument notif = NotificationDocument.builder().id("n1").userId("u1").build();
        when(notificationRepository.findByUserIdOrderByDateCreationDesc("u1")).thenReturn(List.of(notif));

        List<NotificationDocument> result = notificationService.getUserNotifications("user@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("n1");
    }

    @Test
    void markAsRead_shouldMarkNotificationAsRead() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        NotificationDocument notif = NotificationDocument.builder().id("n1").userId("u1").lue(false).build();

        when(utilisateurRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findById("n1")).thenReturn(Optional.of(notif));
        when(notificationRepository.save(any())).thenReturn(notif);

        NotificationDocument result = notificationService.markAsRead("user@example.com", "n1");

        assertThat(result.isLue()).isTrue();
    }

    @Test
    void markAsRead_shouldThrowNotFoundWhenNotOwner() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        NotificationDocument notif = NotificationDocument.builder().id("n1").userId("other-user").build();

        when(utilisateurRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findById("n1")).thenReturn(Optional.of(notif));

        assertThatThrownBy(() -> notificationService.markAsRead("user@example.com", "n1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Notification introuvable");
    }

    @Test
    void matchesUserProfile_shouldMatchSecteur() {
        OffreMarcheDocument offre = new OffreMarcheDocument();
        offre.setSecteur("IT");
        offre.setIntitule("Test");
        offre.setDescription("Test");
        offre.setOrganisme("Test");
        offre.setLocalisation("Test");
        offre.setReference("Test");

        ProfilRecherche profil = new ProfilRecherche();
        profil.setSecteurs(List.of("IT"));

        UtilisateurDocument user = new UtilisateurDocument();
        user.setProfil(profil);
        user.setStatut(StatutCompteEnum.ACTIF);

        notificationService.createNotificationsForOffer(offre);
    }
}
