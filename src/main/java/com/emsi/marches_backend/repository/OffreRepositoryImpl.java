package com.emsi.marches_backend.repository;

import com.emsi.marches_backend.dto.offre.OffreFilter;
import com.emsi.marches_backend.model.OffreMarcheDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Repository
public class OffreRepositoryImpl implements OffreRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public OffreRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<OffreMarcheDocument> searchByFilters(OffreFilter filter) {
        boolean hasMotCle = hasText(filter.motCle());

        Query query = buildBaseQuery(filter, hasMotCle);
        applyFilters(query, filter);

        long total = mongoTemplate.count(query, OffreMarcheDocument.class);

        int safePage = Math.max(filter.page(), 0);
        int safeSize = filter.size() <= 0 ? 20 : Math.min(filter.size(), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        query.with(pageable);
        applySort(query, filter.sort(), hasMotCle);

        List<OffreMarcheDocument> content = mongoTemplate.find(query, OffreMarcheDocument.class);
        return new PageImpl<>(content, pageable, total);
    }

    private Query buildBaseQuery(OffreFilter filter, boolean hasMotCle) {
        if (hasMotCle) {
            TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matching(filter.motCle().trim());
            return TextQuery.queryText(textCriteria);
        }
        return new Query();
    }

    private void applyFilters(Query query, OffreFilter filter) {
        List<Criteria> andCriteria = new ArrayList<>();

        if (hasText(filter.secteur())) {
            andCriteria.add(Criteria.where("secteur").regex(buildContainsRegex(filter.secteur()), "i"));
        }

        if (hasText(filter.localisation())) {
            andCriteria.add(Criteria.where("localisation").regex(buildContainsRegex(filter.localisation()), "i"));
        }

        LocalDate dateMin = filter.dateMin();
        if (dateMin != null) {
            andCriteria.add(Criteria.where("datePublication").gte(dateMin));
        }

        LocalDate dateLimiteMax = filter.dateLimiteMax();
        if (dateLimiteMax != null) {
            andCriteria.add(Criteria.where("dateCloture").lte(dateLimiteMax));
        }

        if (hasText(filter.statut())) {
            LocalDate today = LocalDate.now();
            String normalizedStatut = filter.statut().trim().toUpperCase(Locale.ROOT);
            switch (normalizedStatut) {
                case "OUVERT" -> andCriteria.add(new Criteria().orOperator(
                        Criteria.where("dateCloture").gte(today),
                        Criteria.where("dateCloture").is(null)
                ));
                case "CLOS" -> andCriteria.add(Criteria.where("dateCloture").lt(today));
                default -> {
                }
            }
        }

        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        }
    }

    private void applySort(Query query, String requestedSort, boolean hasMotCle) {
        String normalizedSort = requestedSort == null ? "" : requestedSort.trim().toLowerCase();

        switch (normalizedSort) {
            case "date_asc" -> query.with(Sort.by(Sort.Direction.ASC, "datePublication"));
            case "pertinence" -> {
                if (hasMotCle && query instanceof TextQuery textQuery) {
                    textQuery.sortByScore();
                } else {
                    query.with(Sort.by(Sort.Direction.DESC, "datePublication"));
                }
            }
            case "date_desc", "" -> query.with(Sort.by(Sort.Direction.DESC, "datePublication"));
            default -> query.with(Sort.by(Sort.Direction.DESC, "datePublication"));
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String buildContainsRegex(String value) {
        return ".*" + java.util.regex.Pattern.quote(value.trim()) + ".*";
    }

    @Override
    public List<String> findDistinctSecteurs() {
        return mongoTemplate.query(OffreMarcheDocument.class)
                .distinct("secteur")
                .as(String.class)
                .all()
                .stream()
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Override
    public List<String> findDistinctLocalisations() {
        return mongoTemplate.query(OffreMarcheDocument.class)
                .distinct("localisation")
                .as(String.class)
                .all()
                .stream()
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .sorted(Comparator.comparing(String::toLowerCase))
                .toList();
    }
}
