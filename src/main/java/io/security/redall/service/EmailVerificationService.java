package io.security.redall.service;

import io.security.redall.domain.ConfirmEmail;
import io.security.redall.domain.User;
import io.security.redall.repository.ConfirmEmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이메일 인증
 * - 회원가입 시: 인증 키 생성 + 저장 + 메일 발송
 * - 링크 클릭 시: 키 검증(유효/만료) 후 계정 활성화(enabled=true)
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final long EXPIRE_HOURS = 24;

    private final ConfirmEmailRepository confirmEmailRepository;
    private final EmailService emailService;

    /** 회원가입 직후: 인증 키 생성 + 저장 + 메일 발송 */
    @Transactional
    public void sendVerificationEmail(User user){
        String securedKey = UUID.randomUUID().toString();

        ConfirmEmail confirmEmail = ConfirmEmail.builder()
                .user(user)
                .email(user.getEmail())
                .securedKey(securedKey)
                .dateExpired(LocalDateTime.now().plusHours(EXPIRE_HOURS))
                .build();

        confirmEmailRepository.save(confirmEmail);

        emailService.sendVerificationLink(user.getEmail(), securedKey);
    }

    /** 링크 클릭: 키 검증 후 계정 활성화 */
    @Transactional
    public void verify(String securedKey){
        ConfirmEmail confirmEmail = confirmEmailRepository.findBySecuredKey(securedKey)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 링크입니다."));

        if(confirmEmail.isVerified()){
            throw new IllegalArgumentException("이미 인증이 완료된 링크입니다.");
        }

        if(confirmEmail.isExpired()){
            throw new IllegalArgumentException("인증 링크가 만료되었습니다. 인증 메일을 다시 요청하세요.");
        }

        confirmEmail.getUser().verifyEmail();
        confirmEmail.markVerified();
    }
}
