package com.emsi.marches_backend.dto.dashboard;

public record DashboardStatsResponse(
        long totalOffres,
        long offresCollecteesAujourdHui,
        long variationCollecteVsHier,
        long correspondancesActives,
        long nouvellesCorrespondancesAujourdHui,
        long marchesEnSuivi,
        long marchesEnAnalyse,
        long cloturesDans48h
) {
}
