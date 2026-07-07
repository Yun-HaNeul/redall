package io.security.redall.oauth;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 네이버 사용자 정보 조회
 * GET https://openapi.naver.com/v1/nid/me (Bearer 토근)
 * 응답: { resultcode, message, response: {id, email, name, ...}}
 * -> 카카오/구글과 달리 실제 정보가 response 객체 안에 포함
 */
@Component
public class NaverOAuthClient implements OAuthClient {
    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.NAVER;
    }

    @Override
    @SuppressWarnings("unchekced")
    public OAuthUserInfo getUserInfo(String accessToken) {
        Map<String, Object> body = restClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        // 디버깅: 네이버 사용자 정보 응답 확인
        System.out.println("=== 네이버 사용자 정보 응답 ===");
        System.out.println(body);

        if (body == null) {
            throw new IllegalArgumentException("네이버 사용자 정보를 가져오지 못했습니다.");
        }

        // 네이버는 실제 정보가 response 객체 안에 있음
        Map<String, Object> response = (Map<String, Object>) body.get("response");
        if (response == null) {
            throw new IllegalArgumentException("네이버 응답 형식이 올바르지 않습니다.");
        }

        String providerId = (String) response.get("id");    // 네이버 고유 ID
        String email = (String) response.get("email");
        String name = (String) response.get("name");

        // 디버깅: 파싱한 값 확인
        System.out.println("providerId: " + providerId + ", email: " + email + ", name: " + name);

        return new OAuthUserInfo(OAuthProvider.NAVER, providerId, email, name);
    }
}
