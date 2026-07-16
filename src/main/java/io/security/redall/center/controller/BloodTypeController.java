package io.security.redall.center.controller;

import io.security.redall.center.dto.BloodTypeInsightResponse;
import io.security.redall.center.service.BloodTypeInsightService;
import io.security.redall.dto.BloodTypeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 혈액형 등록 & 인사이트 API (인증 필요)
 */
@RestController
@RequestMapping("/api/blood-type")
@RequiredArgsConstructor
public class BloodTypeController {

    private final BloodTypeInsightService insightService;

    /**
     * 내 혈액형 등록/수정
     * PUT /api/blood-type
     * @param authentication
     * @param request
     * @return
     */
    @PutMapping
    public ResponseEntity<Map<String, String>> updateBloodType(
            Authentication authentication,
            @Valid @RequestBody BloodTypeRequest request){
        insightService.updateBloodType(
                authentication.getName(), request.getBloodType(), request.getRhType());

        return ResponseEntity.ok(Map.of("message", "혈액형이 등록되었습니다."));
    }

    @GetMapping("/insight")
    public ResponseEntity<BloodTypeInsightResponse> getMyInsight(
            Authentication authentication){
        BloodTypeInsightResponse insight = insightService.getMyInsight(authentication.getName());

        // 미등록이면 204 No Content
        if (insight == null){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(insight);
    }
}
