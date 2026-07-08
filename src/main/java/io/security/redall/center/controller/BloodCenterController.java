package io.security.redall.center.controller;

import io.security.redall.center.domain.BloodCenter;
import io.security.redall.center.dto.BloodCenterNearbyResponse;
import io.security.redall.center.dto.BloodCenterResponse;
import io.security.redall.center.repository.BloodCenterRepository;
import io.security.redall.center.service.BloodCenterEtlService;
import io.security.redall.center.service.BloodCenterSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blood-centers")
@RequiredArgsConstructor
public class BloodCenterController {

    private final BloodCenterEtlService etlService;
    private final BloodCenterRepository bloodCenterRepository;
    private final BloodCenterSearchService searchService;

    /**
     * ETL 실행
     * POST /api/blood-centers/etl
     * @return
     */
    @PostMapping("/etl")
    public ResponseEntity<BloodCenterEtlService.EtlResult> runEtl(){
        return ResponseEntity.ok(etlService.runEtl());
    }

    /**
     * 좌표 있는 목록 조회 (지도용)
     * GET /api/blood-centers
     * @return
     */
    @GetMapping
    public ResponseEntity<List<BloodCenterResponse>> getBloodCenters(){
        List<BloodCenter> centers =
                bloodCenterRepository.findByLatIsNotNullAndLonIsNotNull();
        return ResponseEntity.ok(centers.stream().map(BloodCenterResponse::from).toList());
    }

    /**
     * 이름 검색
     * GET /api/blood-centers/search?keyword=
     */
    @GetMapping("/search")
    public ResponseEntity<List<BloodCenterResponse>> search(@RequestParam String keyword){
        return ResponseEntity.ok(
                bloodCenterRepository.findByNameContaining(keyword).stream()
                        .map(BloodCenterResponse::from).toList()
        );
    }

    /**
     * 가까운 순 N개
     * GET /api/blood-centers/nearest?
     * @param lat
     * @param lon
     * @param limit
     * @return
     */
    @GetMapping("/nearest")
    public ResponseEntity<List<BloodCenterNearbyResponse>> findNearest(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") int limit){
        return ResponseEntity.ok(searchService.findNearest(lat, lon, limit));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<BloodCenterNearbyResponse>> findNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5.0") double radius){
        return ResponseEntity.ok(searchService.findWithinRadius(lat, lon, radius));
    }


}
