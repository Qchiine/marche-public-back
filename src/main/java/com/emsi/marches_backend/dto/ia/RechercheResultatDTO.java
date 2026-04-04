package com.emsi.marches_backend.dto.ia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechercheResultatDTO {
    
    private boolean succes;
    private int totalScrape;
    private int totalFiltres;
    private List<AnalyseResultDTO> offres;
    private String message;
}
