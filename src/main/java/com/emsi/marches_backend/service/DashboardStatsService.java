package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.dashboard.DashboardStatsResponse;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.SuiviStatut;
import com.emsi.marches_backend.repository.NotificationRepository;
import com.emsi.marches_backend.repository.OffreRepository;
import com.emsi.marches_backend.repository.SuiviRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final OffreRepository offreRepository;
    private final NotificationRepository notificationRepository;
    private final SuiviRepository suiviRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DashboardStatsResponse getStatsForUser(String email) {
        UtilisateurDocument user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utilisateur introuvable"));

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime startTomorrow = tomorrow.atStartOfDay();
        LocalDateTime startYesterday = yesterday.atStartOfDay();

        long totalOffres = offreRepository.count();
        long offresCollecteesAujourdHui = offreRepository.countByDateCollecteBetween(startToday, startTomorrow);
        long offresCollecteesHier = offreRepository.countByDateCollecteBetween(startYesterday, startToday);
        long correspondancesActives = notificationRepository.countByUserId(user.getId());
        long nouvellesCorrespondancesAujourdHui = notificationRepository.countByUserIdAndDateCreationBetween(
                user.getId(),
                startToday,
                startTomorrow
        );
        long marchesEnSuivi = suiviRepository.countByUserId(user.getId());
        long marchesEnAnalyse = suiviRepository.countByUserIdAndStatutIn(
                user.getId(),
                List.of(SuiviStatut.POSTULE, SuiviStatut.RETENU)
        );
        long cloturesDans48h = offreRepository.countByDateClotureBetween(today, today.plusDays(2));

        return new DashboardStatsResponse(
                totalOffres,
                offresCollecteesAujourdHui,
                offresCollecteesAujourdHui - offresCollecteesHier,
                correspondancesActives,
                nouvellesCorrespondancesAujourdHui,
                marchesEnSuivi,
                marchesEnAnalyse,
                cloturesDans48h
        );
    }
}
