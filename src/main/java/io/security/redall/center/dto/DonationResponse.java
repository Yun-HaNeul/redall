package io.security.redall.center.dto;

import io.security.redall.center.domain.Donation;
import io.security.redall.center.domain.DonationType;

import java.time.LocalDate;

/**
 * 헌혈 기록 응답
 */
public record DonationResponse(
        Long id,
        LocalDate donationDate,
        DonationType donationType,
        String donationTypeName,        // "전혈" 등 표시용
        Long bloodCenterId,
        String placeName,               //  장소 표시명
        String memo
) {
    public static DonationResponse from(Donation d){
        return new DonationResponse(
                d.getId(),
                d.getDonationDate(),
                d.getDonationType(),
                d.getDonationType().getDisplayName(),
                d.getBloodCenter() != null ? d.getBloodCenter().getId() : null,
                d.getPlaceDisplayName(),
                d.getMemo()
        );
    }
}
