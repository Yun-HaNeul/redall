package io.security.redall.center.controller;

import io.security.redall.center.dto.StatisticSummaryResponse;
import io.security.redall.center.service.BloodStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 헌혈 통계 API (공개).
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class BloodStatisticController {
    private final BloodStatisticService bloodStatisticService;

    @GetMapping("/summary")
    public ResponseEntity<StatisticSummaryResponse> getSummary(){
        return ResponseEntity.ok(bloodStatisticService.getSummary());
    }
}
