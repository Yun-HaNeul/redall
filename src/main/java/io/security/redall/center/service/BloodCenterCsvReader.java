package io.security.redall.center.service;

import io.security.redall.center.dto.BloodCenterPublicData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ETL - Extract 단계
 * resource의 CSV를 읽어 헌혈의집 원본 데이터 목록으로 만든다.
 * CSV 형식 : 혈액원, 헌혈의집, 구분, 주소지, 전화번호 (UTF-8, 주소에 쉼표 x)
 */
@Component
public class BloodCenterCsvReader {
    private static final String CSV_PATH = "data/blood_centers.csv";

    public List<BloodCenterPublicData> read(){
        List<BloodCenterPublicData> result = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(CSV_PATH);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), java.nio.charset.Charset.forName("EUC-KR")))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null){
                if (isFirstLine){
                    isFirstLine = false;
                    continue;
                }
                if (line.isBlank()) continue;

                String[] parts = line.split(",");
                if (parts.length != 5) continue;

                BloodCenterPublicData data = new BloodCenterPublicData(
                        parts[0].trim(), parts[1].trim(), parts[2].trim(),
                        parts[3].trim(), parts[4].trim()
                );

                if (data.isValid()){
                    result.add(data);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 읽기 오류 : " + e.getMessage(), e);
        }
        return result;
    }

}
