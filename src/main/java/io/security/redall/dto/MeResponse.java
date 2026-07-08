package io.security.redall.dto;

import io.security.redall.domain.User;

import java.util.List;

/**
 * 내 정보 조회 응답
 * JWT로 인증된 사용자의 기본 정보
 */
public record MeResponse(
        Long id,
        String username,
        String name,
        String mail,
        List<String> roles
) {
    /**
     * User 엔티티를 응답 DTO로 변환
     * from 메서드가 User 엔티티를 이 응답 형태로 바꿔줌
     * @param user
     * @return
     */
    public static MeResponse from(User user){
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getAuthority())
                .toList();

        return new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                roleNames
        );
    }

}
