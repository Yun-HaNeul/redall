package io.security.redall.center.service;

import io.security.redall.center.domain.Donation;
import io.security.redall.center.domain.DonationType;
import io.security.redall.center.dto.DonationAvailabilityResponse;
import io.security.redall.center.repository.DonationRepository;
import io.security.redall.domain.User;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 헌혈 가능일 계산 서비스
 *
 * 두 가지 조건을 모두 만족해야 헌혈 가능
 * 1) 간격 조건 : 가장 최근 헌혈일 + (그 종류의 간격)이 지났는가
 * -> 전혈 후에는 8주, 성분헌혈 후에는 2주각 모든 헌혈 불가
 * 2) 횟수 조건: 올해 해당 종류의 연간 한도를 넘지 않았는지
 */
@Service
@RequiredArgsConstructor
public class DonationAvailabilityService {
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;

    /**
     * 모든 헌혈 종류에 대해 가능 여부 계산
     */
    @Transactional(readOnly = true)
    public List<DonationAvailabilityResponse> getAvailability(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Long userId = user.getId();
        LocalDate today = LocalDate.now();

        // 가장 최근 헌혈 (종류 무관) - 간격 조건의 기준
        Optional<Donation> lastDonation =
                donationRepository.findFirstByUserIdOrderByDonationDateDesc(userId);

        // 간격 조건으로 계산한 "다음 가능일"
        LocalDate nextByInterval;
        if (lastDonation.isPresent()){
            Donation last = lastDonation.get();
            // 마지막 헌혈의 종류에 해당하는 간격을 적용
            int intervalDays = last.getDonationType().getIntervalDays();
            nextByInterval = last.getDonationDate().plusDays(intervalDays);
        }else {
            nextByInterval = today;     // 헌혈 기록 없을 시 오늘 가능
        }

        // 각 종류별로 판단
        return Arrays.stream(DonationType.values())
                .map(type -> evaluate(userId, type, today, nextByInterval))
                .toList();

    }

    /**
     * 특정 종류의 가능 여부 판단
     * @param userId
     * @param type
     * @param today
     * @param nextByInterval
     * @return
     */
    private DonationAvailabilityResponse evaluate(Long userId, DonationType type, LocalDate today, LocalDate nextByInterval){
        // 올해 이 종료를 몇 번 했는지 (연간 한도 체크)
        LocalDate yearStart = LocalDate.of(today.getYear() , 1, 1);
        LocalDate yearEnd = LocalDate.of(today.getYear(), 12, 31);
        long countThisYear = donationRepository
                .countByUserIdAndDonationTypeAndDonationDateBetween(
                        userId, type, yearStart, yearEnd);

        boolean limitReached = countThisYear >= type.getYearlyLimit();

        // 간격 조건 충족 여부
        boolean intervalPassed = !today.isBefore(nextByInterval);

        // 남은 일수
        long dDay = intervalPassed ? 0
                : ChronoUnit.DAYS.between(today, nextByInterval);

        // 최종 판단: 간격도 지나고 한도도 안 넘었을 때만 가능
        boolean canDonate = intervalPassed && !limitReached;

        // 불가 사유
        String reason = null;
        if (limitReached){
            reason = String.format("올해 %s 헌혈 한도(%d회)를 모두 사용했습니다.",
                    type.getDisplayName(), type.getYearlyLimit());
        } else if (!intervalPassed) {
            reason = String.format("최근 헌혈 후 %d일 더 기다려야 합니다.", dDay);
        }

        return new DonationAvailabilityResponse(
                type,
                type.getDisplayName(),
                canDonate,
                nextByInterval,
                dDay,
                countThisYear,
                type.getYearlyLimit(),
                limitReached,
                reason
        );
    }
}
