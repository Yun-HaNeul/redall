package io.security.redall.oauth;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class KakaoOAuthClient implements OAuthClient {
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuthUserInfo getUserInfo(String accessToken) {
        Map<String, Object> response = restClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        if(response == null){
            throw new IllegalArgumentException("카카오 사용자 정보를 가져오지 못했습니다.");
        }

        String providerId = String.valueOf(response.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao");
        String email = kakaoAccount != null ? String.valueOf(kakaoAccount.get("email")) : null;

        Map<String, Object> properties = (Map<String, Object>) response.get("properties");
        String name = properties != null ? String.valueOf(properties.get("nickname")) : "카카오사용자";

        return new OAuthUserInfo(OAuthProvider.KAKAO, providerId, email, name);
    }
}
