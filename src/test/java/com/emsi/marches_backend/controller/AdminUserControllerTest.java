package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.dto.admin.AdminRoleUpdateRequest;
import com.emsi.marches_backend.dto.admin.AdminUserResponse;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @InjectMocks
    private AdminUserController adminUserController;

    @Test
    void listUsers_shouldReturnUsers() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail("user@example.com");
        user.setNom("Doe");
        user.setPrenom("John");
        user.setRole(RoleEnum.USER);
        user.setStatut(StatutCompteEnum.ACTIF);
        user.setDateInscription(LocalDateTime.now());

        when(utilisateurRepository.findAll()).thenReturn(List.of(user));

        var result = adminUserController.listUsers();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    void updateRole_shouldPromoteUserWhenNoAdminExists() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail("user@example.com");
        user.setRole(RoleEnum.USER);

        when(utilisateurRepository.findById("u1")).thenReturn(Optional.of(user));
        when(utilisateurRepository.countByRole(RoleEnum.ADMIN)).thenReturn(0L);
        when(utilisateurRepository.save(user)).thenReturn(user);

        var result = adminUserController.updateRole("u1", new AdminRoleUpdateRequest(RoleEnum.ADMIN));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().role()).isEqualTo(RoleEnum.ADMIN);
    }

    @Test
    void updateRole_shouldRejectSecondAdmin() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail("user@example.com");
        user.setRole(RoleEnum.USER);

        when(utilisateurRepository.findById("u1")).thenReturn(Optional.of(user));
        when(utilisateurRepository.countByRole(RoleEnum.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> adminUserController.updateRole("u1", new AdminRoleUpdateRequest(RoleEnum.ADMIN)))
                .hasMessageContaining("Un administrateur existe deja");
    }

    @Test
    void updateRole_shouldRejectRemovingLastAdmin() {
        UtilisateurDocument admin = new UtilisateurDocument();
        admin.setId("admin1");
        admin.setEmail("admin@marchepublic.ma");
        admin.setRole(RoleEnum.ADMIN);

        when(utilisateurRepository.findById("admin1")).thenReturn(Optional.of(admin));
        when(utilisateurRepository.countByRole(RoleEnum.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> adminUserController.updateRole("admin1", new AdminRoleUpdateRequest(RoleEnum.USER)))
                .hasMessageContaining("Impossible de retirer le dernier administrateur");
    }
}
