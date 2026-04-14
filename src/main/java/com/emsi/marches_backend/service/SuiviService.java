package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.suivi.SuiviRequest;
import com.emsi.marches_backend.dto.suivi.SuiviResponse;
import com.emsi.marches_backend.model.SuiviDocument;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.SuiviStatut;
import com.emsi.marches_backend.repository.SuiviRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class SuiviService {

    private final SuiviRepository suiviRepository;
    private final UtilisateurRepository utilisateurRepository;

    public SuiviService(SuiviRepository suiviRepository, UtilisateurRepository utilisateurRepository) {
        this.suiviRepository = suiviRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public SuiviResponse create(String email, String offreId, SuiviRequest request) {
        String userId = resolveUserId(email);
        if (suiviRepository.findByUserIdAndOffreId(userId, offreId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suivi deja existant pour cette offre");
        }

        SuiviDocument suivi = SuiviDocument.builder()
                .userId(userId)
                .offreId(offreId)
                .statut(request != null && request.statut() != null ? request.statut() : SuiviStatut.INTERESSE)
                .note(request != null ? normalizeNote(request.note()) : null)
                .build();

        try {
            return toResponse(suiviRepository.save(suivi));
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suivi deja existant pour cette offre");
        }
    }

    public List<SuiviResponse> list(String email, SuiviStatut statut) {
        String userId = resolveUserId(email);
        List<SuiviDocument> suivis = (statut == null)
                ? suiviRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                : suiviRepository.findByUserIdAndStatutOrderByUpdatedAtDesc(userId, statut);
        return suivis.stream().map(this::toResponse).toList();
    }

    public SuiviResponse update(String email, String suiviId, SuiviRequest request) {
        String userId = resolveUserId(email);
        SuiviDocument suivi = findOwnedByUser(suiviId, userId);

        if (request != null && request.statut() != null) {
            suivi.setStatut(request.statut());
        }
        if (request != null && request.note() != null) {
            suivi.setNote(normalizeNote(request.note()));
        }

        return toResponse(suiviRepository.save(suivi));
    }

    public void delete(String email, String suiviId) {
        String userId = resolveUserId(email);
        SuiviDocument suivi = findOwnedByUser(suiviId, userId);
        suiviRepository.delete(suivi);
    }

    private SuiviDocument findOwnedByUser(String suiviId, String userId) {
        SuiviDocument suivi = suiviRepository.findById(suiviId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suivi introuvable"));
        if (!userId.equals(suivi.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Suivi introuvable");
        }
        return suivi;
    }

    private String resolveUserId(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        UtilisateurDocument utilisateur = utilisateurRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
        return utilisateur.getId();
    }

    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }
        String normalized = note.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private SuiviResponse toResponse(SuiviDocument suivi) {
        return new SuiviResponse(
                suivi.getId(),
                suivi.getUserId(),
                suivi.getOffreId(),
                suivi.getStatut(),
                suivi.getNote(),
                suivi.getCreatedAt(),
                suivi.getUpdatedAt()
        );
    }
}
