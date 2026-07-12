package io.security.redall.center.service;

import io.security.redall.center.domain.BloodStatistic;
import io.security.redall.center.dto.StatisticSummaryResponse;
import io.security.redall.center.repository.BloodStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 헌혈 통계 조회 서비스.
 */
@Service
@RequiredArgsConstructor
public class BloodStatisticService {
    private static final String TOTAL_REGION = "합계";

    private final BloodStatisticRepository statisticRepository;

    /**
     * 전국 요약 (물방울 + 요약카드)
     * 합계 지역의 최신 연도 데이터 + 작년 대비 증감.
     * @return
     */
    @Transactional(readOnly = true)
    public StatisticSummaryResponse getSummary(){
        // 합계 지역 데이터를 연도 오름차순으로
        List<BloodStatistic> totals =
                statisticRepository.findByRegionOrderByYearAsc(TOTAL_REGION);

        if (totals.isEmpty()){
            throw new IllegalArgumentException("통계 데이터가 없습니다.");
        }

        // 마지막이 최신 연도
        BloodStatistic latest = totals.get(totals.size() - 1);

        // 전년 데이터 (있을시)
        Long previousCount = null;
        Double changePercent = null;

        if(totals.size() >= 2){
            BloodStatistic previous = totals.get(totals.size() - 2);
            previousCount = previous.getDonationCount();

            // 작년 대비 증감률 = (올해 - 작년) / 작년 * 100
            if (previousCount != null && previousCount > 0
                    && latest.getDonationCount() != null) {
                double change = (latest.getDonationCount() - previousCount)
                        * 100.0 / previousCount;
                changePercent = Math.round(change * 10) / 10.0;  // 소수 1자리
            }
        }

        return new StatisticSummaryResponse(
                latest.getYear(),
                latest.getDonationRate() != null ? latest.getDonationRate() : 0.0,
                latest.getDonationCount() != null ? latest.getDonationCount() : 0,
                latest.getPopulation() != null ? latest.getPopulation() : 0,
                previousCount,
                changePercent
        );
    }

}
