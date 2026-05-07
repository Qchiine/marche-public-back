package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.suivi.SuiviRequest;
import com.emsi.marches_backend.dto.suivi.SuiviResponse;
import com.emsi.marches_backend.service.SuiviService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SuiviControllerTest {

    @Mock
    private SuiviService suiviService;
    @InjectMocks
    private SuiviController suiviController;

    private MockMvc mockMvc;

    @Test
    void create_shouldReturn201() throws Exception {
        SuiviResponse response = new SuiviResponse("1", "user1", "offre1", com.emsi.marches_backend.model.enums.SuiviStatut.INTERESSE, null, null, null);
        when(suiviService.create(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        var auth = new UsernamePasswordAuthenticationToken("user@example.com", null);

        mockMvc = MockMvcBuilders.standaloneSetup(suiviController).build();
        mockMvc.perform(post("/api/suivi/offre1")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"INTERESSE\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void list_shouldReturnList() throws Exception {
        when(suiviService.list(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        var auth = new UsernamePasswordAuthenticationToken("user@example.com", null);

        mockMvc = MockMvcBuilders.standaloneSetup(suiviController).build();
        mockMvc.perform(get("/api/suivi").principal(auth))
                .andExpect(status().isOk());
    }
}
