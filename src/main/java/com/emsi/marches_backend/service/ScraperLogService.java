package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.ScraperLogDocument;
import com.emsi.marches_backend.repository.ScraperLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperLogService {

    private final ScraperLogRepository scraperLogRepository;

    public ScraperLogDocument save(ScraperLogDocument log) {
        return scraperLogRepository.save(log);
    }

    public Page<ScraperLogDocument> findAllLogs(Pageable pageable) {
        return scraperLogRepository.findAllByOrderByDateDebutDesc(pageable);
    }
}
