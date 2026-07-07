package io.security.redall.repository;

import io.security.redall.domain.ConfirmEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmEmailRepository extends JpaRepository<ConfirmEmail, Long> {

    // 인증 링크 클릭 시 키로 조회
    Optional<ConfirmEmail> findBySecuredKey(String securedKey);
}
