package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.ScraperLogDocument;
import com.emsi.marches_backend.repository.ScraperLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScraperLogServiceTest {

    @Mock
    private ScraperLogRepository scraperLogRepository;
    @InjectMocks
    private ScraperLogService scraperLogService;

    @Test
    void findAllLogs_shouldReturnLogs() {
        ScraperLogDocument log = ScraperLogDocument.builder()
                .dateDebut(LocalDateTime.now())
                .statut("SUCCES")
                .build();
        when(scraperLogRepository.findAllByOrderByDateDebutDesc(org.mockito.ArgumentMatchers.any())).thenReturn(
                new org.springframework.data.domain.PageImpl<>(List.of(log))
        );

        var result = scraperLogService.findAllLogs(org.springframework.data.domain.PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }
}
