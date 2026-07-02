package io.security.redall.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "oauth_account",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_provider_provider_id",
                columnNames = {"provider", "provider_id"}
        )
)

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthAccount extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // KAKAO, GOOGLE, NAVER
    @Column(nullable = false, length = 20)
    private String provider;

    //  소셜 제공자가 부여한 고유 ID (숫자여도 문자열로 저장)
    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    //  소셜이 준 원본 이메일 (동의 안하면 null 가능)
    @Column(length = 255)
    private String email;

    @Builder
    public OauthAccount(User user, String provider, String providerId, String email) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
    }
}
