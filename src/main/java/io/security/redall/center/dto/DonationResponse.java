package io.security.redall.center.dto;

import io.security.redall.center.domain.Donation;
import io.security.redall.center.domain.DonationType;

import java.time.LocalDate;

/**
 * 헌혈 기록 응답
 * sequence: 전체 헌혈 중 몇 번째인지 (자동 계산)
 * typeSequence: 같은 종류 중 몇 번째인지 (자동 계산)
 */
public record DonationResponse(
        Long id,
        LocalDate donationDate,
        DonationType donationType,
        String donationTypeName,        // "전혈" 등 표시용
        Long bloodCenterId,
        String placeName,               //  장소 표시명
        String memo,
        int sequence,                   //  전체 회차
        int typeSequence                //  종류별 회차
) {
    public static DonationResponse from(Donation d, int sequence, int typeSequence){
        return new DonationResponse(
                d.getId(),
                d.getDonationDate(),
                d.getDonationType(),
                d.getDonationType().getDisplayName(),
                d.getBloodCenter() != null ? d.getBloodCenter().getId() : null,
                d.getPlaceDisplayName(),
                d.getMemo(),
                sequence,
                typeSequence
        );
    }
}
