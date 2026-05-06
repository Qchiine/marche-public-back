package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void getNotifications_shouldReturnNotifications() {
        NotificationDocument notif = NotificationDocument.builder().id("n1").build();
        when(notificationService.getUserNotifications("user@example.com")).thenReturn(List.of(notif));

        var auth = new UsernamePasswordAuthenticationToken("user@example.com", null);
        var result = notificationController.getNotifications(auth);

        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void markAsRead_shouldReturnUpdatedNotification() {
        NotificationDocument notif = NotificationDocument.builder().id("n1").lue(true).build();
        when(notificationService.markAsRead("user@example.com", "n1")).thenReturn(notif);

        var auth = new UsernamePasswordAuthenticationToken("user@example.com", null);
        var result = notificationController.markAsRead("n1", auth);

        assertThat(result.getBody().isLue()).isTrue();
    }

    @Test
    void deleteNotification_shouldSucceed() {
        var auth = new UsernamePasswordAuthenticationToken("user@example.com", null);
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                notificationController.deleteNotification("n1", auth));
    }
}
