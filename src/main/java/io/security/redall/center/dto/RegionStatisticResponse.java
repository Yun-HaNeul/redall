package io.security.redall.center.dto;

import io.security.redall.center.domain.BloodStatistic;

/**
 * 지역별 통계 (순위 차트용)
 */
public record RegionStatisticResponse(
        String region,
        int year,
        long donationCount,
        double donationRate,
        long population
) {
    public static RegionStatisticResponse from(BloodStatistic s){
        return new RegionStatisticResponse(
                s.getRegion(),
                s.getYear(),
                s.getDonationCount() != null ? s.getDonationCount() : 0,
                s.getDonationRate() != null ? s.getDonationRate() : 0.0,
                s.getPopulation() != null ? s.getPopulation() : 0
        );
    }
}
