package io.security.redall.center.service;

import io.security.redall.center.domain.BloodTypeStatistic;
import io.security.redall.center.dto.BloodTypeInsightResponse;
import io.security.redall.center.dto.BloodTypeStatResponse;
import io.security.redall.center.repository.BloodTypeStatisticRepository;
import io.security.redall.domain.User;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 내 혈액형 인사이트 서비스
 * 사용자 혈액형 + 혈액형별 통계 -> 개인화 분석
 */
@Service
@RequiredArgsConstructor
public class BloodTypeInsightService {

    private final UserRepository userRepository;
    private final BloodTypeStatisticRepository statisticRepository;

    @Transactional
    public void updateBloodType(String username, String bloodType, String rhType){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateBloodType(bloodType, rhType);
    }

    @Transactional(readOnly = true)
    public BloodTypeInsightResponse getMyInsight(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //  혈액형 미등록
        if (user.getBloodType() == null || user.getRhType() == null) {
            return null;
        }

        //  최신 연도의 내 혈액형 통계 찾기
        int latestYear = findLatestYear();
        Optional<BloodTypeStatistic> statOpt = statisticRepository
                .findByYearAndBloodTypeAndRhType(
                        latestYear, user.getBloodType(), user.getRhType());

        if (statOpt.isEmpty()){
            return null;
        }

        BloodTypeStatistic stat = statOpt.get();
        boolean isRare = "NEGATIVE".equals(user.getRhType());

        //  표시명 (A형 RH+)
        String rhSymbol = "POSITIVE".equals(user.getRhType()) ? "+" : "-";
        String displayName = user.getBloodType() + "형 RH" + rhSymbol;

        //  안내 문구
        String message;
        if (isRare){
            message = String.format(
                    "%s는 전체 헌혈의 %.1f%%로 매우 희귀한 혈액형이에요. " +
                    "희귀 혈액형을 위해 헌혈에 동참해주세요🙏 ",
                    displayName, stat.getRatio());
        } else {
            message = String.format(
                    "%s는 전체 헌혈의 %.1f%%를 차지해요. " +
                    "꾸준한 헌혈로 안정적인 혈액 공급에 기여할 수 있어요! 😊",
                    displayName, stat.getRatio());
        }

        return new BloodTypeInsightResponse(
                user.getBloodType(),
                user.getRhType(),
                displayName,
                stat.getRatio(),
                stat.getDonationCount(),
                isRare,
                message
        );
    }

    /**
     * 혈액형별 분포 (최신 연도, 전체)
     * @return
     */
    @Transactional(readOnly = true)
    public List<BloodTypeStatResponse> getDistribution(){
        int latestYear = findLatestYear();
        return statisticRepository.findByYearOrderByDonationCountDesc(latestYear)
                .stream()
                .map(BloodTypeStatResponse::from)
                .toList();
    }

    // 통계에서 최신 연도 찾기
    private int findLatestYear() {
        List<BloodTypeStatistic> all = statisticRepository.findAll();
        return all.stream()
                .map(BloodTypeStatistic::getYear)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("통계 데이터가 없습니다."));
    }
}
