package com.emsi.marches_backend.service;

import com.emsi.marches_backend.event.OffreCollectedEvent;
import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.ProfilRecherche;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.NotificationCanal;
import com.emsi.marches_backend.repository.NotificationRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleOffreCollectedEvent(OffreCollectedEvent event) {
        matcherOffresVersUtilisateurs(event.offresCollectees());
    }

    public void matcherOffresVersUtilisateurs(List<OffreMarcheDocument> offresCollectees) {
        if (offresCollectees == null || offresCollectees.isEmpty()) {
            return;
        }

        List<OffreMarcheDocument> offres = offresCollectees.stream()
                .filter(Objects::nonNull)
                .toList();

        if (offres.isEmpty()) {
            return;
        }

        List<UtilisateurDocument> utilisateurs = utilisateurRepository.findAll();
        for (UtilisateurDocument utilisateur : utilisateurs) {
            if (utilisateur == null || utilisateur.getId() == null) {
                continue;
            }

            ProfilRecherche profil = utilisateur.getProfil();
            if (estProfilVide(profil)) {
                continue;
            }

            for (OffreMarcheDocument offre : offres) {
                if (!matchOffreAvecProfil(offre, profil)) {
                    continue;
                }

                String offreId = construireOffreId(offre);
                if (offreId == null) {
                    continue;
                }

                if (notificationRepository.existsByUserIdAndOffreId(utilisateur.getId(), offreId)) {
                    continue;
                }

                NotificationDocument notification = NotificationDocument.builder()
                        .userId(utilisateur.getId())
                        .offreId(offreId)
                        .referenceOffre(offre.getReference())
                        .canal(NotificationCanal.IN_APP)
                        .titre("Nouvelle offre correspondant a votre profil")
                        .message("Offre: " + valeur(offre.getIntitule()) + " (" + valeur(offre.getReference()) + ")")
                        .build();

                notificationRepository.save(notification);
            }
        }
    }

    private boolean matchOffreAvecProfil(OffreMarcheDocument offre, ProfilRecherche profil) {
        String contenu = (valeur(offre.getIntitule()) + " " + valeur(offre.getDescription())).toLowerCase(Locale.ROOT);

        boolean motsClesMatch = profil.getMotsCles() == null || profil.getMotsCles().isEmpty()
                || profil.getMotsCles().stream()
                .filter(this::hasText)
                .map(this::normaliser)
                .anyMatch(contenu::contains);

        boolean secteurMatch = profil.getSecteurs() == null || profil.getSecteurs().isEmpty()
                || profil.getSecteurs().stream()
                .filter(this::hasText)
                .map(this::normaliser)
                .anyMatch(secteur -> secteur.equals(normaliser(offre.getSecteur())));

        boolean localisationMatch = !hasText(profil.getLocalisation())
                || normaliser(offre.getLocalisation()).contains(normaliser(profil.getLocalisation()));

        return motsClesMatch && secteurMatch && localisationMatch;
    }

    private boolean estProfilVide(ProfilRecherche profil) {
        if (profil == null) {
            return true;
        }

        boolean motsClesVides = profil.getMotsCles() == null
                || profil.getMotsCles().stream().noneMatch(this::hasText);
        boolean secteursVides = profil.getSecteurs() == null
                || profil.getSecteurs().stream().noneMatch(this::hasText);
        boolean localisationVide = !hasText(profil.getLocalisation());

        return motsClesVides && secteursVides && localisationVide;
    }

    private String construireOffreId(OffreMarcheDocument offre) {
        if (hasText(offre.getId())) {
            return offre.getId().trim();
        }
        if (hasText(offre.getReference())) {
            return offre.getReference().trim();
        }
        return null;
    }

    private String valeur(String value) {
        return value == null ? "" : value.trim();
    }

    private String normaliser(String value) {
        return valeur(value).toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
