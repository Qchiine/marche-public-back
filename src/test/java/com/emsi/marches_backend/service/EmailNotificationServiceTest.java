package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.UtilisateurDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @Test
    void sendNewOfferNotification_shouldSendEmail() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setPrenom("John");
        user.setNom("Doe");
        user.setEmail("john@example.com");

        OffreMarcheDocument offre = new OffreMarcheDocument();
        offre.setIntitule("Test Offer");
        offre.setOrganisme("Test Org");
        offre.setSecteur("IT");
        offre.setLocalisation("Casablanca");
        offre.setReference("REF-1");
        offre.setDateCloture(LocalDate.of(2026, 12, 31));

        NotificationDocument notification = NotificationDocument.builder().build();

        emailNotificationService.sendNewOfferNotification(user, offre, notification);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();

        assertThat(message.getTo()).containsExactly("john@example.com");
        assertThat(message.getSubject()).contains("IT");
        assertThat(message.getText()).contains("Test Offer");
    }

    @Test
    void sendLoginNotification_shouldSendEmail() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setPrenom("John");
        user.setNom("Doe");
        user.setEmail("john@example.com");

        emailNotificationService.sendLoginNotification(user);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();

        assertThat(message.getTo()).containsExactly("john@example.com");
        assertThat(message.getSubject()).contains("Nouvelle connexion");
        assertThat(message.getText()).contains("connexion");
    }
}
