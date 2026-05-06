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

    private static final String FIELD_DATE_PUBLICATION = "datePublication";
    private static final String FIELD_DATE_CLOTURE = "dateCloture";

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
            andCriteria.add(Criteria.where(FIELD_DATE_PUBLICATION).gte(dateMin));
        }

        LocalDate dateLimiteMax = filter.dateLimiteMax();
        if (dateLimiteMax != null) {
            andCriteria.add(Criteria.where(FIELD_DATE_CLOTURE).lte(dateLimiteMax));
        }

        if (hasText(filter.statut())) {
            LocalDate today = LocalDate.now();
            String normalizedStatut = filter.statut().trim().toUpperCase(Locale.ROOT);
            if ("OUVERT".equals(normalizedStatut)) {
                andCriteria.add(new Criteria().orOperator(
                        Criteria.where(FIELD_DATE_CLOTURE).gte(today),
                        Criteria.where(FIELD_DATE_CLOTURE).is(null)
                ));
            } else if ("CLOS".equals(normalizedStatut)) {
                andCriteria.add(Criteria.where(FIELD_DATE_CLOTURE).lt(today));
            }
        }

        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        }
    }

    private void applySort(Query query, String requestedSort, boolean hasMotCle) {
        String normalizedSort = requestedSort == null ? "" : requestedSort.trim().toLowerCase();

        switch (normalizedSort) {
            case "date_asc" -> query.with(Sort.by(Sort.Direction.ASC, FIELD_DATE_PUBLICATION));
            case "pertinence" -> {
                if (hasMotCle && query instanceof TextQuery textQuery) {
                    textQuery.sortByScore();
                } else {
                    query.with(Sort.by(Sort.Direction.DESC, FIELD_DATE_PUBLICATION));
                }
            }
            case "date_desc", "" -> query.with(Sort.by(Sort.Direction.DESC, FIELD_DATE_PUBLICATION));
            default -> query.with(Sort.by(Sort.Direction.DESC, FIELD_DATE_PUBLICATION));
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
