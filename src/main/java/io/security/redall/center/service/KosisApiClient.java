package io.security.redall.center.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * ETL - Extract 단계
 * KOSIS 오픈API를 호출해 헌혈 통계 원본 데이터를 가져온다.
 */
@Component
public class KosisApiClient {

    private final RestClient restClient = RestClient.create();

    @Value("${kosis.api.key}")
    private String apiKey;

    @Value("${kosis.api.url}")
    private String apiUrl;

    @Value("${kosis.api.org-id}")
    private String orgId;

    @Value("${kosis.api.tbl-id}")
    private String tblId;

    @Value("${kosis.api.bloodtype-tbl-id}")
    private String bloodTypeTblId;

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetch() {
        //  요청 URL 구성 (최근 3개 연도, 연간, 전체 지역, 3개 항목)
        String uri = apiUrl
                + "?method=getList"
                + "&apiKey=" + apiKey
                + "&itmId=T001+T002+T003+"
                + "&objL1=ALL"
                + "&format=json&jsonVD=Y"
                + "&prdSe=Y&newEstPrdCnt=3"
                + "&orgId=" + orgId
                + "&tblId=" + tblId;

        // content-type이 text/html로 응답 -> String으로 받음
        String body = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        if (body == null || body.isEmpty()) {
            throw new IllegalStateException("KOSIS API에서 데이터를 가져오지 못했습니다.");
        }

        // 문자열을 직접 JSON으로 파싱
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            // 파싱 실패 시 응답 내용을 로그로 (원인 파악용)
            System.out.println("=== KOSIS 응답 (파싱 실패) ===");
            System.out.println(body);
            throw new IllegalStateException("KOSIS 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchBloodType(){
        String uri = apiUrl
                + "?method=getList"
                + "&apiKey=" + apiKey
                + "&itmId=T001+T002+"
                + "&objL1=0"
                + "&objL2=ALL&objL3=ALL"
                + "&format=json&jsonVD=Y"
                + "&prdSe=Y&newEstPrdCnt=3"
                + "&orgId=" + orgId
                + "&tblId=" + bloodTypeTblId;

        String body = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        if (body == null || body.isEmpty()) {
            throw new IllegalStateException("KOSIS 혈액형 통계 응답이 비어있습니다.");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {});
        }catch (Exception e){
            throw new IllegalStateException("KOSIS 혈액형 응답 파싱 실패: " + e.getMessage(), e);
        }
    }
}
