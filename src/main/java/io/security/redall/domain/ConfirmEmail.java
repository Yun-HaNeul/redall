package io.security.redall.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "confirm_email")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfirmEmail extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String email;

    /**
     * 인증용 토큰 (UUID 등) -> 링크에 담아 보냄
     */
    @Column(nullable = false, unique = true, length = 255)
    private String securedKey;

    @Column(nullable = false)
    private LocalDateTime dateExpired;

    @Column(nullable = false)
    private boolean verified;

    @Builder
    public ConfirmEmail(User user, String email, String securedKey, LocalDateTime dateExpired){
        this.user = user;
        this.email = email;
        this.securedKey = securedKey;
        this.dateExpired = dateExpired;
        this.verified = false;
    }

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(dateExpired);
    }

    public void markVerified(){
        this.verified = true;
    }

    public boolean isVerified(){
        return verified;
    }
}
