package io.security.redall.oauth;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthClientFactory {
    private final Map<OAuthProvider, OAuthClient> clients;

    // 스프링이 모든 OAuthClient 구현체(카카오, 구글) 를 List로 자동 주입
    public OAuthClientFactory(List<OAuthClient> clientList) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, Function.identity()));
    }

    public OAuthClient getClient(OAuthProvider provider) {
        OAuthClient client = clients.get(provider);

        if(client == null) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + provider);
        }

        return client;
    }
}
