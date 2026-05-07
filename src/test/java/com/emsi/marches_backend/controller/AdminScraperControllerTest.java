package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.model.ScraperLogDocument;
import com.emsi.marches_backend.service.ScraperLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminScraperControllerTest {

    @Mock
    private ScraperLogService scraperLogService;
    @InjectMocks
    private AdminScraperController adminScraperController;

    @Test
    void getLogs_shouldReturnLogs() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ScraperLogDocument log = ScraperLogDocument.builder()
                .dateDebut(LocalDateTime.now())
                .statut("SUCCES")
                .build();
        when(scraperLogService.findAllLogs(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(log)));

        var result = adminScraperController.getLogs(0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).hasSize(1);
    }
}
