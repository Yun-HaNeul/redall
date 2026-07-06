package io.security.redall.repository;

import io.security.redall.domain.OauthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface OauthAccountRepository extends JpaRepository<OauthAccount, Long> {

    // 소셜 로그인 시 기존 연계 계정 조회 (provider + providerId)로 찾음

    Optional<OauthAccount> findByProviderAndProviderId(String provider, String providerId);
}
