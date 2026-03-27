package com.emsi.marches_backend.model;

import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "utilisateurs")
@Schema(description = "Utilisateur inscrit sur la plateforme VeilleMarché.ma")
public class UtilisateurDocument {

    @Id
    @Schema(description = "Identifiant MongoDB", example = "64a1f2e3b5c9d80012ab3456")
    private String id;

    @Indexed(unique = true)
    @Schema(description = "Email unique de l'utilisateur", example = "ali.hassan@example.com")
    private String email;

    @Schema(description = "Nom de famille", example = "Hassan")
    private String nom;

    @Schema(description = "Prénom", example = "Ali")
    private String prenom;

    @Schema(hidden = true)
    private String motDePasseHash;

    @Schema(description = "Rôle de l'utilisateur", example = "USER")
    private RoleEnum role = RoleEnum.USER;

    @Schema(description = "Statut du compte", example = "EN_ATTENTE_ACTIVATION")
    private StatutCompteEnum statut = StatutCompteEnum.EN_ATTENTE_ACTIVATION;

    @Schema(description = "Profil de recherche personnalisé")
    private ProfilRecherche profil;

    @CreatedDate
    @Schema(description = "Date d'inscription (auto)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dateInscription;
}
