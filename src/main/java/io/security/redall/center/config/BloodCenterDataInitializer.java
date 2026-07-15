package io.security.redall.center.config;

import io.security.redall.center.repository.BloodCenterRepository;
import io.security.redall.center.service.BloodCenterEtlService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 시 헌혈의집 데이터를 자동으로 적재
 * 단, 이미 데이터가 있으면 건너뜀 (중복 방지)
 *
 * CommandLineRunner : 스프링 앱이 완전히 뜬 직후 run()이 자동 실행
 */
@Component
@RequiredArgsConstructor
public class BloodCenterDataInitializer implements CommandLineRunner {
    private static final Logger log =
            LoggerFactory.getLogger(BloodCenterDataInitializer.class);
    private final BloodCenterRepository bloodCenterRepository;
    private final BloodCenterEtlService etlService;

    @Override
    public void run(String... args) throws Exception {
        // 이미 데이터가 있을 시 건너뜀
        long count = bloodCenterRepository.count();
        if(count > 0){
            log.info("헌혈의집 데이터가 이미 존재합니다 ({}건). ETL을 건너뜁니다.", count);
            return;
        }

        log.info("=== ETL start ===");
        BloodCenterEtlService.EtlResult result = etlService.runEtl();
        log.info("=== ETL 완료: 추출 {}건, 저장 {}건, 좌표실패 {}건, 중복 {}건 ===",
                result.extracted(), result.loaded(),
                result.geocodeFail(), result.duplicate());
    }
}
