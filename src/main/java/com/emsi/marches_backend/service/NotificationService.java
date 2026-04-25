package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.ProfilRecherche;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.repository.NotificationRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Crée des notifications pour tous les utilisateurs intéressés par une offre
     */
    public void createNotificationsForOffer(OffreMarcheDocument offre) {
        log.info("Création des notifications pour l'offre: {}", offre.getReference());

        // Récupérer tous les utilisateurs
        List<UtilisateurDocument> utilisateurs = utilisateurRepository.findAll();

        for (UtilisateurDocument utilisateur : utilisateurs) {
            if (matchesUserProfile(offre, utilisateur.getProfil())) {
                // Vérifier si notification existe déjà
                if (!notificationRepository.existsByUserIdAndOffreId(utilisateur.getId(), offre.getId())) {
                    NotificationDocument notification = NotificationDocument.builder()
                            .userId(utilisateur.getId())
                            .offreId(offre.getId())
                            .referenceOffre(offre.getReference())
                            .titre(offre.getIntitule())
                            .message("Nouvelle offre en " + offre.getSecteur() + " à " + offre.getLocalisation())
                            .lue(false)
                            .build();

                    NotificationDocument savedNotif = notificationRepository.save(notification);
                    log.info("Notification créée pour utilisateur {} - Offre {}", utilisateur.getId(), offre.getReference());

                    // Envoyer email asynchrone
                    emailNotificationService.sendNewOfferNotification(utilisateur, offre, savedNotif);
                }
            }
        }
    }

    /**
     * Vérifie si une offre match le profil de recherche d'un utilisateur
     */
    private boolean matchesUserProfile(OffreMarcheDocument offre, ProfilRecherche profil) {
        if (profil == null) {
            return false;
        }

        boolean matchSecteur = profil.getSecteurs() == null || profil.getSecteurs().isEmpty() ||
                profil.getSecteurs().stream()
                        .anyMatch(s -> offre.getSecteur() != null && offre.getSecteur().toLowerCase().contains(s.toLowerCase()));

        boolean matchLocalisation = profil.getLocalisation() == null || profil.getLocalisation().isEmpty() ||
                (offre.getLocalisation() != null && offre.getLocalisation().toLowerCase().contains(profil.getLocalisation().toLowerCase()));

        boolean matchMotsCles = profil.getMotsCles() == null || profil.getMotsCles().isEmpty() ||
                profil.getMotsCles().stream()
                        .anyMatch(mc -> (offre.getIntitule() != null && offre.getIntitule().toLowerCase().contains(mc.toLowerCase())) ||
                                (offre.getDescription() != null && offre.getDescription().toLowerCase().contains(mc.toLowerCase())));

        return matchSecteur && matchLocalisation && matchMotsCles;
    }

    public List<NotificationDocument> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByDateCreationDesc(userId);
    }

    public NotificationDocument markAsRead(String notificationId) {
        NotificationDocument notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setLue(true);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public long countUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByDateCreationDesc(userId)
                .stream()
                .filter(n -> !n.isLue())
                .count();
    }
}
