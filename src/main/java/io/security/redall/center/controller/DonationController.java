package io.security.redall.center.controller;

import io.security.redall.center.dto.DonationAvailabilityResponse;
import io.security.redall.center.dto.DonationRequest;
import io.security.redall.center.dto.DonationResponse;
import io.security.redall.center.service.DonationAvailabilityService;
import io.security.redall.center.service.DonationInsightService;
import io.security.redall.center.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 헌혈 기록 API (인증 필요, 본인 데이터만)
 */
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;
    private final DonationAvailabilityService availabilityService;
    private final DonationInsightService insightService;

    @GetMapping("/insight")
    public ResponseEntity<Map<String, String>> getInsight(
            Authentication authentication) {
        String insight = insightService.generateInsight(authentication.getName());
        return ResponseEntity.ok(Map.of("insight", insight));
    }

    /**
     * 헌혈 등록
     * POST /api/donations
     * @param authentication
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<DonationResponse> create(
            Authentication authentication,
            @Valid @RequestBody DonationRequest request){
        return ResponseEntity.ok(
                donationService.create(authentication.getName(), request));
    }

    /**
     * 헌혈 가능일 계산
     * GET /api/donations/availability
     * @param authentication
     * @return
     */
    @GetMapping("/availability")
    public ResponseEntity<List<DonationAvailabilityResponse>> getAvailability(
            Authentication authentication
    ){
        return ResponseEntity.ok(
                availabilityService.getAvailability(authentication.getName()));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            Authentication authentication
    ){
        return ResponseEntity.ok(
                donationService.getSummary(authentication.getName()));
    }

    /**
     * 내 기록 목록
     * GET /api/donations
     * @param authentication
     * @return
     */
    @GetMapping
    public ResponseEntity<List<DonationResponse>> getMyDonations(
            Authentication authentication){
        return ResponseEntity.ok(
                donationService.getMyDonations(authentication.getName()));
    }

    /**
     * 수정
     * PUT /api/donations/{id}
     * @param authentication
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<DonationResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DonationRequest request){
        return ResponseEntity.ok(
                donationService.update(authentication.getName(), id, request));
    }

    /**
     * 삭제
     * DELETE /api/donations/{id}
     * @param authentication
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            Authentication authentication,
            @PathVariable Long id){
        donationService.delete(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("message",  "삭제되었습니다."));
    }
}
