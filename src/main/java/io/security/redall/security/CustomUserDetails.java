package io.security.redall.security;

import io.security.redall.domain.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * User 엔티티를 Spring Security 가 이해하는 UserDetails 로 감싸는 어댑터.
 * 앞서 설계한 계정 상태 플래그들이 여기서 각 메서드에 1:1로 대응
 * 로그인 시 Security 가 이 메서드들을 호출해 잠금/활성/만료 여부를 판단
 */
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user){
        this.user = user;
    }

    public User getUser(){
        return user;
    }

    public Long getId(){
        return user.getId();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // ===== 계정 상태 (엔티티 플래그와 1:1 대응) =====
    /** 계정 만료 안 됨? (account_expired 의 반대) */
    @Override
    public boolean isAccountNonExpired(){
        return !user.isAccountExpired();
    }

    /** 계정 잠금 안 됨? (account_locked 의 반대) → 5회 실패 잠금이 여기 걸림 */
    @Override
    public boolean isAccountNonLocked(){
        return !user.isAccountLocked();
    }

    /** 비밀번호 만료 안 됨? (password_expired 의 반대) */
    @Override
    public boolean isCredentialsNonExpired(){
        return !user.isPasswordExpired();
    }

    /** 계정 활성화됨? (enabled) → 이메일 인증 안 하면 여기 걸림 */
    @Override
    public boolean isEnabled(){
        return user.isEnabled();
    }
}
