package io.security.redall.center.service;

import io.security.redall.center.domain.BloodCenter;
import io.security.redall.center.dto.BloodCenterNearbyResponse;
import io.security.redall.center.dto.BloodCenterResponse;
import io.security.redall.center.repository.BloodCenterRepository;
import io.security.redall.center.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 헌혈의집 주변 검색 서비스
 * 하버사인 공식으로 거리를 계산해 정렬/필터
 */

@Service
@RequiredArgsConstructor
public class BloodCenterSearchService {
    private final BloodCenterRepository bloodCenterRepository;

    /**
     * 가까운 순 N개
     * @param lat. lon 내 위치
     * @param limit 개수
     * @return
     */
    @Transactional(readOnly = true)
    public List<BloodCenterNearbyResponse> findNearest(double lat, double lon, int limit){
        return bloodCenterRepository
                .findByLatIsNotNullAndLonIsNotNull().stream()
                .map(c -> toNearby(c, lat, lon))
                .sorted(Comparator.comparingDouble(BloodCenterNearbyResponse::distanceKm))
                .limit(limit)
                .toList();
    }


    /**
     * 반경 이내 전부 (가까운 순 정렬)
     * @param radiusKm 반경
     * @return
     */
    @Transactional(readOnly = true)
    public List<BloodCenterNearbyResponse> findWithinRadius(double lat, double lon, double radiusKm ){
        return bloodCenterRepository
                .findByLatIsNotNullAndLonIsNotNull().stream()
                .map(c -> toNearby(c, lat, lon))
                .filter(r -> r.distanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(BloodCenterNearbyResponse::distanceKm))
                .toList();
    }

    private BloodCenterNearbyResponse toNearby(BloodCenter c, double lat, double lon){
        double dist = DistanceCalculator.distanceKm(
                lat, lon, c.getLat(), c.getLon()
        );
        return BloodCenterNearbyResponse.from(c, dist);
    }
}
