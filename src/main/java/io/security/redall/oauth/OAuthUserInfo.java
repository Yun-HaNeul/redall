package io.security.redall.oauth;

public record OAuthUserInfo(
        OAuthProvider provider,
        String providerId,  // 소셜에서의 고유 회원번호
        String email,
        String name
) { }
