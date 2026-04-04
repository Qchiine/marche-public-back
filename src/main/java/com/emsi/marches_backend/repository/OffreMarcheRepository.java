package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.model.OffreMarche;
import com.emsi.marches_backend.model.OffreMarche.TypeMarche;
import com.emsi.marches_backend.model.OffreMarche.StatutOffre;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffreMarcheRepository extends MongoRepository<OffreMarche, String> {

    Optional<OffreMarche> findByReferencePortail(String referencePortail);

    @Query("{ $and: [ " +
           "{ $or: [ { 'typeMarche': null }, { 'typeMarche': ?0 } ] }, " +
           "{ $or: [ { 'statut': null }, { 'statut': ?1 } ] }, " +
           "{ $or: [ { 'region': null }, { 'region': { $regex: ?2, $options: 'i' } } ] }, " +
           "{ $or: [ { 'objet': null }, { 'objet': { $regex: ?3, $options: 'i' } } ] } " +
           "] }")
    List<OffreMarche> rechercherOffres(
            TypeMarche typeMarche,
            StatutOffre statut,
            String region,
            String motCle
    );

    List<OffreMarche> findByScorePertinenceGreaterThanEqualOrderByScorePertinenceDesc(double scoreMin);
}