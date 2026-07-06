package io.security.redall.service;

import io.security.redall.domain.User;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 로그인 잠금 관련 로직
 * - 로그인 실패 시 카운트 증가 (5회 도달 시 잠금)
 * - 로그인 성공 시 카운트 리셋
 * = 로그인 시도 시 잠금 자동 해제 판단 ( 10분 경과)
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final long UNLOCK_AFTER_MINUTES = 10;    // 자동 해제 시간

    private final UserRepository userRepository;

    /**
     * 로그인 실패 처리 : 해당 아이디의 회원을 찾아 실패 카운트를 ++
     * 존재하지 않는 아이디면 조용히 무시 (계정 존재 여부 노출 방지)
     */
    @Transactional
    public void handleFailure(String username){
        userRepository.findByUsername(username).ifPresent(user -> {
            user.increaseLoginFailCount();  // 엔티티 내부에서 5회 도달 시 잠금 처리
        });
    }

    /**
     * 로그인 성공 처리: 실패 카운트를 0으로 리셋
     */
    @Transactional
    public void handleSuccess(String username){
        userRepository.findByUsername(username).ifPresent(User::resetLoginFailCount);
    }

    /**
     * 잠금 자동 해제 판단.
     * 잠김 계정인데 잠금 시각으로부터 10분이 지났을 시 해제
     * 인증 시도 전에 호출
     */
    @Transactional
    public void unlockIfExpired(String username){
        userRepository.findByUsername(username).ifPresent(user -> {
            if(user.isAccountLocked() && user.getLockedAt() != null){
                LocalDateTime unlockTime = user.getLockedAt().plusMinutes(UNLOCK_AFTER_MINUTES);
                if(LocalDateTime.now().isAfter(unlockTime)){
                    user.unlock();  // 10분 경과 -> 자동 해제
                }
            }
        });
    }

}
