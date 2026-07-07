package io.security.redall.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 네이버 인가 코드(code)를 access token으로 교환.
 * POST https://nid.naver.com/oauth2.0/token
 * 네이버는 state 파라미터가 추가로 필요
 *
 필요한 설정(application.properties)
 *  oauth.naver.client-id
 *  oauth.naver.client-secret
 *  oauth.naver.redirect-uri
 */
@Component
public class NaverTokenClient {
    private static final String TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private final RestClient restClient = RestClient.create();

    @Value("${oauth.naver.client-id}")
    private String clientId;

    @Value("${oauth.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth.naver.redirect-uri}")
    private String redirectUri;

    @SuppressWarnings("unchekced")
    public String getAccessToken(String code, String state){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("state", state);   // 네이버는 state 필요

        Map<String, Object> response = restClient.post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("access_token") == null) {
            throw new IllegalArgumentException("네이버 토큰 발급에 실패했습니다.");
        }
        return (String) response.get("access_token");
    }
}
