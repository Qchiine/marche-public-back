package com.emsi.marches_backend.service;

import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.UtilisateurDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Async
    public void sendNewOfferNotification(UtilisateurDocument utilisateur, OffreMarcheDocument offre, NotificationDocument notification) {
        try {
            log.info("Envoi email notification pour {}", utilisateur.getEmail());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(utilisateur.getEmail());
            message.setSubject("""
                Nouvelle offre en %s - VeilleMarché.ma
                """.formatted(offre.getSecteur()).trim());

            String contenu = """
                    Bonjour %s %s,
                    
                    Une nouvelle offre correspondant à vos critères a été publiée:
                    
                    Titre: %s
                    Organisme: %s
                    Secteur: %s
                    Localisation: %s
                    Référence: %s
                    Clôture: %s
                    
                    Consultez l'offre complète sur notre plateforme.
                    
                    Cordialement,
                    VeilleMarché.ma
                    """.formatted(
                    utilisateur.getPrenom(),
                    utilisateur.getNom(),
                    offre.getIntitule(),
                    offre.getOrganisme(),
                    offre.getSecteur(),
                    offre.getLocalisation(),
                    offre.getReference(),
                    offre.getDateCloture() != null ?
                        offre.getDateCloture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"
            );

            message.setText(contenu);
            message.setFrom("noreply@veillemarche.ma");

            mailSender.send(message);
            log.info("Email envoyé avec succès à {}", utilisateur.getEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
        }
    }

    @Async
    public void sendLoginNotification(UtilisateurDocument utilisateur) {
        try {
            log.info("Envoi email de connexion pour {}", utilisateur.getEmail());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(utilisateur.getEmail());
            message.setSubject("Nouvelle connexion - VeilleMarche.ma");
            message.setText("""
                    Bonjour %s %s,

                    Une connexion a ete effectuee sur votre compte VeilleMarche.ma le %s.

                    Si vous etes a l'origine de cette action, vous pouvez ignorer cet email.
                    Sinon, changez votre mot de passe rapidement depuis la page de connexion.

                    Cordialement,
                    VeilleMarche.ma
                    """.formatted(
                    utilisateur.getPrenom(),
                    utilisateur.getNom(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ));
            message.setFrom("noreply@veillemarche.ma");

            mailSender.send(message);
            log.info("Email de connexion envoye avec succes a {}", utilisateur.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de connexion: {}", e.getMessage());
        }
    }
}
