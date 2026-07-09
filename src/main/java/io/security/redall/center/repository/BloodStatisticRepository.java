package io.security.redall.center.repository;

import io.security.redall.center.domain.BloodStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BloodStatisticRepository extends JpaRepository<BloodStatistic, Long> {
    Optional<BloodStatistic> findByYearAndRegion(Integer year, String region);  // 중복 확인

    List<BloodStatistic> findByYear(Integer year);                              // 지역별 순위용

    List<BloodStatistic> findByRegionOrderByYearAsc(String region);             // 연도별 추이 + 지역 상세용

}
