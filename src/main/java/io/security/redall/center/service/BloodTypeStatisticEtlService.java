package io.security.redall.center.service;

import io.security.redall.center.domain.BloodTypeStatistic;
import io.security.redall.center.repository.BloodTypeStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 혈액형별 헌혈 통계 ETL.
 * KOSIS 호출 -> 필터 (혈액원 합계 + 개별 혈액형) -> 연도+혈액형으로 묶기 -> 저장
 */
@Service
@RequiredArgsConstructor
public class BloodTypeStatisticEtlService {

    private record BloodTypeInfo(String bloodType, String rhType) {
    }

    private static final Map<String, BloodTypeInfo> BLOOD_TYPE_CODES = Map.of(
            "A0101", new BloodTypeInfo("O", "POSITIVE"),
            "A0102", new BloodTypeInfo("A", "POSITIVE"),
            "A0103", new BloodTypeInfo("B", "POSITIVE"),
            "A0104", new BloodTypeInfo("AB", "POSITIVE"),
            "A0201", new BloodTypeInfo("O", "NEGATIVE"),
            "A0202", new BloodTypeInfo("A", "NEGATIVE"),
            "A0203", new BloodTypeInfo("B", "NEGATIVE"),
            "A0204", new BloodTypeInfo("AB", "NEGATIVE")
    );


    private final KosisApiClient kosisApiClient;
    private final BloodTypeStatisticRepository repository;

    @Transactional
    public int runEtl() {
        List<Map<String, Object>> rawData = kosisApiClient.fetchBloodType();

        // (연도+혈액형)으로 묶기
        Map<String, BloodTypeRow> grouped = new HashMap<>();

        for (Map<String, Object> item : rawData) {
            String c2 = (String) item.get("C2");        //  혈액형 코드
            String c3 = (String) item.get("C3");        //  혈액원 코드

            //  필터: 혈액원 합계(C01) + 개별 혈액형(A00 합계 제외)
            if (!"C01".equals(c3) || !BLOOD_TYPE_CODES.containsKey(c2)) {
                continue;
            }

            // 코드로 혈액형 이름 결정
            BloodTypeInfo info = BLOOD_TYPE_CODES.get(c2);

            String yearStr = (String) item.get("PRD_DE");
            String itmId = (String) item.get("ITM_ID");
            String dt = (String) item.get("DT");
            if (yearStr == null || dt == null) continue;

            String key = yearStr + "|" + info.bloodType() + "|" + info.rhType();
            BloodTypeRow row = grouped.computeIfAbsent(key,
                    k -> new BloodTypeRow(Integer.parseInt(yearStr),
                            info.bloodType(), info.rhType()));

            if ("T001".equals(itmId)) {
                row.count = parseLong(dt);
            } else if ("T002".equals(itmId)) {
                row.ratio = parseDouble(dt);
            }
        }

        //  저장 (중복이면 건너뜀)
        int saved = 0;
        for (BloodTypeRow row : grouped.values()) {
            if (row.bloodType == null || row.count == null) {
                continue;
            }
            if (repository.findByYearAndBloodTypeAndRhType(row.year, row.bloodType, row.rhType).isPresent()) {
                continue;
            }
            repository.save(BloodTypeStatistic.builder()
                    .year(row.year)
                    .bloodType(row.bloodType)
                    .rhType(row.rhType)
                    .donationCount(row.count)
                    .ratio(row.ratio)
                    .build());

            saved++;
        }

        return saved;

    }

    private Long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }


    private static class BloodTypeRow {
        Integer year;
        String bloodType;
        String rhType;
        Long count;
        Double ratio;

        BloodTypeRow(Integer year, String bloodType, String rhType) {
            this.year = year;
            this.bloodType = bloodType;
            this.rhType = rhType;
        }
    }
}
