package io.security.redall.center.controller;

import io.security.redall.center.dto.BloodTypeStatResponse;
import io.security.redall.center.dto.RegionStatisticResponse;
import io.security.redall.center.dto.StatisticSummaryResponse;
import io.security.redall.center.dto.YearlyStatisticResponse;
import io.security.redall.center.service.BloodStatisticService;
import io.security.redall.center.service.BloodTypeInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 헌혈 통계 API (공개).
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class BloodStatisticController {
    private final BloodStatisticService bloodStatisticService;
    private final BloodTypeInsightService insightService;

    @GetMapping("/summary")
    public ResponseEntity<StatisticSummaryResponse> getSummary(){
        return ResponseEntity.ok(bloodStatisticService.getSummary());
    }

    /**
     * 연도별 추이
     * GET /api/statistics/yearly
     * @return
     */
    @GetMapping("/yearly")
    public ResponseEntity<List<YearlyStatisticResponse>> getYearly(){
        return ResponseEntity.ok(bloodStatisticService.getYearlyTrend());
    }

    /**
     * 지역별 순위
     * GET /api/statistics/regions
     * @param year
     * @return
     */
    @GetMapping("/regions")
    public ResponseEntity<List<RegionStatisticResponse>> getRegions(@RequestParam int year){
        return ResponseEntity.ok(bloodStatisticService.getRegionRanking(year));
    }

    /**
     * 지역 상세
     * GET /api/statistics/region/{region}
     * @param region
     * @return
     */
    @GetMapping("/region/{region}")
    public ResponseEntity<List<RegionStatisticResponse>> getRegionDetail(@PathVariable String region){
        return ResponseEntity.ok(bloodStatisticService.getRegionDetail(region));
    }

    /**
     * 혈액형별 분포
     * GET /api/statistics/blood-types
     * @return
     */
    @GetMapping("/blood-types")
    public ResponseEntity<List<BloodTypeStatResponse>> getBloodTypeDistribution(){
        return ResponseEntity.ok(insightService.getDistribution());
    }


}
