package io.security.redall.controller;

import io.security.redall.dto.PasswordChangeRequest;
import io.security.redall.dto.PasswordResetRequest;
import io.security.redall.dto.SignupRequest;
import io.security.redall.service.AuthService;
import io.security.redall.service.EmailVerificationService;
import io.security.redall.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

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

    /**
     * 비밀번호 찾기 (임시 비번 발급) - 이메일 존재 여부와 무관하게 동일 응답
     */
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request){
        passwordResetService.resetPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "입력하신 이메일로 임시 비밀번호를 발생했습니다."
        ));
    }

    /**
     * 비밀번호 변경 (임시 비번 -> 새 비번). 로그인 상태에서 호출
     */
    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest request, Authentication authentication){
        String username = authentication.getName(); // JWT에서 추출된 로그인 아이디
        passwordResetService.changePassword(
                username, request.getCurrentPassword(), request.getNewPassword()
        );
        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 변경되었습니다."
        ));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> vefiryEmail(@RequestParam("key") String securedKey){
        emailVerificationService.verify(securedKey);
        return ResponseEntity.ok(Map.of(
                "message", "이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다."
        ));
    }

}
