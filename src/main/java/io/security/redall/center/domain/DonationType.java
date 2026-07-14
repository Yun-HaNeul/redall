package io.security.redall.center.domain;

import lombok.Getter;

/**
 * 헌혈 종류와 그 규칙
 * 재헌혈 간격과 연간 최대 횟수를 Enum에 닮아 도메인 규칙을 캡슐화
 *
 * 출처 ) 대한적십자가 혈액관리본부
 * - 전혈: 8주(56일) 간격, 연 5회
 * - 성분헌혈(혈장/혈소판): 2주(14일) 간격, 연 24회
 */
@Getter
public enum DonationType {

    WHOLE_BLOOD("전혈", 56, 5),
    PLASMA("혈장", 14, 24),
    PLATELET("혈소판", 14, 24),
    PLATELET_PLASMA("혈소판혈장", 14, 24);

    private final String displayName;       //  화면 표시용 이름
    private final int intervalDays;         //  재헌혈 간격 (일)
    private final int yearlyLimit;          //  연간 최대 횟수

    DonationType(String displayName, int intervalDays, int yearlyLimit) {
        this.displayName = displayName;
        this.intervalDays = intervalDays;
        this.yearlyLimit = yearlyLimit;
    }
}
