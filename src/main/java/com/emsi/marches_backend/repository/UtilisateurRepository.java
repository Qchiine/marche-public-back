package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.RoleEnum;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends MongoRepository<UtilisateurDocument, String> {
    Optional<UtilisateurDocument> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(RoleEnum role);

    List<UtilisateurDocument> findByRole(RoleEnum role);
}
