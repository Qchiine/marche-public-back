package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.PasswordResetCodeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetCodeRepository extends MongoRepository<PasswordResetCodeDocument, String> {

    Optional<PasswordResetCodeDocument> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);
}
