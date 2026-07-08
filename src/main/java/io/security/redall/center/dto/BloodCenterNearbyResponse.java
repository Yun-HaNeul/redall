package io.security.redall.center.dto;

import io.security.redall.center.domain.BloodCenter;

/**
 * 주변 검색 응답
 * 기존 정보 + 내 위치로부터의 거리(km)
 */
public record BloodCenterNearbyResponse(
        Long id,
        String bloodBankName,
        String name,
        String code,
        String address,
        String tel,
        Double lat,
        Double lon,
        double distanceKm   // 내 위치로부터 거리
) {
    public static BloodCenterNearbyResponse from(BloodCenter b, double distanceKm){
        return new BloodCenterNearbyResponse(
                b.getId(), b.getBloodBankName(), b.getName(), b.getCode(),
                b.getAddress(), b.getTel(), b.getLat(), b.getLon(),
                Math.round(distanceKm * 100) / 100.0    //소수점 2자리로 반올림
        );
    }
}
