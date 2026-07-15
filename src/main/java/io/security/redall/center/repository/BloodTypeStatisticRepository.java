package io.security.redall.center.repository;

import io.security.redall.center.domain.BloodTypeStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BloodTypeStatisticRepository extends JpaRepository<BloodTypeStatistic, Long> {

    Optional<BloodTypeStatistic> findByYearAndBloodTypeAndRhType(
        Integer year, String bloodType, String rhType);

    List<BloodTypeStatistic> findByYearOrderByDonationCountDesc(Integer year);

    // 최신 연도 찾기용
    List<BloodTypeStatistic> findAllByOrderByYearDesc();
}
