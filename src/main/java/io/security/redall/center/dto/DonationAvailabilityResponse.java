package io.security.redall.center.dto;

import io.security.redall.center.domain.DonationType;

import java.time.LocalDate;

public record DonationAvailabilityResponse(
        DonationType type,
        String typeName,                //  "전혈" 등
        boolean canDonate,              //  지금 가능한지 여부
        LocalDate availableDate,        //  가능해지는 날
        long dDay,                      //  며칠 남았는지 (가능하면 0)
        long countThisYear,             //  올해 이 종류의 횟수
        int yearlyLimit,                //  연간 한도
        boolean limitReached,           //  연간 한도 초과
        String reason                   //  불가 사유 (가능시 null)

) { }
