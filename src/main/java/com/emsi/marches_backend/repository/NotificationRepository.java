package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.NotificationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends MongoRepository<NotificationDocument, String> {
    boolean existsByUserIdAndOffreId(String userId, String offreId);

    List<NotificationDocument> findByUserIdOrderByDateCreationDesc(String userId);

    long countByUserId(String userId);

    long countByUserIdAndDateCreationBetween(String userId, LocalDateTime start, LocalDateTime end);
}
