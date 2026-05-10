package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.model.ScraperLogDocument;
import com.emsi.marches_backend.repository.NotificationRepository;
import com.emsi.marches_backend.repository.OffreRepository;
import com.emsi.marches_backend.repository.ScraperLogRepository;
import com.emsi.marches_backend.repository.SuiviRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/platform")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPlatformController {

    private final OffreRepository offreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;
    private final SuiviRepository suiviRepository;
    private final ScraperLogRepository scraperLogRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.data.mongodb.database:marchesdb}")
    private String mongoDatabase;

    @GetMapping("/markets")
    public ResponseEntity<AdminMarketsResponse> markets() {
        List<OffreMarcheDocument> offres = offreRepository.findAll();
        LocalDate today = LocalDate.now();

        long open = offres.stream()
                .filter(offre -> offre.getDateCloture() == null || !offre.getDateCloture().isBefore(today))
                .count();
        long closed = offres.size() - open;
        long withoutOfficialUrl = offres.stream()
                .filter(offre -> isBlank(offre.getUrlOfficielle()))
                .count();

        List<AdminMarketItem> recent = offres.stream()
                .sorted(Comparator.comparing(
                        OffreMarcheDocument::getDatePublication,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(20)
                .map(this::toMarketItem)
                .toList();

        return ResponseEntity.ok(new AdminMarketsResponse(offres.size(), open, closed, withoutOfficialUrl, recent));
    }

    @DeleteMapping("/markets/{id}")
    public ResponseEntity<Void> deleteMarket(@PathVariable String id) {
        if (!offreRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Marche introuvable");
        }
        offreRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsResponse> statistics() {
        List<OffreMarcheDocument> offres = offreRepository.findAll();
        LocalDate today = LocalDate.now();

        long open = offres.stream()
                .filter(offre -> offre.getDateCloture() == null || !offre.getDateCloture().isBefore(today))
                .count();
        long closingSoon = offres.stream()
                .filter(offre -> offre.getDateCloture() != null)
                .filter(offre -> !offre.getDateCloture().isBefore(today))
                .filter(offre -> !offre.getDateCloture().isAfter(today.plusDays(7)))
                .count();

        return ResponseEntity.ok(new AdminStatisticsResponse(
                offres.size(),
                open,
                closingSoon,
                utilisateurRepository.count(),
                suiviRepository.count(),
                notificationRepository.count(),
                topValues(offres, OffreMarcheDocument::getSecteur),
                topValues(offres, OffreMarcheDocument::getLocalisation),
                dailyCollections(offres)
        ));
    }

    @GetMapping("/emails")
    public ResponseEntity<AdminEmailResponse> emails() {
        return ResponseEntity.ok(new AdminEmailResponse(
                !isBlank(mailUsername),
                mailHost,
                mailPort,
                maskEmail(mailUsername),
                notificationRepository.count()
        ));
    }

    @PostMapping("/emails/test")
    public ResponseEntity<AdminActionResponse> sendTestEmail(@RequestBody AdminTestEmailRequest request) {
        String recipient = request != null && !isBlank(request.to()) ? request.to() : mailUsername;
        if (isBlank(recipient)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun email destinataire configure");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject("Test email - VeilleMarche.ma");
            message.setText("""
                    Bonjour,

                    Ceci est un email de test envoye depuis l'administration VeilleMarche.ma.

                    Si vous recevez ce message, la configuration SMTP fonctionne correctement.
                    """);
            message.setFrom(mailUsername);
            mailSender.send(message);
            return ResponseEntity.ok(new AdminActionResponse("Email de test envoye a " + recipient));
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Echec de l'envoi email: " + exception.getMessage());
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AdminLogItem>> logs() {
        List<AdminLogItem> logs = scraperLogRepository
                .findAllByOrderByDateDebutDesc(PageRequest.of(0, 50))
                .getContent()
                .stream()
                .map(this::toLogItem)
                .toList();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/settings")
    public ResponseEntity<AdminSettingsResponse> settings() {
        return ResponseEntity.ok(new AdminSettingsResponse(
                "VeilleMarche.ma",
                serverPort,
                mongoDatabase,
                mailHost,
                mailPort,
                utilisateurRepository.count(),
                offreRepository.count(),
                scraperLogRepository.count()
        ));
    }

    private AdminMarketItem toMarketItem(OffreMarcheDocument offre) {
        String status = "OUVERT";
        if (offre.getDateCloture() != null && offre.getDateCloture().isBefore(LocalDate.now())) {
            status = "CLOS";
        }

        return new AdminMarketItem(
                offre.getId(),
                offre.getReference(),
                offre.getIntitule(),
                offre.getOrganisme(),
                offre.getSecteur(),
                offre.getLocalisation(),
                offre.getDatePublication(),
                offre.getDateCloture(),
                status,
                offre.getUrlOfficielle()
        );
    }

    private AdminLogItem toLogItem(ScraperLogDocument log) {
        return new AdminLogItem(
                log.getId(),
                "SCRAPING",
                log.getStatut(),
                log.getMessage(),
                log.getErreur(),
                log.getDateDebut(),
                log.getDateFin()
        );
    }

    private List<AdminMetricItem> topValues(List<OffreMarcheDocument> offres, ValueExtractor extractor) {
        return offres.stream()
                .map(extractor::extract)
                .filter(value -> !isBlank(value))
                .collect(Collectors.groupingBy(value -> value, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(entry -> new AdminMetricItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<AdminMetricItem> dailyCollections(List<OffreMarcheDocument> offres) {
        return offres.stream()
                .map(OffreMarcheDocument::getDateCollecte)
                .filter(Objects::nonNull)
                .map(date -> date.toLocalDate().toString())
                .collect(Collectors.groupingBy(value -> value, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByKey().reversed())
                .limit(7)
                .map(entry -> new AdminMetricItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static String maskEmail(String email) {
        if (isBlank(email) || !email.contains("@")) {
            return "";
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String visible = name.length() <= 2 ? name : name.substring(0, 2) + "***";
        return visible + "@" + parts[1];
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private interface ValueExtractor {
        String extract(OffreMarcheDocument offre);
    }

    public record AdminMarketsResponse(
            long total,
            long ouverts,
            long clos,
            long sansLienOfficiel,
            List<AdminMarketItem> marches
    ) {
    }

    public record AdminMarketItem(
            String id,
            String reference,
            String intitule,
            String organisme,
            String secteur,
            String localisation,
            LocalDate datePublication,
            LocalDate dateCloture,
            String statut,
            String urlOfficielle
    ) {
    }

    public record AdminStatisticsResponse(
            long totalMarches,
            long marchesOuverts,
            long echeancesSeptJours,
            long totalUtilisateurs,
            long totalSuivis,
            long totalNotifications,
            List<AdminMetricItem> parSecteur,
            List<AdminMetricItem> parRegion,
            List<AdminMetricItem> collectesParJour
    ) {
    }

    public record AdminMetricItem(String label, long value) {
    }

    public record AdminEmailResponse(
            boolean configure,
            String host,
            int port,
            String username,
            long notificationsCreees
    ) {
    }

    public record AdminTestEmailRequest(String to) {
    }

    public record AdminActionResponse(String message) {
    }

    public record AdminLogItem(
            String id,
            String source,
            String statut,
            String message,
            String erreur,
            LocalDateTime dateDebut,
            LocalDateTime dateFin
    ) {
    }

    public record AdminSettingsResponse(
            String application,
            String serverPort,
            String mongoDatabase,
            String mailHost,
            int mailPort,
            long totalUtilisateurs,
            long totalMarches,
            long totalLogs
    ) {
    }
}
