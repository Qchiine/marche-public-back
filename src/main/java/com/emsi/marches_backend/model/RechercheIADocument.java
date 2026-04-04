package com.emsi.marches_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "recherches_ia")
public class RechercheIADocument {

    @Id
    private String id;

    private String userId;

    private String motsCles;
    private String typeMarche;
    private String statut;
    private String region;
    private String maitreDOuvrage;
    private Double budgetMin;
    private Double budgetMax;

    private String profilEntreprise;

    private Integer totalResultats;

    @CreatedDate
    private LocalDateTime dateRecherche;
}
