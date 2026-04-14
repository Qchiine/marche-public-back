package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.ProfilRecherche;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.repository.NotificationRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private MatchingService matchingService;

    @Test
    void matcherOffresVersUtilisateurs_shouldCreateNotificationForNominalMatch() {
        UtilisateurDocument utilisateur = userWithProfile("u-1",
                List.of("informatique"), List.of("services"), "casablanca");
        OffreMarcheDocument offre = offre("offre-1", "AO-1",
                "Maintenance informatique", "Support reseau",
                "Services", "Grand Casablanca");

        when(utilisateurRepository.findAll()).thenReturn(List.of(utilisateur));
        when(notificationRepository.existsByUserIdAndOffreId("u-1", "offre-1")).thenReturn(false);

        matchingService.matcherOffresVersUtilisateurs(List.of(offre));

        ArgumentCaptor<NotificationDocument> captor = ArgumentCaptor.forClass(NotificationDocument.class);
        verify(notificationRepository).save(captor.capture());
        NotificationDocument saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("u-1");
        assertThat(saved.getOffreId()).isEqualTo("offre-1");
        assertThat(saved.getReferenceOffre()).isEqualTo("AO-1");
    }

    @Test
    void matcherOffresVersUtilisateurs_shouldNotCreateNotificationForFalsePositiveKeyword() {
        UtilisateurDocument utilisateur = userWithProfile("u-2",
                List.of("btp"), List.of("services"), "casablanca");
        OffreMarcheDocument offre = offre("offre-2", "AO-2",
                "Maintenance informatique", "Support reseau",
                "Services", "Casablanca");

        when(utilisateurRepository.findAll()).thenReturn(List.of(utilisateur));

        matchingService.matcherOffresVersUtilisateurs(List.of(offre));

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any(NotificationDocument.class));
    }

    @Test
    void matcherOffresVersUtilisateurs_shouldIgnoreEmptyProfile() {
        UtilisateurDocument utilisateur = new UtilisateurDocument();
        utilisateur.setId("u-3");
        utilisateur.setProfil(new ProfilRecherche(Collections.emptyList(), Collections.emptyList(), null, null));

        OffreMarcheDocument offre = offre("offre-3", "AO-3",
                "Maintenance informatique", "Support reseau",
                "Services", "Casablanca");

        when(utilisateurRepository.findAll()).thenReturn(List.of(utilisateur));

        matchingService.matcherOffresVersUtilisateurs(List.of(offre));

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any(NotificationDocument.class));
        verify(notificationRepository, never()).existsByUserIdAndOffreId(anyString(), anyString());
    }

    @Test
    void matcherOffresVersUtilisateurs_shouldDoNothingWhenNoOffers() {
        matchingService.matcherOffresVersUtilisateurs(Collections.emptyList());

        verifyNoInteractions(utilisateurRepository);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void matcherOffresVersUtilisateurs_shouldNotCreateDuplicateNotification() {
        UtilisateurDocument utilisateur = userWithProfile("u-4",
                List.of("informatique"), List.of("services"), "casablanca");
        OffreMarcheDocument offre = offre("offre-4", "AO-4",
                "Maintenance informatique", "Support reseau",
                "Services", "Casablanca");

        when(utilisateurRepository.findAll()).thenReturn(List.of(utilisateur));
        when(notificationRepository.existsByUserIdAndOffreId("u-4", "offre-4")).thenReturn(true);

        matchingService.matcherOffresVersUtilisateurs(List.of(offre));

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any(NotificationDocument.class));
    }

    private UtilisateurDocument userWithProfile(
            String userId, List<String> motsCles, List<String> secteurs, String localisation
    ) {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId(userId);
        user.setProfil(new ProfilRecherche(motsCles, secteurs, localisation, null));
        return user;
    }

    private OffreMarcheDocument offre(
            String id, String reference, String intitule, String description, String secteur, String localisation
    ) {
        OffreMarcheDocument offre = new OffreMarcheDocument();
        offre.setId(id);
        offre.setReference(reference);
        offre.setIntitule(intitule);
        offre.setDescription(description);
        offre.setSecteur(secteur);
        offre.setLocalisation(localisation);
        return offre;
    }
}
