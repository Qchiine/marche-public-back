package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.SuiviDocument;
import com.emsi.marches_backend.model.enums.SuiviStatut;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SuiviRepository extends MongoRepository<SuiviDocument, String> {
    Optional<SuiviDocument> findByUserIdAndOffreId(String userId, String offreId);

    List<SuiviDocument> findByUserIdOrderByUpdatedAtDesc(String userId);

    List<SuiviDocument> findByUserIdAndStatutOrderByUpdatedAtDesc(String userId, SuiviStatut statut);
}
