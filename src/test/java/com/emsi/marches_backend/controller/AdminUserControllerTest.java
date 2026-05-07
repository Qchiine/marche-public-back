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
    void updateRole_shouldReturnUpdatedUser() {
        UtilisateurDocument user = new UtilisateurDocument();
        user.setId("u1");
        user.setEmail("user@example.com");
        user.setRole(RoleEnum.USER);

        when(utilisateurRepository.findById("u1")).thenReturn(Optional.of(user));
        when(utilisateurRepository.save(user)).thenReturn(user);

        var result = adminUserController.updateRole("u1", new AdminRoleUpdateRequest(RoleEnum.ADMIN));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().role()).isEqualTo(RoleEnum.ADMIN);
    }
}
