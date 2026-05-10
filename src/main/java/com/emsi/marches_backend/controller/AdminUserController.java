package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.admin.AdminRoleUpdateRequest;
import com.emsi.marches_backend.dto.admin.AdminUserResponse;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UtilisateurRepository utilisateurRepository;

    public AdminUserController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        List<AdminUserResponse> users = utilisateurRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<AdminUserResponse> updateRole(
            @PathVariable String id,
            @Valid @RequestBody AdminRoleUpdateRequest request
    ) {
        UtilisateurDocument user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        validateSingleAdminRule(user, request.role());
        user.setRole(request.role());
        UtilisateurDocument saved = utilisateurRepository.save(user);
        return ResponseEntity.ok(toResponse(saved));
    }

    private void validateSingleAdminRule(UtilisateurDocument user, RoleEnum requestedRole) {
        long adminCount = utilisateurRepository.countByRole(RoleEnum.ADMIN);

        if (requestedRole == RoleEnum.ADMIN && user.getRole() != RoleEnum.ADMIN && adminCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un administrateur existe deja");
        }

        if (requestedRole == RoleEnum.USER && user.getRole() == RoleEnum.ADMIN && adminCount <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de retirer le dernier administrateur");
        }
    }

    private AdminUserResponse toResponse(UtilisateurDocument user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole(),
                resolveStatus(user),
                user.getDateInscription()
        );
    }

    private StatutCompteEnum resolveStatus(UtilisateurDocument user) {
        if (user.getProfil() != null && user.getStatut() == StatutCompteEnum.PROFIL_INCOMPLET) {
            return StatutCompteEnum.ACTIF;
        }
        return user.getStatut();
    }
}
