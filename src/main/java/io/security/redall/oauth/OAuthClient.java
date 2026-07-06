package io.security.redall.oauth;

public interface OAuthClient {
    OAuthProvider getProvider();                    //  내가 담당하는 provider

    OAuthUserInfo getUserInfo(String accessToken);  //   토큰으로 정보 조회
}
