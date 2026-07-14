package io.security.redall.center.repository;

import io.security.redall.center.domain.Donation;
import io.security.redall.center.domain.DonationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    /** 내 헌혈 기록 (최신순)  */
    List<Donation> findByUserIdOrderByDonationDateDesc(Long userId);

    /** 특정 종류의 마지막 헌혈 (가능일 계산용)  */
    Optional<Donation> findFirstByUserIdAndDonationTypeOrderByDonationDateDesc(Long userId, DonationType donationType);

    /** 가장 최근 헌혈 (종류 무관)  */
    Optional<Donation> findFirstByUserIdOrderByDonationDateDesc(Long userId);

    /** 특정 기간의 특정 종류 횟수 (연간 한도 체크용) */
    long countByUserIdAndDonationTypeAndDonationDateBetween(
            Long userId, DonationType donationType,
            LocalDate start, LocalDate end
    );

    /** 총 헌혈 횟수  */
    long countByUserId(Long userId);
}
