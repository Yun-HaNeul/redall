package io.security.redall.center.dto;

/**
 * 내 혈액형 인사이트 응답
 */
public record BloodTypeInsightResponse(
        String bloodType,               //  A, B, O, AB
        String rhType,                  //  POSITIVE< NEGATIVE
        String displayName,             //  A형 RH+
        double ratio,                   //  이 혈액형의 헌혈 구성비 (%)
        long donationCount,             //  이 혈액형 연간 헌혈 건수
        boolean isRare,                 //  희귀 여부 (RH-)
        String message                  //  안내 문구
) { }
