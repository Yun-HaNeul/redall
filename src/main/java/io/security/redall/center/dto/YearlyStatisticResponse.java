package io.security.redall.center.dto;

import io.security.redall.center.domain.BloodStatistic;

/**
 * 연도별 추이 (전국 합계 3개년)
 */
public record YearlyStatisticResponse(
        int year,
        long donationCount,
        double donationRate
) {
    public static YearlyStatisticResponse from(BloodStatistic s){
        return new YearlyStatisticResponse(
                s.getYear(),
                s.getDonationCount() != null ? s.getDonationCount() : 0,
                s.getDonationRate() != null ? s.getDonationRate() : 0.0
        );
    }
}
