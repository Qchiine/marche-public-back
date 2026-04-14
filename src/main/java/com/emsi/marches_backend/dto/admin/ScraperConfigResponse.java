package com.emsi.marches_backend.dto.admin;

public record ScraperConfigResponse(
        long delayMs,
        int timeoutMs,
        boolean useMock
) {
}
