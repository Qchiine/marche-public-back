package com.emsi.marches_backend.dto.admin;

public record ScraperConfigRequest(
        Long delayMs,
        Integer timeoutMs,
        Boolean useMock
) {
}
