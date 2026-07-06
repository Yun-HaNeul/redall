package io.security.redall.oauth;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GoogleOAuthClient implements OAuthClient{
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GOOGLE;
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
            throw new IllegalArgumentException("구글 사용자 정보를 가져오지 못했습니다.");
        }

        String providerId = (String) response.get("sub");
        String email = (String) response.get("email");
        String name = (String) response.get("name");

        return new OAuthUserInfo(OAuthProvider.GOOGLE, providerId, email, name);
    }
}
