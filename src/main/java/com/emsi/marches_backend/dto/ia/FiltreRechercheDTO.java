package com.emsi.marches_backend.dto.ia;
import com.emsi.marches_backend.model.OffreMarche.TypeMarche;
import com.emsi.marches_backend.model.OffreMarche.StatutOffre;
import lombok.Data;
import java.util.List;

@Data
public class FiltreRechercheDTO {

    private List<String> motsCles;

    private TypeMarche typeMarche = TypeMarche.ALL;

    private StatutOffre statut = StatutOffre.EN_COURS;

    private String maitreDOuvrage;

    private String region;

    private Double budgetMin;

    private Double budgetMax;

    /**
     * Décrivez votre entreprise ici pour que Gemini filtre les offres.
     * Ex: "Entreprise IT spécialisée en développement web depuis 5 ans"
     */
    private String profilEntreprise;

    private int maxResults = 20;
}