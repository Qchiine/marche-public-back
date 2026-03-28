package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.offre.OffreFilter;
import com.emsi.marches_backend.dto.offre.OffreResponse;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.repository.OffreRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class OffreService {

    private final OffreRepository offreRepository;

    public OffreService(OffreRepository offreRepository) {
        this.offreRepository = offreRepository;
    }

    public Page<OffreResponse> searchByFilters(OffreFilter filter) {
        return offreRepository.searchByFilters(filter).map(this::toResponse);
    }

    private OffreResponse toResponse(OffreMarcheDocument offre) {
        return new OffreResponse(
                offre.getId(),
                offre.getReference(),
                offre.getIntitule(),
                offre.getDescription(),
                offre.getOrganisme(),
                offre.getSecteur(),
                offre.getLocalisation(),
                offre.getEmailContact(),
                offre.getUrlOfficielle(),
                offre.getDatePublication(),
                offre.getDateCloture(),
                offre.getDateCollecte()
        );
    }
}
