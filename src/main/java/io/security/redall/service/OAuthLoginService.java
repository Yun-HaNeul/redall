package io.security.redall.service;

import io.security.redall.domain.OauthAccount;
import io.security.redall.domain.Role;
import io.security.redall.domain.User;
import io.security.redall.oauth.*;
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

/**
 * 소셜 로그인 처리
 * 프론트가 받은 provider access token 을 받아:
 * 1) provider 에 사용자 정보 조회
 * 2) oauth_account 로 기존 연동 확인
 * 3) 있으면 로그인 / 없으면 자동 회원가입
 * 4) 우리 JWT 발급
 */

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final OAuthClientFactory oAuthClientFactory;
    private final OauthAccountRepository oauthAccountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoTokenClient kakaoTokenClient;
    private final GoogleTokenClient googleTokenClient;
    private final NaverTokenClient naverTokenClient;

    /**
     * 카카오 리다이렉트 로그인 처리
     * 프론트가 받은 인가 코드(code)를 access token으로 교환한 뒤
     * 기존 소셜 로그인 흐름(login)을 그대로 재활용
     * @return
     */
    @Transactional
    public Map<String, String> loginWithKakaoCode(String code){
        String accessToken = kakaoTokenClient.getAccessToken(code);
        return login(OAuthProvider.KAKAO, accessToken);
    }

    /**
     * 구글 리다이렉트 로그인 처리
     * @param code
     * @return
     */
    @Transactional
    public Map<String, String> loginWithGoogleCode(String code){
        String accessToken = googleTokenClient.getAccessToken(code);
        return login(OAuthProvider.GOOGLE, accessToken);
    }

    /**
     * 구글 리다이렉트 로그인 처리
     * @param code
     * @return
     */
    @Transactional
    public Map<String, String> loginWithNaverCode(String code, String state){
        String accessToken = naverTokenClient.getAccessToken(code, state);
        return login(OAuthProvider.NAVER, accessToken);
    }


    @Transactional
    public Map<String, String> login(OAuthProvider provider, String accessToken){
        //  1. 팩토리에서 알맞은 클라이언트를 골라 provider에 사용자 정보 조회
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

    /**
     * 신규 소셜 사용자 자동 가잊
     * username 은 자동 생성, password 는 없음(null), 이메일 인증 불필요(enabled=true).
     * @param userInfo
     * @return
     */

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
