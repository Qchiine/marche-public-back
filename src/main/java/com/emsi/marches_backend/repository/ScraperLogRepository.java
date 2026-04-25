package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.ScraperLogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScraperLogRepository extends MongoRepository<ScraperLogDocument, String> {

    Page<ScraperLogDocument> findAllByOrderByDateDebutDesc(Pageable pageable);

    List<ScraperLogDocument> findByStatut(String statut);

    List<ScraperLogDocument> findByDateDebutBetween(LocalDateTime start, LocalDateTime end);
}
