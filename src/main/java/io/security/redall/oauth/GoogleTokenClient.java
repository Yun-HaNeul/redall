package io.security.redall.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 구글 인가 코드(code)를 access token으로 교환
 * POST https://oauth2.googleapis.com/token
 *
 * 필요한 설정(application.properties)
 *  oauth.google.client-id
 *  oauth.google.client-secret
 *  oauth.google.redirect-uri
 */
@Component
public class GoogleTokenClient {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private final RestClient restClient = RestClient.create();

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    @SuppressWarnings("unchecked")
    public String getAccessToken(String code){
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
            throw new IllegalArgumentException("구글 토큰 발급에 실패했습니다.");
        }

        return (String) response.get("access_token");
    }


}

