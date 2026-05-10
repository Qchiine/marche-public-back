package com.emsi.marches_backend.config;

import com.emsi.marches_backend.model.ProfilRecherche;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.NotificationFrequence;
import com.emsi.marches_backend.model.enums.RoleEnum;
import com.emsi.marches_backend.model.enums.StatutCompteEnum;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        activateUsersWithSavedProfile();
        completeUsersWithoutProfile();

        List<UtilisateurDocument> admins = utilisateurRepository.findByRole(RoleEnum.ADMIN);

        if (!admins.isEmpty()) {
            keepOnlyOneAdmin(admins);
            return;
        }

        UtilisateurDocument admin = utilisateurRepository.findByEmail("admin@marchepublic.ma")
                .orElseGet(UtilisateurDocument::new);
        admin.setNom("Admin");
        admin.setPrenom("Super");
        admin.setEmail("admin@marchepublic.ma");
        if (admin.getMotDePasseHash() == null || admin.getMotDePasseHash().isBlank()) {
            admin.setMotDePasseHash(passwordEncoder.encode("admin123"));
        }
        admin.setRole(RoleEnum.ADMIN);
        admin.setStatut(StatutCompteEnum.ACTIF);

        utilisateurRepository.save(admin);
    }

    private void completeUsersWithoutProfile() {
        List<UtilisateurDocument> incompleteUsers = utilisateurRepository.findAll().stream()
                .filter(user -> user.getRole() == RoleEnum.USER)
                .filter(user -> user.getProfil() == null)
                .peek(user -> {
                    user.setProfil(defaultProfile());
                    user.setStatut(StatutCompteEnum.ACTIF);
                })
                .toList();

        if (!incompleteUsers.isEmpty()) {
            utilisateurRepository.saveAll(incompleteUsers);
        }
    }

    private ProfilRecherche defaultProfile() {
        return new ProfilRecherche(
                List.of("BTP", "Informatique", "Fournitures", "Services"),
                List.of("BTP", "Informatique", "Fournitures", "Services"),
                "",
                List.of(),
                null,
                NotificationFrequence.IMMEDIATE
        );
    }

    private void activateUsersWithSavedProfile() {
        List<UtilisateurDocument> usersWithProfile = utilisateurRepository.findAll().stream()
                .filter(user -> user.getProfil() != null)
                .filter(user -> user.getStatut() == StatutCompteEnum.PROFIL_INCOMPLET
                        || user.getStatut() == StatutCompteEnum.EN_ATTENTE_ACTIVATION)
                .peek(user -> user.setStatut(StatutCompteEnum.ACTIF))
                .toList();

        if (!usersWithProfile.isEmpty()) {
            utilisateurRepository.saveAll(usersWithProfile);
        }
    }

    private void keepOnlyOneAdmin(List<UtilisateurDocument> admins) {
        UtilisateurDocument primaryAdmin = admins.stream()
                .filter(admin -> "admin@marchepublic.ma".equalsIgnoreCase(admin.getEmail()))
                .findFirst()
                .orElseGet(() -> admins.stream()
                        .min(Comparator.comparing(
                                UtilisateurDocument::getDateInscription,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                        .orElse(admins.get(0)));

        List<UtilisateurDocument> duplicateAdmins = admins.stream()
                .filter(admin -> !admin.getId().equals(primaryAdmin.getId()))
                .peek(admin -> admin.setRole(RoleEnum.USER))
                .toList();

        if (!duplicateAdmins.isEmpty()) {
            utilisateurRepository.saveAll(duplicateAdmins);
        }
    }
}
