package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.offre.OffreFilter;
import com.emsi.marches_backend.dto.offre.OffreResponse;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.repository.OffreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffreServiceTest {

    @Mock
    private OffreRepository offreRepository;

    @InjectMocks
    private OffreService offreService;

    @Test
    void searchByFilters_shouldMapDocumentToResponse() {
        OffreFilter filter = new OffreFilter("informatique", "IT", LocalDate.of(2026, 3, 1), 0, 20, "pertinence");

        OffreMarcheDocument offre = new OffreMarcheDocument();
        offre.setId("offre-1");
        offre.setReference("AO-1");
        offre.setIntitule("Fourniture informatique");
        offre.setDescription("Serveurs et reseau");
        offre.setOrganisme("EMSI");
        offre.setSecteur("IT");
        offre.setLocalisation("Casablanca");
        offre.setEmailContact("contact@example.com");
        offre.setUrlOfficielle("https://example.com/offres/1");
        offre.setDatePublication(LocalDate.of(2026, 3, 10));
        offre.setDateCloture(LocalDate.of(2026, 3, 30));
        offre.setDateCollecte(LocalDateTime.of(2026, 3, 10, 9, 0));

        Page<OffreMarcheDocument> page = new PageImpl<>(List.of(offre), PageRequest.of(0, 20), 1);
        when(offreRepository.searchByFilters(filter)).thenReturn(page);

        Page<OffreResponse> result = offreService.searchByFilters(filter);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo("offre-1");
        assertThat(result.getContent().get(0).intitule()).isEqualTo("Fourniture informatique");
        verify(offreRepository).searchByFilters(filter);
    }
}
