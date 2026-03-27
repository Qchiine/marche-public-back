package com.emsi.marches_backend.dto.offre;

import java.time.LocalDate;

// Les valeurs par défaut (page=0, size=20, sort="date_desc") sont gérées
// au niveau du contrôleur via @RequestParam(defaultValue = "...")
public record OffreFilter(
        String motCle,
        String secteur,
        LocalDate dateMin,
        int page,
        int size,
        String sort
) {}
