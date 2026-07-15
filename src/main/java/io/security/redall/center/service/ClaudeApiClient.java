package io.security.redall.center.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Claude API 호출 클라이언트
 * 프롬프트를 받아서 Claude API 형식으로 요청을 만들고,
 * 헤더에 API 키를 넣어 호출 -> 응답에서 실제 답변 텍스트(content[0].text)를 추출
 */
@Component
public class ClaudeApiClient {
    private final RestClient restClient = RestClient.create();

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    @Value("${anthropic.api.model}")
    private String model;

    /**
     * 프롬프르틑 보내고 Claude의 텍스트 응답을 받는다.
     * @param prompt
     * @return
     */
    @SuppressWarnings("unchecked")
    public String generate(String prompt){
        return "지금까지 꾸준히 헌혈해주셔서 감사해요! 🩸 " +
           "당신의 헌혈은 소중한 생명을 살리는 데 큰 힘이 됩니다. " +
           "다음 헌혈 가능일을 확인하고, 가까운 헌혈의 집을 방문해보세요! 😊";
        // 요청 본문 구성
        /*Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1024,
                "meesage", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri(apiUrl)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null){
                throw new IllegalStateException("Claude API 응답이 없습니다.");
            }

            //  content[0].text 추출
            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) response.get("content");

            if (content == null || content.isEmpty()) {
                throw new IllegalStateException("Claude API 응답에 내용이 없습니다.");
            }

            return (String) content.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("AI 인사이트 생성 중 오류: " + e.getMessage(), e);
        }*/
    }
}
