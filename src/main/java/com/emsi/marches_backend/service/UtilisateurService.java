package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.user.ProfilRequest;
import com.emsi.marches_backend.dto.user.ProfilResponse;
import com.emsi.marches_backend.model.ProfilRecherche;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public ProfilResponse validerQuestionnaire(String email, ProfilRequest request) {
        UtilisateurDocument utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        ProfilRecherche profil = new ProfilRecherche(
                request.motsClesInteret(),
                request.secteursChoisis(),
                request.localisation(),
                request.frequenceNotification()
        );

        utilisateur.setProfil(profil);
        utilisateur.setStatut(StatutCompteEnum.ACTIF);
        UtilisateurDocument saved = utilisateurRepository.save(utilisateur);

        return toProfilResponse(saved.getProfil());
    }

    public ProfilResponse getProfil(String email) {
        UtilisateurDocument utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (utilisateur.getProfil() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil non configure");
        }

        return toProfilResponse(utilisateur.getProfil());
    }

    private ProfilResponse toProfilResponse(ProfilRecherche profil) {
        return new ProfilResponse(
                profil.getMotsCles(),
                profil.getSecteurs(),
                profil.getLocalisation(),
                profil.getFrequenceNotification()
        );
    }
}
