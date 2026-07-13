package io.security.redall.center.service;

import io.security.redall.center.domain.BloodStatistic;
import io.security.redall.center.dto.RegionStatisticResponse;
import io.security.redall.center.dto.StatisticSummaryResponse;
import io.security.redall.center.dto.YearlyStatisticResponse;
import io.security.redall.center.repository.BloodStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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

    /**
     * 연도별 추이 (전국 합계)
     * @return
     */
    @Transactional(readOnly = true)
    public List<YearlyStatisticResponse> getYearlyTrend(){
        return statisticRepository.findByRegionOrderByYearAsc(TOTAL_REGION).stream()
                .map(YearlyStatisticResponse::from)
                .toList();
    }

    /**
     * 지역별 순위 (특정 연도, 헌혈률 높은 순)
     * 합계 제외
     * @param year
     * @return
     */
    @Transactional(readOnly = true)
    public List<RegionStatisticResponse> getRegionRanking(int year){
        return statisticRepository.findByYear(year).stream()
                .filter(s -> !TOTAL_REGION.equals(s.getRegion()))   // 합계 제외
                .map(RegionStatisticResponse::from)
                .sorted(Comparator.comparingDouble(
                        RegionStatisticResponse::donationRate).reversed()   //   높은 순 정렬
                ).toList();
    }

    /**
     * 지역 상세 (특정 지역의 전체 연도)
     * @param region
     * @return
     */
    @Transactional(readOnly = true)
    public List<RegionStatisticResponse> getRegionDetail(String region){
        return statisticRepository.findByRegionOrderByYearAsc(region).stream()
                .map(RegionStatisticResponse::from)
                .toList();
    }

}
