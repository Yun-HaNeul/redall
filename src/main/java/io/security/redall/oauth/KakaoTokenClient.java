package io.security.redall.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 카카오 인가 코드(code)를 access token으로 교환하는 역할
 * 리다이렉트 방식에서 프론트가 받은 code를 백엔드가 토큰으로 바꿈
 *
 * 필요한 설정(application.properties)
 *  oauth.kakao.client-id       =   REST API 키
 *  oauth.kakao.client-secret   =   카카오 로그인 > 보안 > Client Secret
 *  oauth.kakao.redirect-uri    =   프론트 콜백 주소 (카카오에 등록한 값과 동일)
 */
@Component
public class KakaoTokenClient {
    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private final RestClient restClient = RestClient.create();

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 인가 코드로 카카오 access token 을 발급
     */
    @SuppressWarnings("unchecked")
    public String getAccessToken(String code) {
        // 카카오는 form 형식(x-www-form-urlencoded)로 받음
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        Map<String, Object> response = restClient.post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(Map.class);

        if(response == null || response.get("access_token") == null){
            throw new IllegalArgumentException("카카오 토큰 발급에 실패했습니다.");
        }

        return (String) response.get("access_token");
    }
}
