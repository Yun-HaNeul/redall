package io.security.redall.center.dto;

public record StatisticSummaryResponse(
        int year,                       // 최신 연도 (예: 2025)
        double donationRate,            // 헌혈률 % (물방울용)
        long donationCount,             // 헌혈실적 (건)
        long population,                // 인구수
        Long previousCount,             // 전년 헌혈실적 (없으면 null)
        Double changePercent            // 작년 대비 증감률 % (없으면 null)
) {}
