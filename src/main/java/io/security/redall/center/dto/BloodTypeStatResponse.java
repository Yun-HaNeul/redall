package io.security.redall.center.dto;

import io.security.redall.center.domain.BloodTypeStatistic;

public record BloodTypeStatResponse(
        String bloodType,
        String rhType,
        String displayName,
        long donationCount,
        double ratio
) {
    public static BloodTypeStatResponse from(BloodTypeStatistic s){
        String rhSymbol = "POSITIVE".equals(s.getRhType()) ? "+" : "-";
        return new BloodTypeStatResponse(
                s.getBloodType(),
                s.getRhType(),
                s.getBloodType() + "형 RH" + rhSymbol,
                s.getDonationCount(),
                s.getRatio()
        );
    }
}
