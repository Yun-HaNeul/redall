package io.security.redall.controller;

import io.security.redall.domain.User;
import io.security.redall.dto.MeResponse;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내 정보 조회
 * JWT로 인증된 사용자의 정보를 반환
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<MeResponse> getMe(Authentication authentication) {
        // JWT 필터가 인증 정보를 채워두므로 username을 꺼낼 수 있음
        String username = authentication.getName();

        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(MeResponse.from(user));
    }
}
