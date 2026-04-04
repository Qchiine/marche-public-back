package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.RechercheIADocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RechercheIADocumentRepository extends MongoRepository<RechercheIADocument, String> {

    List<RechercheIADocument> findTop10ByUserIdOrderByDateRechercheDesc(String userId);
}
