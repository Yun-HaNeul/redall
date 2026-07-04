package io.security.redall.controller;

import io.security.redall.dto.SignupRequest;
import io.security.redall.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 회원가입
     * @Valid 로 SignupRequest 의 형식 검증 먼저 수행
     * 통과 시 AuthService.signup 이 중복검사/암호화/저장 처리
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request){
        Long userId = authService.signup(request);

        return ResponseEntity.ok(Map.of(
                "message", "회원가입이 완료되었습니다.",
                "userId", userId
        ));
    }
}
