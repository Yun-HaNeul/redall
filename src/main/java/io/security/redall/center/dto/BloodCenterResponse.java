package io.security.redall.center.dto;

import io.security.redall.center.domain.BloodCenter;

public record BloodCenterResponse(
        Long id,
        String bloodBankName,
        String name,
        String code,
        String address,
        String tel,
        Double lat,
        Double lon
) {
    public static BloodCenterResponse from(BloodCenter b){
        return new BloodCenterResponse(
                b.getId(), b.getBloodBankName(), b.getName(), b.getCode(),
                b.getAddress(), b.getTel(), b.getLat(), b.getLon()
        );
    }
}
