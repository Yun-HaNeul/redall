package io.security.redall.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그인 이력
 * 보안/감사 목적으로 로그인 시각과 접속 IP 기록
 * 로그인 성공 시 한 건씩 INSERT
 */

@Getter
@Entity
@Table(name = "login_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime loggedInAt;

    // IPv6까지 고려해 45자
    @Column(length = 45)
    private String remoteAddr;

    @Builder
    public LoginHistory(User user, String remoteAddr) {
        this.user = user;
        this.remoteAddr = remoteAddr;
        this.loggedInAt = LocalDateTime.now();
    }


}
