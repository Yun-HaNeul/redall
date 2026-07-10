package io.security.redall.center.domain;

import io.security.redall.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "blood_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BloodStatistic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_year", nullable = false)
    private Integer year;

    @Column(nullable = false, length = 50)
    private String region;

    @Column
    private Long donationCount;     // 헌혈실적(건)

    @Column
    private Double donationRate;    // 헌혈률(%)

    @Column
    private Long population;        // 인구수

    @Builder
    public BloodStatistic(Integer year, String region, Long donationCount, Double donationRate, Long population){
        this.year = year;
        this.region = region;
        this.donationCount = donationCount;
        this.donationRate = donationRate;
        this.population = population;
    }

}
