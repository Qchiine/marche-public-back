package com.emsi.marches_backend.dto.offre;

import java.util.List;

public record OffreFilterOptionsResponse(
        List<String> categories,
        List<String> localisations
) {
}
