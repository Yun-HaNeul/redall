package io.security.redall.controller;

import io.security.redall.dto.OAuthLoginRequest;
import io.security.redall.service.OAuthLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthLoginService oAuthLoginService;

    /**
     * 소셜 로그인 (access token 직접 전잘 방식)
     * 프론트가 이미 provider 토큰을 가진 경우 사용
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<?> oauthLogin(@Valid @RequestBody OAuthLoginRequest request){
        Map<String, String> tokens = oAuthLoginService.login(
                request.getProvider(), request.getAccessToken()
        );
        return ResponseEntity.ok(tokens);
    }

    /**
     * 카카오 리다이렉트 로그인 (인가 코드 방식)
     * @param body
     * @return
     */
    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body){
        String code = body.get("code");
        Map<String, String> tokens = oAuthLoginService.loginWithKakaoCode(code);
        return ResponseEntity.ok(tokens);
    }

    /**
     * 구글 리다이렉트 로그인 (인가 코드 방식)
     * @param body
     * @return
     */
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body){
        String code = body.get("code");
        Map<String, String> tokens = oAuthLoginService.loginWithGoogleCode(code);
        return ResponseEntity.ok(tokens);
    }

    /**
     * 네이버 리다이렉트 로그인
     * @param body
     * @return
     */
    @PostMapping("/naver")
    public ResponseEntity<?> naverLogin(@RequestBody Map<String, String> body){
        String code = body.get("code");
        String state = body.get("state");
        Map<String, String> tokens = oAuthLoginService.loginWithNaverCode(code, state);
        return ResponseEntity.ok(tokens);
    }
}
