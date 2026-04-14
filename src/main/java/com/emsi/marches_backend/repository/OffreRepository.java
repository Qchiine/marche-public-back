package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.OffreMarcheDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OffreRepository extends MongoRepository<OffreMarcheDocument, String>, OffreRepositoryCustom {
    Optional<OffreMarcheDocument> findByReference(String reference);
}
