package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.dashboard.DashboardStatsResponse;
import com.emsi.marches_backend.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(Authentication authentication) {
        DashboardStatsResponse response = dashboardStatsService.getStatsForUser(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
