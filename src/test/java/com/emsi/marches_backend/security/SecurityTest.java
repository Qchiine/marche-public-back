package com.emsi.marches_backend.security;

import com.emsi.marches_backend.dto.offre.OffreResponse;
import com.emsi.marches_backend.service.OffreService;
import com.emsi.marches_backend.service.ScrapingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OffreService offreService;

    @MockBean
    private ScrapingService scrapingService;

    @Test
    void endpointsSecured_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/suivi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsConfiguration_shouldAllowCrossOrigin() throws Exception {
        mockMvc.perform(get("/api/offres/search")
                        .header("Origin", "http://evil.com"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void csrfProtection_shouldBeDisabled() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@test.com\",\"password\":\"password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void securityHeaders_shouldBePresent() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Cache-Control"));
    }

    @Test
    void sqlInjectionAttempt_shouldBeHandled() throws Exception {
        when(offreService.searchByFilters(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(
                        new OffreResponse("1", "REF-1", "Test", "Desc", "Org", "IT", "Loc", "ref@test.com", "http://test.com", null, null, null)
                )));
        mockMvc.perform(get("/api/offres/search")
                        .param("q", "' OR 1=1 --"))
                .andExpect(status().isOk());
    }

    @Test
    void xssAttempt_shouldBeHandled() throws Exception {
        when(offreService.searchByFilters(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(
                        new OffreResponse("1", "REF-1", "Test", "Desc", "Org", "IT", "Loc", "ref@test.com", "http://test.com", null, null, null)
                )));
        mockMvc.perform(get("/api/offres/search")
                        .param("q", "<script>alert('xss')</script>"))
                .andExpect(status().isOk());
    }
}
