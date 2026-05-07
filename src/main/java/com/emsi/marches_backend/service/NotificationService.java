package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.ProfilRecherche;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.NotificationFrequence;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.NotificationRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailNotificationService emailNotificationService;

    public void createNotificationsForOffer(OffreMarcheDocument offre) {
        log.info("Creation des notifications pour l'offre: {}", offre.getReference());

        List<UtilisateurDocument> utilisateurs = utilisateurRepository.findAll();

        for (UtilisateurDocument utilisateur : utilisateurs) {
            if (utilisateur.getStatut() == StatutCompteEnum.ACTIF) {
                processUserNotification(offre, utilisateur);
            }
        }
    }

    private void processUserNotification(OffreMarcheDocument offre, UtilisateurDocument utilisateur) {
        if (!matchesUserProfile(offre, utilisateur.getProfil())) {
            return;
        }
        if (notificationRepository.existsByUserIdAndOffreId(utilisateur.getId(), offre.getId())) {
            return;
        }

        NotificationDocument notification = NotificationDocument.builder()
                .userId(utilisateur.getId())
                .offreId(offre.getId())
                .referenceOffre(offre.getReference())
                .titre(offre.getIntitule())
                .message("Nouvelle offre en " + offre.getSecteur() + " a " + offre.getLocalisation())
                .lue(false)
                .build();

        NotificationDocument savedNotif = notificationRepository.save(notification);
        log.info("Notification creee pour utilisateur {} - Offre {}", utilisateur.getId(), offre.getReference());

        if (shouldSendEmail(utilisateur)) {
            emailNotificationService.sendNewOfferNotification(utilisateur, offre, savedNotif);
        } else {
            log.info("Email differe pour utilisateur {} selon la frequence {}", utilisateur.getEmail(),
                    utilisateur.getProfil() != null ? utilisateur.getProfil().getFrequenceNotification() : NotificationFrequence.IMMEDIATE);
        }
    }

    private boolean matchesUserProfile(OffreMarcheDocument offre, ProfilRecherche profil) {
        if (profil == null) {
            return false;
        }

        String secteur = normalize(offre.getSecteur());
        String intitule = normalize(offre.getIntitule());
        String description = normalize(offre.getDescription());
        String organisme = normalize(offre.getOrganisme());
        String localisation = normalize(offre.getLocalisation());
        String reference = normalize(offre.getReference());
        String searchableOffer = String.join(" ", secteur, intitule, description, organisme, localisation, reference);

        boolean matchSecteur = profil.getSecteurs() == null || profil.getSecteurs().isEmpty() ||
                profil.getSecteurs().stream().anyMatch(s -> matchesToken(searchableOffer, s));

        boolean matchLocalisation = profil.getLocalisation() == null || profil.getLocalisation().isEmpty() ||
                matchesToken(localisation, profil.getLocalisation());

        boolean matchMotsCles = profil.getMotsCles() == null || profil.getMotsCles().isEmpty() ||
                profil.getMotsCles().stream().anyMatch(mc -> matchesToken(searchableOffer, mc));

        boolean matchOrganismes = profil.getOrganismes() == null || profil.getOrganismes().isEmpty() ||
                profil.getOrganismes().stream().anyMatch(org -> matchesToken(organisme, org));

        boolean matchDateLimite = profil.getDateLimiteMax() == null ||
                (offre.getDateCloture() != null && !offre.getDateCloture().isAfter(profil.getDateLimiteMax()));

        return matchSecteur && matchLocalisation && matchMotsCles && matchOrganismes && matchDateLimite;
    }

    private boolean shouldSendEmail(UtilisateurDocument utilisateur) {
        ProfilRecherche profil = utilisateur.getProfil();
        NotificationFrequence frequence = profil != null && profil.getFrequenceNotification() != null
                ? profil.getFrequenceNotification()
                : NotificationFrequence.IMMEDIATE;

        if (frequence == NotificationFrequence.IMMEDIATE) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = frequence == NotificationFrequence.DAILY
                ? now.toLocalDate().atStartOfDay()
                : now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();

        return notificationRepository.findByUserIdOrderByDateCreationDesc(utilisateur.getId()).stream()
                .filter(existing -> existing.getDateCreation() != null)
                .noneMatch(existing -> !existing.getDateCreation().isBefore(windowStart));
    }

    private boolean matchesToken(String haystack, String rawToken) {
        String token = normalize(rawToken);
        if (token.isBlank()) {
            return false;
        }
        if (haystack.isBlank()) {
            return false;
        }
        if (haystack.contains(token) || token.contains(haystack)) {
            return true;
        }
        for (String part : token.split("[^a-z0-9]+")) {
            if (part.length() >= 3 && haystack.contains(part)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    public List<NotificationDocument> getUserNotifications(String email) {
        String userId = resolveUserId(email);
        return notificationRepository.findByUserIdOrderByDateCreationDesc(userId);
    }

    public NotificationDocument markAsRead(String email, String notificationId) {
        NotificationDocument notification = findOwnedNotification(email, notificationId);
        notification.setLue(true);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(String email, String notificationId) {
        NotificationDocument notification = findOwnedNotification(email, notificationId);
        notificationRepository.delete(notification);
    }

    public long countUnreadNotifications(String email) {
        String userId = resolveUserId(email);
        return notificationRepository.findByUserIdOrderByDateCreationDesc(userId)
                .stream()
                .filter(n -> !n.isLue())
                .count();
    }

    private NotificationDocument findOwnedNotification(String email, String notificationId) {
        String userId = resolveUserId(email);
        NotificationDocument notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));

        if (!userId.equals(notification.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable");
        }

        return notification;
    }

    private String resolveUserId(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        UtilisateurDocument utilisateur = utilisateurRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
        return utilisateur.getId();
    }
}
