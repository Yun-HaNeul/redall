package io.security.redall.center.domain;

import io.security.redall.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 혈액형별 헌혈 통계
 * 연도 + 혈액형 조합당 한 행
 */
@Getter
@Entity
@Table(name = "blood_type_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BloodTypeStatistic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "stat_year", nullable = false)
    private Integer year;

    /**
     * 혈액형 (A형, B형, O형, AB형)
     */
    @Column(nullable = false, length = 10)
    private String bloodType;

    /**
     * RH 타입
     */
    @Column(nullable = false, length = 10)
    private String rhType;

    /**
     * 헌혈 실적 (건)
     */
    @Column
    private Long donationCount;

    /**
     * 구성비 (%)
     */
    @Column
    private Double ratio;

    @Builder
    public BloodTypeStatistic(Integer year, String bloodType, String rhType, Long donationCount, Double ratio) {
        this.year = year;
        this.bloodType = bloodType;
        this.rhType = rhType;
        this.donationCount = donationCount;
        this.ratio = ratio;
    }

}
