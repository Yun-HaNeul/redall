package io.security.redall.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 회원 엔티티:
 *  - 일반 가입: username(직접 입력) + password(BCrypt)
 *  - 소셜 가입: username(자동 생성) + password(null). oauth_account로 연동
 *  계정 상태 플래그는 Spring Security UserDetails 메서드와 1:1 대응
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 ID, 소셜은 자동 생성값, 자동 생성값 길이를 고려해 50
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // 소셜 로그인 유저는 비밀번호가 없으므로 nullable
    @Column(nullable = true)
    private String password;

    @Column(length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String name;

    /** 혈액형 (A, B, O, AB) - 등록 안 하면 null */
    @Column(length = 10)
    private String bloodType;

    /** RH 타입 (POSITIVE, NEGATIVE) */
    @Column(length = 10)
    private String rhType;

    /**
     *  계정 상태 (Security UserDetails 대응)
     *  이메일 인증 완료 여부 (가입 직후 false, 인증 링크 클릭 시 true)
     */
    @Column(nullable = false)
    private boolean enabled;

    @Column
    private boolean accountExpired;

    @Column
    private boolean accountLocked;

    @Column
    private boolean passwordExpired;

    // 소프트 삭제 (탈퇴)
    @Column(nullable = false)
    private boolean withdraw;

    /**
     * 로그인 잠금 관련
     */
    @Column(nullable = false)
    private int loginFailCount;

    private LocalDateTime lockedAt;     // 잠금 시각 (10분 자동 해제 기준)

    /**
     * 임시 비밀번호
     */
    @Column(nullable = false)
    private boolean isTempPassword;

    /**
     * 기록용 시각/IP
     */
    private LocalDateTime withdrawnAt;
    private LocalDateTime passwordChangedAt;
    @Column(length = 45)
    private String createdIp;
    @Column(length = 45)
    private String lastUpdateIp;

    /**
     * 권한 (N:N)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Builder
    public User(String username, String password, String email, String name,
                String createdIp){
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.createdIp = createdIp;

        // 가입 시 기본 상태: 이메일 인증 전이라 비활성
        this.enabled = false;
        this.accountExpired = false;
        this.accountLocked = false;
        this.passwordExpired = false;
        this.withdraw = false;
        this.loginFailCount = 0;
        this.isTempPassword = false;
    }

    /**
     * 도메인 행위 메서드
     */

    public void addRole(Role role){
        this.roles.add(role);
    }

    /**
     * 이메일 인증 완료 -> 계정 활성화
     */
    public void verifyEmail(){
        this.enabled = true;
    }

    /**
     * 로그인 실패 시 호출
     * -> 5회 도달 시 잠금 처리
     */
    public void increaseLoginFailCount(){
        this.loginFailCount++;
        if(this.loginFailCount == 5){
            this.accountLocked = true;
            this.lockedAt = LocalDateTime.now();
        }
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     */
    public void resetLoginFailCount(){
        this.loginFailCount = 0;
    }

    /**
     * 잠금 해제 (10분 자동 해제 또는 비번 찾기 시)
     */
    public void unlock(){
        this.accountLocked = false;
        this.loginFailCount = 0;
        this.lockedAt = null;
    }

    /**
     * 임시 비밀번호 발급
     *  : 비번 교체 + 표시 + 잠금 해제까지 한 번에
     */
    public void issueTempPassword(String encodedTempPassword){
        this.password = encodedTempPassword;
        this.isTempPassword = true;
        this.passwordChangedAt = LocalDateTime.now();
    }

    /**
     * 새 비밀번호로 변경: 임시 비번 상태 해제
     */
    public void changePassword(String encodedPassword){
        this.password = encodedPassword;
        this.isTempPassword = false;
        this.passwordChangedAt = LocalDateTime.now();
    }

    /**
     * 탈퇴 (소프트 삭제)
     */
    public void withdrawAccount(){
        this.withdraw = true;
        this.withdrawnAt = LocalDateTime.now();
        this.enabled = false;
    }

    /**
     * 혈액형 등록/수정
     * @param bloodType
     * @param rhType
     */
    public void updateBloodType(String bloodType, String rhType) {
    this.bloodType = bloodType;
    this.rhType = rhType;
}

}
