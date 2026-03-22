package com.emsi.marches_backend.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "utilisateurs")
public class UtilisateurDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String nom;
    private String prenom;
    private String motDePasseHash;

    private RoleEnum role = RoleEnum.USER;
    private StatutCompteEnum statut = StatutCompteEnum.EN_ATTENTE_ACTIVATION;

    private ProfilRecherche profil;

    private LocalDateTime dateInscription = LocalDateTime.now();
}