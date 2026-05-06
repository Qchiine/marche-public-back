package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.repository.NotificationRepository;
import com.emsi.marches_backend.repository.OffreRepository;
import com.emsi.marches_backend.repository.SuiviRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardStatsServiceTest {

    @Mock
    private OffreRepository offreRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SuiviRepository suiviRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @InjectMocks
    private DashboardStatsService dashboardStatsService;

    @Test
    void getStatsForUser_shouldReturnStats() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail("user@example.com");
        when(utilisateurRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(offreRepository.count()).thenReturn(10L);
        when(notificationRepository.countByUserId("u1")).thenReturn(3L);
        when(suiviRepository.countByUserId("u1")).thenReturn(2L);

        var stats = dashboardStatsService.getStatsForUser("user@example.com");

        assertThat(stats).isNotNull();
    }
}
