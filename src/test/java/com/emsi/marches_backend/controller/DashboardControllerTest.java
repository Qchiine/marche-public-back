package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.dashboard.DashboardStatsResponse;
import com.emsi.marches_backend.service.DashboardStatsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardStatsService dashboardStatsService;
    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @Test
    void getStats_shouldReturnStats() throws Exception {
        when(dashboardStatsService.getStatsForUser("user@example.com"))
                .thenReturn(new DashboardStatsResponse(10L, 2L, 1L, 3L, 1L, 5L, 2L, 1L));

        var auth = new UsernamePasswordAuthenticationToken("user@example.com", null);

        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
        mockMvc.perform(get("/api/dashboard/stats").principal(auth))
                .andExpect(status().isOk());
    }
}
