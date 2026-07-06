package io.security.redall.service;

import io.security.redall.domain.User;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * 비밀번호 찾기
 * 이메일로 회원을 확인하고 임시 비밀번호 발급
 * 임시 비번 발급 시 잠금도 함께 해제 (본인 인증 된 것으로 간주)
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int TEMP_PASSWORD_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * 비밀번호 찾기 처리
     * 보안상 이메일 존재 여부와 무관하게 동일하게 응답 ( 계절 노출 방지 )
     */
    @Transactional
    public void resetPassword(String email){
        userRepository.findByEmail(email).ifPresent(user -> {
            // 1. 임시 비밀번호 생성
            String tempPassword = generateTempPassword();

            // 2. 암호화해서 저장 + is_temp_password = 1 + 잠금해제 (엔티티 메서드 한 번에 처리)
            user.issueTempPassword(passwordEncoder.encode(tempPassword));

            // 3. 이메일 발송
            emailService.sendTempPassword(user.getEmail(), tempPassword);
        });
    }

    private String generateTempPassword(){
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++){
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 비밀번호 변경
     * 임시 비밀번호로 로그인한 사용자가 새 비번으로 바꿀 떄 사용
     * 현재 비번을 확인한 뒤 새 비번으로 교체하고 is_temp_password 를 해제
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비번으로 교체 + 임시 비번 상태 해제
        user.changePassword(passwordEncoder.encode(newPassword));
    }
}
