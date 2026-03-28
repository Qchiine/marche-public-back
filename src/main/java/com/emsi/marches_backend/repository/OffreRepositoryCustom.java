package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.dto.offre.OffreFilter;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import org.springframework.data.domain.Page;

public interface OffreRepositoryCustom {
    Page<OffreMarcheDocument> searchByFilters(OffreFilter filter);
}
