package com.emsi.marches_backend.dto.offre;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OffreResponse(
        String id,
        String reference,
        String intitule,
        String description,
        String organisme,
        String secteur,
        String localisation,
        String emailContact,
        String urlOfficielle,
        LocalDate datePublication,
        LocalDate dateCloture,
        LocalDateTime dateCollecte
) {}
