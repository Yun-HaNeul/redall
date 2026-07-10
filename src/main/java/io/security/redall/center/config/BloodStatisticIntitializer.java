package io.security.redall.center.config;

import io.security.redall.center.repository.BloodStatisticRepository;
import io.security.redall.center.service.BloodStatisticEtlService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 시 헌혈 통계 데이터 자동 적재 (없을 때만)
 */

@Component
@Order(2)
@RequiredArgsConstructor
public class BloodStatisticIntitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BloodStatisticIntitializer.class);

    private final BloodStatisticRepository statisticRepository;
    private final BloodStatisticEtlService etlService;

    @Override
    public void run(String... args) throws Exception {
        if(statisticRepository.count() > 0){
            log.info("[ETL 생략] 헌혈 통계 데이터 존재");
            return;
        }

        log.info("[ETL STATISTIC START] 헌혈 통계 ETL 시작");
        var result = etlService.runEtl();
        log.info("[ETL STATISTIC SUCCESS] 헌혈 통계 ETL 완료: 원본 {}건 → {}개 지역·연도 → 저장 {}건",
                result.rawCount(), result.groupedCount(), result.saved());

    }
}
