package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.OffreMarcheDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OffreRepository extends MongoRepository<OffreMarcheDocument, String>, OffreRepositoryCustom {
}
