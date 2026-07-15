package io.security.redall.center.config;

import io.security.redall.center.repository.BloodTypeStatisticRepository;
import io.security.redall.center.service.BloodTypeStatisticEtlService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@RequiredArgsConstructor
public class BloodTypeStatisticInitializer implements CommandLineRunner {
    private static final Logger log =
            LoggerFactory.getLogger(BloodTypeStatisticInitializer.class);

    private final BloodTypeStatisticRepository repository;
    private final BloodTypeStatisticEtlService etlService;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            log.info("혈액형 통계가 이미 존재합니다. ETL 건너뜁니다.");
            return;
        }
        log.info("=== 혈액형 통계 ETL start ===");
        int saved = etlService.runEtl();
        log.info("=== 혈액형 통계 ETL 완료: {}건 저장 ===", saved);
    }
}
