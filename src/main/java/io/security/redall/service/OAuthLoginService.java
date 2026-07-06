package io.security.redall.service;

import io.security.redall.domain.OauthAccount;
import io.security.redall.domain.Role;
import io.security.redall.domain.User;
import io.security.redall.oauth.OAuthClient;
import io.security.redall.oauth.OAuthClientFactory;
import io.security.redall.oauth.OAuthProvider;
import io.security.redall.oauth.OAuthUserInfo;
import io.security.redall.repository.OauthAccountRepository;
import io.security.redall.repository.RoleRepository;
import io.security.redall.repository.UserRepository;
import io.security.redall.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final OAuthClientFactory oAuthClientFactory;
    private final OauthAccountRepository oauthAccountRepository;
    private final UserRepository userRepository;
    private final RoleRepository repository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;

    @Transactional
    public Map<String, String> login(OAuthProvider provider, String accessToken){
        //  1. 팩토리에서 알맞은 클라이언트를 골라 사용자 정보 조회
        OAuthClient client = oAuthClientFactory.getClient(provider);
        OAuthUserInfo userInfo = client.getUserInfo(accessToken);

        // 2. 기존 연동 계정 확인 -> 있으면 해당 회원으로, 없으면 자동 가입
        User user = oauthAccountRepository
                .findByProviderAndProviderId(provider.name(), userInfo.providerId())
                .map(OauthAccount::getUser)
                .orElseGet(() -> registerNewSocialUser(userInfo));

        // 3. Jwt 발급
        String authorities = user.getRoles().stream()
                .map(Role::getAuthority)
                .collect(Collectors.joining(","));

        String jwtAccessToken = jwtTokenProvider.createAccessToken(user.getUsername(), authorities);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

        return Map.of(
                "accessToken",jwtAccessToken,
                "refreshToken",refreshToken
        );
    }

    // 신규 소셜 사용자 자동 가입
    private User registerNewSocialUser(OAuthUserInfo userInfo) {
        String username = userInfo.provider().name().toLowerCase()
                + "_" + UUID.randomUUID().toString().substring(0, 8);

        User user = User.builder()
                .username(username)
                .password(null)
                .email(userInfo.email())
                .name(userInfo.name())
                .build();

        user.verifyEmail();     //  provider가 인증했으므로 바로 활성화

        Role userRole = roleRepository.findByAuthority("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("기본 권한이 없습니다."));

        user.addRole(userRole);
        userRepository.save(user);

        OauthAccount oauthAccount = OauthAccount.builder()
                .user(user)
                .provider(userInfo.provider().name())
                .providerId(userInfo.providerId())
                .email(userInfo.email())
                .build();
        oauthAccountRepository.save(oauthAccount);

        return user;

    }
}
