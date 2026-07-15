package io.security.redall.center.service;

import io.security.redall.center.domain.Donation;
import io.security.redall.center.domain.DonationType;
import io.security.redall.center.repository.DonationRepository;
import io.security.redall.domain.User;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 헌혈 인사이트 서비스
 * 사용자의 헌혈 기록을 분석용 프롬프트로 구성해 Cluade에게 조언을 받는다.
 */
@Service
@RequiredArgsConstructor
public class DonationInsightService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final ClaudeApiClient claudeApiClient;

    @Transactional(readOnly = true)
    public String generateInsight(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Donation> donations = donationRepository.findByUserIdOrderByDonationDateDesc(user.getId());

        // 기록이 없으면 AI 호출 없이 안내
        if (donations.isEmpty()) {
            return "아직 헌혈 기록이 없어요. 첫 헌혈 기록 시 맞춤 분석을 받을 수 있어요🙌";
        }

        String prompt = buildPrompt(user.getName(), donations);

        return claudeApiClient.generate(prompt);
    }

    private String buildPrompt(String userName, List<Donation> donations) {
        // 종류별 횟수 집계
        Map<DonationType, Long> typeCounts = donations.stream()
                .collect(Collectors.groupingBy(
                        Donation::getDonationType, Collectors.counting()));

        // 마지막 헌혈 경과일
        LocalDate lastDate = donations.get(donations.size() - 1).getDonationDate();
        long daysSinceLast = ChronoUnit.DAYS.between(lastDate, LocalDate.now());

        // 종류별 횟수 문자열
        String typeSummary = typeCounts.entrySet().stream()
                .map(e -> String.format("%s %d회", e.getKey().getDisplayName(), e.getValue()))
                .collect(Collectors.joining(", "));

        // 프롬프트 조립
        return """
                 당신은 헌혈을 장려하는 따뜻한 헬스케어 어시스턴트입니다.
                 아래 사용자의 헌혈 기록을 보고, 한국어로 개인화된 인사이트를 작성해주세요.
                 
                 포함할 내용:
                  1. 헌혈 습관에 대한 긍정적 분석
                  2. 지금까지의 기여에 대한 격려 (헌혈 1회는 최대 3명을 도울 수 있습니다)
                  3. 다음 헌혈에 대한 구체적 추천 (종류별 간격: 전혈 8주, 성분헌혈 2주)
    
                  주의사항:
                  - 따뜻하고 격려하는 톤으로
                  - 의료 조언이 아닌 일반적인 헌혈 정보 수준으로
                  - 3~4문장으로 간결하게
                  - 이모지를 적절히 사용
    
                  [사용자 헌혈 기록]
                  이름: %s
                  총 헌혈 횟수: %d회
                  종류별: %s
                  마지막 헌혈: %d일 전
                """.formatted(userName, donations.size(), typeSummary, daysSinceLast);
    }
}
