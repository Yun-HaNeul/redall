package io.security.redall.center.service;

import io.security.redall.center.domain.BloodStatistic;
import io.security.redall.center.repository.BloodStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 헌혈 통계 ETL 서비스
 * Extract(KOSIS API) -> Transform(연도+지역으로 묶기) -> Load(저장).
 */
@Service
@RequiredArgsConstructor
public class BloodStatisticEtlService {

    //  Extract
    private final KosisApiClient kosisApiClient;
    // Load
    private final BloodStatisticRepository statisticRepository;

    @Transactional
    public EtlResult runEtl(){
        //  1. Extract: KOSIS에서 원본 데이터 가져오기
        List<Map<String, Object>> rawData = kosisApiClient.fetch();

        //  2. Transform: (연도+지역) 기준으로 묶기
        //  key = "연도|지역", value = 임시 집계 객체
        Map<String, StatRow> grouped = new HashMap<>();

        for (Map<String, Object> item : rawData) {
            String yearStr  = (String) item.get("PRD_DE");
            String region   = (String) item.get("C1_NM");
            String itmId    = (String) item.get("ITM_ID");
            String dt       = (String) item.get("DT");

            // 값이 없으면 건너뜀
            if (yearStr == null || region == null || itmId == null || dt == null) {
                continue;
            }

            String key = yearStr + "|" + region;
            StatRow row = grouped.computeIfAbsent(key,
                    k -> new StatRow(Integer.parseInt(yearStr), region));

            // 행목 코드로 어느 필드인지 구분해서 채움
            switch (itmId){
                case "T001" -> row.donationCount = parseLong(dt);       //  헌혈실적
                case "T002" -> row.donationRate = parseDouble(dt);      //  헌혈율
                case "T003" -> row.population = parseLong(dt);
            }
        }

        //  3. Load : 묶은 데이터를 DB에 저장 (연도+지역 중복 확인)
        int saved = 0, updated = 0;
        for (StatRow row : grouped.values()) {
            Optional<BloodStatistic> existing = statisticRepository.findByYearAndRegion(row.year, row.region);

            if(existing.isPresent()){
                // 이미 있으면 건너뜀
                updated++;
                continue;
            }

            BloodStatistic stat = BloodStatistic.builder()
                    .year(row.year)
                    .region(row.region)
                    .donationCount(row.donationCount)
                    .donationRate(row.donationRate)
                    .population(row.population)
                    .build();
            statisticRepository.save(stat);
            saved++;
        }

        return new EtlResult(rawData.size(), grouped.size(), saved, updated);
    }

    private Long parseLong(String a){
        try {
            return Long.parseLong(a);
        }catch (Exception e){
            return null;
        }
    }

    private Double parseDouble(String a){
        try {
            return Double.parseDouble(a);
        }catch (Exception e){
            return null;
        }
    }


    // Transform 중간 집계용 임시 객체
    private static class StatRow {
        Integer year;
        String region;
        Long donationCount;
        Double donationRate;
        Long population;

        StatRow(Integer year, String region){
            this.year = year;
            this.region = region;
        }
    }

    public record EtlResult(
            int rawCount,      // 원본 레코드 수
            int groupedCount,  // 묶인 (연도+지역) 수
            int saved,         // 새로 저장
            int skipped        // 이미 있어서 건너뜀
    ) {}
}
