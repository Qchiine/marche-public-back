package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.offre.OffreFilterOptionsResponse;
import com.emsi.marches_backend.dto.offre.OffreResponse;
import com.emsi.marches_backend.service.OffreService;
import com.emsi.marches_backend.service.ScrapingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OffreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OffreService offreService;
    @Mock
    private ScrapingService scrapingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void searchOffres_shouldReturnPage() throws Exception {
        OffreResponse response = new OffreResponse("1", "REF-1", "Test", "Desc", "Org", "IT", "Loc", "ref@test.com", "http://test.com", null, null, null);
        when(offreService.searchByFilters(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/offres/search")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getFilterOptions_shouldReturnOptions() throws Exception {
        when(offreService.getFilterOptions()).thenReturn(new OffreFilterOptionsResponse(List.of("IT"), List.of("Casablanca")));

        mockMvc.perform(get("/api/offres/filters"))
                .andExpect(status().isOk());
    }
}
