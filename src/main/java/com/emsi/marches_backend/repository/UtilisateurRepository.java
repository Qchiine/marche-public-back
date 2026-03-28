package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.UtilisateurDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UtilisateurRepository extends MongoRepository<UtilisateurDocument, String> {
    Optional<UtilisateurDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}
